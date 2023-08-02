package com.vicmatskiv.weaponlib;

import com.vicmatskiv.weaponlib.compatibility.CompatibilityProvider;
import com.vicmatskiv.weaponlib.compatibility.CompatibleSound;
import com.vicmatskiv.weaponlib.state.Aspect;
import com.vicmatskiv.weaponlib.state.ManagedState;
import com.vicmatskiv.weaponlib.state.PermitManager;
import com.vicmatskiv.weaponlib.state.StateManager;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WeaponFireAspect implements Aspect<WeaponState, PlayerWeaponInstance> {
	private static final Logger logger = LogManager.getLogger(WeaponFireAspect.class);

	private static final float FLASH_X_OFFSET_ZOOMED = 0.0F;

	private static final long ALERT_TIMEOUT = 500L;

	private static Predicate<PlayerWeaponInstance> readyToShootAccordingToFireRate;

	private static Predicate<PlayerWeaponInstance> postBurstTimeoutExpired;

	private static Predicate<PlayerWeaponInstance> readyToShootAccordingToFireMode;

	static {
		readyToShootAccordingToFireRate = (instance -> ((float) (System.currentTimeMillis()
				- instance.getLastFireTimestamp()) >= 50.0F / (instance.getWeapon()).builder.fireRate));

		postBurstTimeoutExpired = (instance -> (System.currentTimeMillis()
				- instance.getLastBurstEndTimestamp() >= (instance.getWeapon()).builder.burstTimeoutMilliseconds));

		readyToShootAccordingToFireMode = (instance -> (instance.getSeriesShotCount() < instance.getMaxShots()));
	}

	private static Predicate<PlayerWeaponInstance> oneClickBurstEnabled = PlayerWeaponInstance::isOneClickBurstAllowed;
	private static Predicate<PlayerWeaponInstance> hasAmmo;
	private static Predicate<PlayerWeaponInstance> ejectSpentRoundRequired;
	private static Predicate<PlayerWeaponInstance> ejectSpentRoundTimeoutExpired;
	private static Predicate<PlayerWeaponInstance> seriesResetAllowed = PlayerWeaponInstance::isSeriesResetAllowed;
	private static Predicate<PlayerWeaponInstance> alertTimeoutExpired;
	static {
		hasAmmo = (instance -> (instance.getAmmo() > 0 && Tags.getAmmo(instance.getItemStack()) > 0));

		ejectSpentRoundRequired = (instance -> instance.getWeapon().ejectSpentRoundRequired());

		ejectSpentRoundTimeoutExpired = (instance -> (System
				.currentTimeMillis() >= (instance.getWeapon()).builder.pumpTimeoutMilliseconds
						+ instance.getStateUpdateTimestamp()));

		alertTimeoutExpired = (instance -> (System.currentTimeMillis() >= 500L + instance.getStateUpdateTimestamp()));

		sprinting = (instance -> instance.getPlayer().isSprinting());
	}
	private static Predicate<PlayerWeaponInstance> sprinting;
	private static final Set<WeaponState> allowedFireOrEjectFromStates = new HashSet<>(
			Arrays.asList(new WeaponState[] { WeaponState.READY, WeaponState.PAUSED, WeaponState.EJECT_REQUIRED }));

	private static final Set<WeaponState> allowedUpdateFromStates = new HashSet<>(
			Arrays.asList(new WeaponState[] { WeaponState.EJECTING, WeaponState.PAUSED, WeaponState.FIRING,
					WeaponState.RECOILED, WeaponState.PAUSED, WeaponState.ALERT }));

	private ModContext modContext;

	private StateManager<WeaponState, ? super PlayerWeaponInstance> stateManager;

	public WeaponFireAspect(CommonModContext modContext) {
		this.modContext = modContext;
	}

	public void setPermitManager(PermitManager permitManager) {
	}

	public void setStateManager(StateManager<WeaponState, ? super PlayerWeaponInstance> stateManager) {
		this.stateManager = stateManager;

		stateManager

				.in(this).change(WeaponState.READY).to(WeaponState.ALERT).when(hasAmmo.negate())
				.withAction(this::cannotFire).manual()

				.in(this).change(WeaponState.ALERT).to(WeaponState.READY).when(alertTimeoutExpired).automatic()

				.in(this).change(WeaponState.READY).to(WeaponState.FIRING)
				.when(hasAmmo.and(sprinting.negate()).and(readyToShootAccordingToFireRate)).withAction(this::fire)
				.manual()

				.in(this).change(WeaponState.FIRING).to(WeaponState.RECOILED).automatic()

				.in(this).change(WeaponState.RECOILED).to(WeaponState.PAUSED).automatic()

				.in(this).change(WeaponState.PAUSED).to(WeaponState.EJECT_REQUIRED).when(ejectSpentRoundRequired)
				.manual()

				.in(this).change(WeaponState.EJECT_REQUIRED).to(WeaponState.EJECTING).withAction(this::ejectSpentRound)
				.manual()

				.in(this).change(WeaponState.EJECTING).to(WeaponState.READY).when(ejectSpentRoundTimeoutExpired)
				.automatic()

				.in(this).change(WeaponState.PAUSED).to(WeaponState.FIRING)
				.when(hasAmmo.and(sprinting.negate()).and(readyToShootAccordingToFireMode)
						.and(readyToShootAccordingToFireRate))

				.withAction(this::fire).manual()

				.in(this).change(WeaponState.PAUSED).to(WeaponState.FIRING)
				.when(hasAmmo.and(sprinting.negate()).and(oneClickBurstEnabled).and(readyToShootAccordingToFireMode)
						.and(readyToShootAccordingToFireRate))

				.withAction(this::fire).automatic()

				.in(this).change(WeaponState.PAUSED).to(WeaponState.READY)
				.when(ejectSpentRoundRequired.negate().and(oneClickBurstEnabled)
						.and(readyToShootAccordingToFireMode.negate().or(hasAmmo.negate())).and(seriesResetAllowed)
						.and(postBurstTimeoutExpired))

				.withAction(PlayerWeaponInstance::resetCurrentSeries).automatic()

				.in(this).change(WeaponState.PAUSED).to(WeaponState.READY)
				.when(ejectSpentRoundRequired.negate().and(oneClickBurstEnabled.negate()))
				.withAction(PlayerWeaponInstance::resetCurrentSeries).manual();
	}

	void onFireButtonDown(EntityPlayer player) {
		PlayerWeaponInstance weaponInstance = this.modContext.getPlayerItemInstanceRegistry()
				.getMainHandItemInstance((EntityLivingBase) player, PlayerWeaponInstance.class);
		if (weaponInstance != null) {
			this.stateManager.changeStateFromAnyOf(this, weaponInstance, allowedFireOrEjectFromStates,WeaponState.FIRING, WeaponState.EJECTING, WeaponState.ALERT);

		}
	}

	public void onFireButtonRelease(EntityPlayer player) {
        PlayerWeaponInstance weaponInstance = (PlayerWeaponInstance) this.modContext.getPlayerItemInstanceRegistry().getMainHandItemInstance(player, PlayerWeaponInstance.class);
        if (weaponInstance != null) {
            weaponInstance.setSeriesResetAllowed(true);
            this.stateManager.changeState(this, weaponInstance, WeaponState.EJECT_REQUIRED, WeaponState.READY);
        }
    }

	public void onUpdate(EntityPlayer player) {
        PlayerWeaponInstance weaponInstance = (PlayerWeaponInstance) this.modContext.getPlayerItemInstanceRegistry().getMainHandItemInstance(player, PlayerWeaponInstance.class);
        if (weaponInstance != null) {
            this.stateManager.changeStateFromAnyOf(this, weaponInstance, allowedUpdateFromStates, new WeaponState[0]);
        }
    }

	private void cannotFire(PlayerWeaponInstance weaponInstance) {
		if (weaponInstance.getAmmo() == 0 || Tags.getAmmo(weaponInstance.getItemStack()) == 0) {
			String message;
			if (weaponInstance.getWeapon().getAmmoCapacity() == 0 && this.modContext.getAttachmentAspect()
					.getActiveAttachment(weaponInstance, AttachmentCategory.MAGAZINE) == null) {
				message = CompatibilityProvider.compatibility.getLocalizedString("gui.noMagazine", new Object[0]);
			} else {
				message = CompatibilityProvider.compatibility.getLocalizedString("gui.noAmmo", new Object[0]);
			}
			this.modContext.getStatusMessageCenter().addAlertMessage(message, 3, 250L, 200L);
			if (weaponInstance.getPlayer() instanceof EntityPlayer) {
				CompatibilityProvider.compatibility.playSound(weaponInstance.getPlayer(),
						this.modContext.getNoAmmoSound(), 1.0F, 1.0F);
			}
		}
	}

	private void fire(PlayerWeaponInstance weaponInstance) {
		EntityLivingBase player = weaponInstance.getPlayer();
		Weapon weapon = (Weapon) weaponInstance.getItem();
		Random random = player.getRNG();

		this.modContext.getChannel().getChannel().sendToServer((IMessage) new TryFireMessage(true,
				(oneClickBurstEnabled.test(weaponInstance) && weaponInstance.getSeriesShotCount() == 0)));
		boolean silencerOn = this.modContext.getAttachmentAspect().isSilencerOn(weaponInstance);

		CompatibleSound shootSound = null;

		if (oneClickBurstEnabled.test(weaponInstance)) {

			CompatibleSound burstShootSound = null;
			if (silencerOn) {
				burstShootSound = weapon.getSilencedBurstShootSound();
			}
			if (burstShootSound == null) {
				burstShootSound = weapon.getBurstShootSound();
			}
			if (burstShootSound != null) {
				if (weaponInstance.getSeriesShotCount() == 0) {
					shootSound = burstShootSound;
				}
			} else {
				shootSound = silencerOn ? weapon.getSilencedShootSound() : weapon.getShootSound();
			}
		} else {
			shootSound = silencerOn ? weapon.getSilencedShootSound() : weapon.getShootSound();
		}

		if (shootSound != null) {
			CompatibilityProvider.compatibility.playSound(player, shootSound,
					silencerOn ? weapon.getSilencedShootSoundVolume() : weapon.getShootSoundVolume(), 1.0F);
		}

		int currentAmmo = weaponInstance.getAmmo();
		if (currentAmmo == 1 && weapon.getEndOfShootSound() != null) {
			CompatibilityProvider.compatibility.playSound(player, weapon.getEndOfShootSound(), 1.0F, 1.0F);
		}

		player.rotationPitch -= weaponInstance.getRecoil();
		float rotationYawFactor = -1.0F + random.nextFloat() * 2.0F;
		player.rotationYaw += weaponInstance.getRecoil() * rotationYawFactor;

		Boolean muzzleFlash = this.modContext.getConfigurationManager().getProjectiles().isMuzzleEffects();
		if ((muzzleFlash == null || muzzleFlash.booleanValue()) && weapon.builder.flashIntensity > 0.0F) {
			this.modContext.getEffectManager().spawnFlashParticle(player, weapon.builder.flashIntensity,
					((Float) weapon.builder.flashScale.get()).floatValue(),
					weaponInstance.isAimed() ? 0.0F
							: (CompatibilityProvider.compatibility.getEffectOffsetX()
									+ ((Float) weapon.builder.flashOffsetX.get()).floatValue()),
					CompatibilityProvider.compatibility.getEffectOffsetY()
							+ ((Float) weapon.builder.flashOffsetY.get()).floatValue(),
					weapon.builder.flashTexture);
		}

		if (weapon.isSmokeEnabled()) {
			this.modContext.getEffectManager().spawnSmokeParticle(player,
					CompatibilityProvider.compatibility.getEffectOffsetX()
							+ ((Float) weapon.builder.smokeOffsetX.get()).floatValue(),
					CompatibilityProvider.compatibility.getEffectOffsetY()
							+ ((Float) weapon.builder.smokeOffsetY.get()).floatValue());
		}

		int seriesShotCount = weaponInstance.getSeriesShotCount();
		if (seriesShotCount == 0) {
			weaponInstance.setSeriesResetAllowed(false);
		}

		weaponInstance.setSeriesShotCount(seriesShotCount + 1);
		if (currentAmmo == 1 || weaponInstance.getSeriesShotCount() == weaponInstance.getMaxShots()) {
			weaponInstance.setLastBurstEndTimestamp(System.currentTimeMillis());
		}
		weaponInstance.setLastFireTimestamp(System.currentTimeMillis());
		weaponInstance.setAmmo(currentAmmo - 1);
	}

	private void ejectSpentRound(PlayerWeaponInstance weaponInstance) {
		EntityLivingBase player = weaponInstance.getPlayer();
		CompatibilityProvider.compatibility.playSound(player, weaponInstance.getWeapon().getEjectSpentRoundSound(),
				1.0F, 1.0F);
	}

	public void serverFire(EntityLivingBase player, ItemStack itemStack, boolean isBurst) {
		serverFire(player, itemStack, null, isBurst);
	}

	public void serverFire(EntityLivingBase player, ItemStack itemStack,
			BiFunction<Weapon, EntityLivingBase, ? extends WeaponSpawnEntity> spawnEntityWith, boolean isBurst) {
		if (!(itemStack.getItem() instanceof Weapon)) {
			return;
		}
		Weapon weapon = (Weapon) itemStack.getItem();

		int currentServerAmmo = Tags.getAmmo(itemStack);

		
		if (currentServerAmmo <= 0) {
			logger.error("No server ammo");

			return;
		}
		Tags.setAmmo(itemStack, --currentServerAmmo);

		if (spawnEntityWith == null) {
			spawnEntityWith = weapon.builder.spawnEntityWith;
		}

		for (int i = 0; i < weapon.builder.pellets; i++) {
			WeaponSpawnEntity spawnEntity = spawnEntityWith.apply(weapon, player);
			CompatibilityProvider.compatibility.spawnEntity(player, spawnEntity);
		}

		PlayerWeaponInstance playerWeaponInstance = Tags.<PlayerWeaponInstance>getInstance(itemStack,
				PlayerWeaponInstance.class);

		if (weapon.isShellCasingEjectEnabled() && playerWeaponInstance != null) {
			EntityShellCasing entityShellCasing = weapon.builder.spawnShellWith.apply(playerWeaponInstance, player);
			if (entityShellCasing != null) {
				CompatibilityProvider.compatibility.spawnEntity(player, entityShellCasing);
			}
		}

		CompatibleSound shootSound = null;

		boolean silencerOn = (playerWeaponInstance != null
				&& this.modContext.getAttachmentAspect().isSilencerOn(playerWeaponInstance));
		if (isBurst && weapon.builder.isOneClickBurstAllowed) {

			CompatibleSound burstShootSound = null;
			if (silencerOn) {
				burstShootSound = weapon.getSilencedBurstShootSound();
			}
			if (burstShootSound == null) {
				burstShootSound = weapon.getBurstShootSound();
			}
			if (burstShootSound != null) {
				shootSound = burstShootSound;
			} else {
				shootSound = silencerOn ? weapon.getSilencedShootSound() : weapon.getShootSound();
			}
		} else {
			shootSound = silencerOn ? weapon.getSilencedShootSound() : weapon.getShootSound();
		}

		CompatibilityProvider.compatibility.playSoundToNearExcept(player, shootSound,
				silencerOn ? weapon.getSilencedShootSoundVolume() : weapon.getShootSoundVolume(), 1.0F);
	}
}

/*
 * Location:
 * I:\BON\mwmodify-deobf.jar!\com\vicmatskiv\weaponlib\WeaponFireAspect.class
 * Java compiler version: 8 (52.0) JD-Core Version: 1.1.3
 */