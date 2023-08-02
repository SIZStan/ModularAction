package com.vicmatskiv.weaponlib;

import com.landb.mwoverhaul.util.MinecraftUtils;
import com.landb.mwoverhaul.util.MultiReflection;
import com.landb.mwoverhaul.world.data.NBTTypes;
import com.vicmatskiv.mw.ModernWarfareMod;
import com.vicmatskiv.weaponlib.AttachmentCategory;
import com.vicmatskiv.weaponlib.ItemAttachment;
import com.vicmatskiv.weaponlib.ItemScope;
import com.vicmatskiv.weaponlib.PlayerItemInstance;
import com.vicmatskiv.weaponlib.PlayerWeaponInstance;
import com.vicmatskiv.weaponlib.Tags;
import com.vicmatskiv.weaponlib.Weapon;
import com.vicmatskiv.weaponlib.compatibility.CompatibleExtraEntityFlags;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import indi.ma.client.ClientHandler;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;

public class HookUtils {
	private static Map weaponRecoilValues;
	private static Field updateIdField;
	private static Field modeField;
	private static byte latestRFPRMode = -1;
	public static float currentRecoilModifier = 1.0F;

	public static boolean isLaserActive() {
		PlayerWeaponInstance context = ModernWarfareMod.MOD_CONTEXT.getMainHeldWeapon();
		if (context == null) {
			return false;
		} else {
			ItemAttachment attach = context.getAttachmentItemWithCategory(AttachmentCategory.LASER);
			return attach == null ? false : context.isLaserOn();
		}
	}

	public static void removeProneFlag(EntityLivingBase entity) {
		if (isEntityProne(entity)) {
			CompatibleExtraEntityFlags.setFlag(entity, CompatibleExtraEntityFlags.PRONING, false);
		}

	}

	public static boolean isEntityProne(EntityLivingBase entity) {
		if (entity instanceof EntityPlayer) {
			int flags = CompatibleExtraEntityFlags.getFlags(entity);
			if ((flags & CompatibleExtraEntityFlags.PRONING) != 0) {
				return true;
			}
		}

		return false;
	}

	@SideOnly(Side.CLIENT)
	public static Vec3 getCompatibleVec(EntityLivingBase base, float p_70666_1_) {
		float correction = 0.0F;
		if (base == MinecraftUtils.getMyPlayer() && isEntityProne(base)) {
			correction += 0.2F;
		}

		if (p_70666_1_ == 1.0F) {
			return Vec3.createVectorHelper(base.posX, base.posY - (double) base.height + (double) correction,
					base.posZ);
		} else {
			double d0 = base.prevPosX + (base.posX - base.prevPosX) * (double) p_70666_1_;
			double d1 = base.prevPosY + (base.posY - base.prevPosY) * (double) p_70666_1_;
			double d2 = base.prevPosZ + (base.posZ - base.prevPosZ) * (double) p_70666_1_;

			 //probe correct
			if (base.worldObj.isRemote) //client
			{
				Vec3 vec3 = Vec3.createVectorHelper(ClientHandler.cameraProbeOffset * -0.6, 0.0, 0.0);
				vec3.rotateAroundY(((float) (((double) (-base.rotationYaw)) * 3.141593 / 180.0)));
//  	       EntityPlayer v1 = event.getWeaponUser();
//  	       v1.posX += vec3d.x;
//  	       EntityPlayer v1_1 = event.getWeaponUser();
//  	       v1_1.posZ += vec3d.z;
				d0 += vec3.xCoord;
				d2 += vec3.zCoord;
			} else {
				
			}

			return Vec3.createVectorHelper(d0, d1 + (double) (base.height - 1.8F) + (double) correction, d2);
		}
	}

	@SideOnly(Side.CLIENT)
	private static Field getRFPRfield() {
		try {
			if (modeField == null) {
				Class<?> c = Class.forName("realrender.REN");
				modeField = c.getDeclaredField("currentMode");
				modeField.setAccessible(true);
			}

			return modeField;
		} catch (Exception var1) {
			return null;
		}
	}

	@SideOnly(Side.CLIENT)
	public static boolean isUsingRFPR() {
		return getRFPRfield() != null;
	}

	@SideOnly(Side.CLIENT)
	public static void swapRPFRMode(boolean isAiming) {
		try {
			byte currentMode = getRFPRfield().getByte((Object) null);
			if (isAiming) {
				latestRFPRMode = currentMode;
				getRFPRfield().setByte((Object) null, (byte) 2);
			} else {
				getRFPRfield().setByte((Object) null, latestRFPRMode);
			}
		} catch (Exception var2) {
			var2.printStackTrace();
		}

	}

	public static boolean canHaveCrosshair(PlayerWeaponInstance instance) {
		return instance != null && (weaponHasMoreThanSemiMode(instance.getWeapon())
				|| instance.getWeapon().getSpawnEntityDamage() < 20.0F);
	}

	public static boolean isHoldingAnyGun() {
		return ModernWarfareMod.MOD_CONTEXT.getMainHeldWeapon() != null;
	}

	public static boolean hasRealAttachmentType(EntityPlayer player, AttachmentCategory category) {
		if (player == null) {
			return false;
		} else {
			ItemStack stack = player.getHeldItem();
			if (stack == null) {
				return false;
			} else if (!(stack.getItem() instanceof Weapon)) {
				return false;
			} else {
				Weapon weapon = (Weapon) stack.getItem();
				PlayerItemInstance itemInstance = Tags.getInstance(player.getHeldItem());
				if (!(itemInstance instanceof PlayerWeaponInstance)) {
					return false;
				} else {
					PlayerWeaponInstance instance = (PlayerWeaponInstance) itemInstance;

					for (int id : instance.getActiveAttachmentIds()) {
						Item item = Item.getItemById(id);
						if (item != null && item instanceof ItemAttachment) {
							ItemAttachment attachment = (ItemAttachment) item;
							if (attachment.getCategory() == category) {
								System.out.println(attachment);
								if (category == AttachmentCategory.SCOPE && !(attachment instanceof ItemScope)) {
									return false;
								}

								return true;
							}
						}
					}

					return false;
				}
			}
		}
	}

	public static boolean hasAttachmentType(EntityPlayer player, AttachmentCategory category) {
		if (player == null) {
			return false;
		} else {
			ItemStack stack = player.getHeldItem();
			if (stack == null) {
				return false;
			} else if (!(stack.getItem() instanceof Weapon)) {
				return false;
			} else {
				Weapon weapon = (Weapon) stack.getItem();
				PlayerItemInstance itemInstance = Tags.getInstance(player.getHeldItem());
				if (!(itemInstance instanceof PlayerWeaponInstance)) {
					return false;
				} else {
					PlayerWeaponInstance instance = (PlayerWeaponInstance) itemInstance;
					return hasAttachmentType(instance, category);
				}
			}
		}
	}

	public static boolean hasAttachmentType(PlayerWeaponInstance instance, AttachmentCategory category) {
		for (int id : instance.getActiveAttachmentIds()) {
			Item item = Item.getItemById(id);
			if (item != null && item instanceof ItemAttachment) {
				ItemAttachment attachment = (ItemAttachment) item;
				if (attachment.getCategory() == category) {
					if (category == AttachmentCategory.SCOPE) {
						if (!(attachment instanceof ItemScope)) {
							return false;
						}

						if (attachment instanceof ItemScope && !((ItemScope) attachment).isOptical()) {
							return false;
						}
					}

					return true;
				}
			}
		}

		return false;
	}

	public static List getCompatibleWeapons(ItemAttachment attachment) {
		try {
			Field f = ItemAttachment.class.getDeclaredField("compatibleWeapons");
			f.setAccessible(true);
			return (List) f.get(attachment);
		} catch (Exception var2) {
			throw new RuntimeException(var2);
		}
	}

	public static boolean isAiming() {
		PlayerWeaponInstance context = ModernWarfareMod.MOD_CONTEXT.getMainHeldWeapon();
		return context == null ? false : context.isAimed();
	}

	public static int getPelletsAmount(Weapon weapon) {
		return weapon.builder.pellets;
	}

	public static boolean isShotgun(Weapon weapon) {
		return weapon.builder.pellets > 1;
	}

	public static boolean isSilencerOn(PlayerWeaponInstance instance) {
		return ModernWarfareMod.MOD_CONTEXT.getAttachmentAspect().isSilencerOn(instance);
	}

	public static boolean weaponHasMoreThanSemiMode(Weapon weapon) {
		Iterator var1 = weapon.builder.maxShots.iterator();

		while (var1.hasNext()) {
			int i = ((Integer) var1.next()).intValue();
			if (i > 1) {
				return true;
			}
		}

		return false;
	}

	public static void recalculateWeaponValuesBasedOnAttachments(PlayerWeaponInstance instance) {
		try {
			if (updateIdField == null) {
				updateIdField = MultiReflection.getDeclaredField(PlayerItemInstance.class, new String[] { "updateId" });
				updateIdField.setAccessible(true);
			}

			long beforeId = updateIdField.getLong(instance);
			instance.setRecoil(instance.getWeapon().builder.recoil);
			ItemAttachment attachment = instance.getAttachmentItemWithCategory(AttachmentCategory.GRIP);
			if (attachment != null && attachment.getApply2() != null) {
				attachment.getApply2().apply(attachment, instance);
			}

			updateIdField.setLong(instance, beforeId);
		} catch (Exception var4) {
			var4.printStackTrace();
		}

	}

	public static float getWeaponFirstRecoilCache(Weapon weapon) {
		if (weaponRecoilValues == null) {
			throw new IllegalArgumentException("武器后座缓存未设置！");
		} else {
			return ((Float) weaponRecoilValues.get(weapon)).floatValue();
		}
	}

	public static void saveWeaponsFirstRecoilCache() {
		weaponRecoilValues = new HashMap();
		Item.itemRegistry.forEach((i) -> {
			if (i instanceof Weapon) {
				Weapon castedWeapon = (Weapon) i;
				weaponRecoilValues.put(castedWeapon, Float.valueOf(castedWeapon.builder.recoil));
			}
		});
	}

	@SideOnly(Side.CLIENT)
	public static void updateRecoil() {
		float worldRecoil;
		if (MinecraftUtils.getMyWorld() == null) {
			worldRecoil = ((Float) NBTTypes.RECOIL_MODIFIER.getInitialValue()).floatValue();
		} else {
			worldRecoil = ((Float) NBTTypes.RECOIL_MODIFIER.getFromMyWorld()).floatValue();
		}

		if (currentRecoilModifier != worldRecoil) {
			MinecraftUtils.addChatMessage(
					String.format("§7[§cMineMc§7] §f服务器已将后座修改器设置为 %.3fx", new Object[] { Float.valueOf(worldRecoil) }));

			try {
				Item.itemRegistry.forEach((i) -> {
					if (i instanceof Weapon) {
						Weapon castedWeapon = (Weapon) i;
						castedWeapon.builder.recoil = getWeaponFirstRecoilCache(castedWeapon) * worldRecoil;
					}
				});
			} catch (Exception var2) {
				throw new RuntimeException("更改后座信息时出错!");
			}

			currentRecoilModifier = worldRecoil;
		}

	}

	public static float getFirerate(Weapon weapon) {
		return weapon.builder.fireRate;
	}

	public static float getTrueFireDelay(Weapon weapon) {
		return 50.0F / getFirerate(weapon);
	}

	public static void setItemMaxStackSize(ItemAttachment item, int stackSize) {
		item.setMaxStackSize(stackSize);
		item.maxStackSize = stackSize;
	}

	public static int getAmmo(ItemStack stack) {
		return Tags.getAmmo(stack);
	}

	public static void setAmmo(ItemStack stack, int ammo) {
		Tags.setAmmo(stack, ammo);
	}
}
