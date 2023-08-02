package indi.ma.client;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.landb.mwoverhaul.util.LanguageDumper;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import indi.ma.ModContainer;
import indi.ma.common.PlayerState;
import indi.ma.network.TacticalHandler;
import indi.ma.server.ServerListener;
import indi.ma.util.BaiscMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.MovementInput;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
//(?=/\*)(.*)(?<=\*/)
public class ClientHandler {
	public static KeyBinding sit = new KeyBinding("key.sit", 46, "key.categories.modularaction");//Sit/Sliding
	public static KeyBinding crawling = new KeyBinding("key.crawl", 44, "key.categories.modularaction");//Crawling
	public static KeyBinding leftProbe = new KeyBinding("key.leftprobe", 16, "key.categories.modularaction");//Left Probe
	public static KeyBinding rightProbe = new KeyBinding("key.rightprobe", 18, "key.categories.modularaction");//Right Probe

	public static PlayerState clientPlayerState = (PlayerState) new PlayerState.ClientPlayerState();
	public static Map<Integer, PlayerState> ohterPlayerStateMap = new HashMap<>();

	public static Vec3 clientPlayerSitMoveVec3d = Vec3.createVectorHelper(0.0D, 0.0D, 0.0D);
	public static double clientPlayerSitMoveAmplifierCharging = 0.0D;
	public static double clientPlayerSitMoveAmplifierCharged = 0.0D;
	public static double clientPlayerSitMoveAmplifier = 0.0D;

	public static boolean enableForceGravity = false;
	public static double forceGravity = 1.0D;
	public static double clientPlayerSitMoveAmplifierUser = 0.8D;
	public static double clientPlayerSitMoveLess = 0.1D;
	public static double clientPlayerSitMoveMax = 1.0D;

	private static World world;
	private static boolean sitKeyLock = false;
	private static boolean crawlingKeyLock = false;
	private static boolean probeKeyLock = false;
	private static boolean isSneaking = false;
	private static boolean wannaSliding = false;
	private static long lastSyncTime = 0L;
	public static int crawlingMousePosXMove = 0;

	public static double cameraOffsetY = 0.0D;
	public static float cameraProbeOffset = 0.0F;

	private static Field speedInAir;

	@SubscribeEvent
	public void onTickClient(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			return;
		}
		//debug
//		if (clientPlayerState.probe != 1) {
//			clientPlayerState.rightProbe();
//		}
		if ((Minecraft.getMinecraft()).theWorld != world) {
			clientPlayerSitMoveAmplifier = 0.0D;
			cameraOffsetY = 0.0D;
			cameraProbeOffset = 0.0F;
			clientPlayerSitMoveAmplifierCharging = 0.0D;
			clientPlayerSitMoveAmplifierCharged = 0.0D;
			clientPlayerState.reset();
			if ((Minecraft.getMinecraft()).thePlayer != null) {
				// (Minecraft.getMinecraft()).thePlayer.setPrimaryHand((Minecraft.getMinecraft()).gameSettings.mainHand);
			}
		}
		if ((Minecraft.getMinecraft()).thePlayer != null) {
			if ((Minecraft.getMinecraft()).thePlayer.isSprinting() && !clientPlayerState.isSitting) {
				clientPlayerSitMoveAmplifierCharging += 0.05D;
				clientPlayerSitMoveAmplifierCharging = Math.min(clientPlayerSitMoveAmplifierCharging, 1.0D);
			} else {
				clientPlayerSitMoveAmplifierCharging = 0.0D;
			}
		}
		world = (World) (Minecraft.getMinecraft()).theWorld;
	}
	public static void initKeys() {	    
		ClientRegistry.registerKeyBinding(sit);
		ClientRegistry.registerKeyBinding(crawling);
		ClientRegistry.registerKeyBinding(leftProbe);
		ClientRegistry.registerKeyBinding(rightProbe);
	}

	public void onFMLInit(FMLInitializationEvent event) {
		speedInAir = ReflectionHelper.findField(EntityPlayer.class, "speedInAir", "field_71102_ce");// Warining
	}

	private static boolean isButtonDown(int id) {
		try {
			if (id < 0) {
				return Mouse.isButtonDown(id + 100);
			}
			return Keyboard.isKeyDown(id);
		} catch (IndexOutOfBoundsException indexOutOfBoundsException) {

			return false;
		}
	}

	private void onSit() {
		EntityPlayerSP entityPlayerSP = (Minecraft.getMinecraft()).thePlayer;
		if (entityPlayerSP == null) {
			return;
		}
		if ((Minecraft.getMinecraft()).currentScreen != null) {
			return;
		}
		boolean sprinting=Minecraft.getMinecraft().thePlayer.isSprinting();
		boolean canSlide=!clientPlayerState.isSitting&&clientPlayerState.canCharging()&&sprinting && ModContainer.CONFIG.slide.enable;
		if ((!sprinting&&clientPlayerState.canSit() && ((!sitKeyLock && isButtonDown(sit.getKeyCode())) || wannaSliding))||(canSlide&&isButtonDown(sit.getKeyCode()))) {
			if (!clientPlayerState.isSitting || wannaSliding) {
				if ((Minecraft.getMinecraft()).thePlayer.onGround) {
					sitKeyLock = true;
					wannaSliding = false;

					//1122 origin
//					AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(
//							((EntityPlayer) entityPlayerSP).posX - 0.1D, ((EntityPlayer) entityPlayerSP).posY + 0.1D,
//							((EntityPlayer) entityPlayerSP).posZ - 0.1D, ((EntityPlayer) entityPlayerSP).posX + 0.1D,
//							((EntityPlayer) entityPlayerSP).posY + 1.2D, ((EntityPlayer) entityPlayerSP).posZ + 0.1D);
					AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(
							((EntityPlayer) entityPlayerSP).posX - 0.1D,
							((EntityPlayer) entityPlayerSP).posY -((EntityPlayer) entityPlayerSP).yOffset+ 0.1D,
							((EntityPlayer) entityPlayerSP).posZ - 0.1D,
							((EntityPlayer) entityPlayerSP).posX + 0.1D,
							((EntityPlayer) entityPlayerSP).posY -((EntityPlayer) entityPlayerSP).yOffset+ 1.2D,
							((EntityPlayer) entityPlayerSP).posZ + 0.1D);
					if (((EntityPlayer) entityPlayerSP).worldObj.func_147461_a(axisalignedbb).isEmpty()) {
						if (!clientPlayerState.isSitting) {
							clientPlayerState.enableSit();
						}

						if (clientPlayerState.isSitting&&sprinting&&canSlide && ModContainer.CONFIG.slide.enable) {
							//System.out.println("Slide!");
							if (wannaSliding) {
								clientPlayerSitMoveAmplifierCharging = 1.0D;
							}
							clientPlayerSitMoveAmplifierCharged = clientPlayerSitMoveAmplifierCharging;
							clientPlayerSitMoveAmplifier = ModContainer.CONFIG.slide.maxForce;

							clientPlayerSitMoveVec3d = (Vec3.createVectorHelper(
									((EntityPlayer) entityPlayerSP).posX - ((EntityPlayer) entityPlayerSP).lastTickPosX,
									0.0D, ((EntityPlayer) entityPlayerSP).posZ
											- ((EntityPlayer) entityPlayerSP).lastTickPosZ)).normalize();
						}
					}
				}
			} else {
				sitKeyLock = true;
				double d0 = 0.3D;
				//1122 Origin
//				if (((EntityPlayer) entityPlayerSP).worldObj
//						.func_147461_a(AxisAlignedBB.getBoundingBox(((EntityPlayer) entityPlayerSP).posX - d0,
//								((EntityPlayer) entityPlayerSP).posY, ((EntityPlayer) entityPlayerSP).posZ - d0,
//								((EntityPlayer) entityPlayerSP).posX + d0, ((EntityPlayer) entityPlayerSP).posY + 1.8D,
//								((EntityPlayer) entityPlayerSP).posZ + d0))
//						.isEmpty()) 
				if (((EntityPlayer) entityPlayerSP).worldObj
						.func_147461_a(AxisAlignedBB.getBoundingBox(
								((EntityPlayer) entityPlayerSP).posX - d0,
								((EntityPlayer) entityPlayerSP).posY-((EntityPlayer) entityPlayerSP).yOffset, 
								((EntityPlayer) entityPlayerSP).posZ - d0,
								((EntityPlayer) entityPlayerSP).posX + d0, 
								((EntityPlayer) entityPlayerSP).posY +0.2D,
								((EntityPlayer) entityPlayerSP).posZ + d0))
						.isEmpty()){

					clientPlayerSitMoveAmplifier = 0.0D;
					clientPlayerState.disableSit();
				}
			}
		}
	}

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		EntityLivingBase elb;
		EntityPlayerSP entityPlayerSP = (Minecraft.getMinecraft()).thePlayer;
		onSit();
		boolean sprinting=Minecraft.getMinecraft().thePlayer.isSprinting();
		boolean canCharge=(sprinting&&clientPlayerState.canCharging()&&!clientPlayerState.isCrawling);
		if (((!sprinting&&clientPlayerState.canCrawl())||canCharge) 
				&& !crawlingKeyLock && isButtonDown(crawling.getKeyCode())) {
			crawlingKeyLock = true;
			if (!clientPlayerState.isCrawling) {
				if ((Minecraft.getMinecraft()).thePlayer.onGround) {
					clientPlayerState.resetProbe();
					cameraProbeOffset = 0.0F;
					clientPlayerState.enableCrawling();
					if (sprinting&&canCharge) {
						Vec3 vec3d = (Vec3.createVectorHelper(
								((EntityPlayer) entityPlayerSP).posX - ((EntityPlayer) entityPlayerSP).lastTickPosX,
								0.0D,
								((EntityPlayer) entityPlayerSP).posZ - ((EntityPlayer) entityPlayerSP).lastTickPosZ))
										.normalize();
						(Minecraft.getMinecraft()).thePlayer.motionX = vec3d.xCoord
								* clientPlayerSitMoveAmplifierCharging;
						(Minecraft.getMinecraft()).thePlayer.motionY = 0.2D * clientPlayerSitMoveAmplifierCharging;
						(Minecraft.getMinecraft()).thePlayer.motionZ = vec3d.zCoord
								* clientPlayerSitMoveAmplifierCharging;
					}
				}
			} else{
				double d0 = 0.3D;
				if (((EntityPlayer) entityPlayerSP).worldObj
						.func_147461_a(AxisAlignedBB.getBoundingBox(
								((EntityPlayer) entityPlayerSP).posX - d0,
								((EntityPlayer) entityPlayerSP).posY-((EntityPlayer) entityPlayerSP).yOffset,
								((EntityPlayer) entityPlayerSP).posZ - d0,
								((EntityPlayer) entityPlayerSP).posX + d0,
								((EntityPlayer) entityPlayerSP).posY +0.2D,//1122 origin + 1.8D
								((EntityPlayer) entityPlayerSP).posZ + d0))
						.isEmpty()) {

					clientPlayerState.disableCrawling();
				}
			}
		}

		if (ModContainer.CONFIG.lean.withGunsOnly) {
			if (Loader.isModLoaded("mw")) {
				if (!((Minecraft.getMinecraft()).thePlayer.getHeldItem()
						.getItem() instanceof com.vicmatskiv.weaponlib.Weapon)) {
					return;
				} else {
					return;
				}
			} else {
				return;
			}
		}
		if (clientPlayerState.canProbe()) {
			if (!probeKeyLock && isButtonDown(leftProbe.getKeyCode())) {
				probeKeyLock = true;
				if (clientPlayerState.probe != -1) {
					clientPlayerState.leftProbe();
				} else {
					clientPlayerState.resetProbe();
				}
			}

			if (!probeKeyLock && isButtonDown(rightProbe.getKeyCode())) {
				probeKeyLock = true;
				if (clientPlayerState.probe != 1) {
					clientPlayerState.rightProbe();
				} else {
					clientPlayerState.resetProbe();
				}
			}
		}
	}

	public static boolean applyRotations(EntityLivingBase entityLiving, float rotationYaw) {
		float partialTicks = 0;
		if (entityLiving == (Minecraft.getMinecraft()).thePlayer && entityLiving.isEntityAlive()) {
			if (clientPlayerState.isSitting) {
				GL11.glTranslated(0.0D, -0.5D, 0.0D);
			}
			if (clientPlayerState.isCrawling) {
				GL11.glRotatef(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
				GL11.glTranslated(0.0D, -1.3D, 0.1D);
				GL11.glTranslated(cameraProbeOffset * 0.4D, 0.0D, 0.0D);
				return true;
			}
			if (cameraProbeOffset != 0.0F) {
				GL11.glRotatef(180.0F - entityLiving.rotationYawHead, 0.0F, 1.0F, 0.0F);
				GL11.glTranslated(cameraProbeOffset * 0.1D, 0.0D, 0.0D);
				GL11.glRotatef(180.0F - entityLiving.rotationYawHead, 0.0F, -1.0F, 0.0F);
				GL11.glRotatef(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(cameraProbeOffset * -20.0F, 0.0F, 0.0F, 1.0F);
				return true;
			}
		}
		if (entityLiving != (Minecraft.getMinecraft()).thePlayer && entityLiving instanceof EntityPlayer
				&& entityLiving.isEntityAlive()
				&& ohterPlayerStateMap.containsKey(Integer.valueOf(entityLiving.getEntityId()))) {
			PlayerState state = ohterPlayerStateMap.get(Integer.valueOf(entityLiving.getEntityId()));
			if (state.isSitting) {
				GL11.glTranslated(0.0D, -0.5D, 0.0D);
			}
			if (state.isCrawling) {
				GL11.glRotatef(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
				GL11.glTranslated(0.0D, -1.3D, 0.1D);
				GL11.glTranslated(state.probeOffset * 0.4D, 0.0D, 0.0D);
				return true;
			}
			state.updateOffset();
			if (state.probeOffset != 0.0F) {
				GL11.glRotatef(180.0F - entityLiving.rotationYawHead, 0.0F, 1.0F, 0.0F);
				GL11.glTranslated(state.probeOffset * 0.1D, 0.0D, 0.0D);
				GL11.glRotatef(180.0F - entityLiving.rotationYawHead, 0.0F, -1.0F, 0.0F);
				GL11.glRotatef(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(state.probeOffset * -20.0F, 0.0F, 0.0F, 1.0F);
				return true;
			}
		}
		return false;
	}

	public static void onMouseMove(MouseHelper mouseHelper) {
		if (clientPlayerState.probe != 0 && ModContainer.CONFIG.lean.mouseCorrection) {
			Vec3 vec = Vec3.createVectorHelper(0.0D, 0.0D, 0.0D).addVector(mouseHelper.deltaX, 0.0D,
					mouseHelper.deltaY);
			vec.rotateAroundY((float) ((cameraProbeOffset * 10.0F) * Math.PI / 180.0D));
			mouseHelper.deltaX = Math.round((float) vec.xCoord);
			mouseHelper.deltaY = Math.round((float) vec.zCoord);
		}
		if (clientPlayerState.isCrawling && ModContainer.CONFIG.crawl.blockView) {
			float angle = (float) (ModContainer.CONFIG.crawl.blockAngle * Math.PI);
			if (Math.abs(crawlingMousePosXMove + mouseHelper.deltaX) > angle) {
				if (mouseHelper.deltaX > 0) {
					mouseHelper.deltaX = (int) (angle - crawlingMousePosXMove);
				} else {
					mouseHelper.deltaX = (int) (-angle - crawlingMousePosXMove);
				}
			}
			crawlingMousePosXMove += mouseHelper.deltaX;
		}
	}

	public static void setRotationAngles(ModelBiped model, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		setRotationAngles((ModelBase) model, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor,
				entityIn);
//	     ModelPlayer.copyModelAngles(model.bipedLeftLeg, model.bipedLeftLegwear);
//	     ModelPlayer.copyModelAngles(model.bipedRightLeg, model.bipedRightLegwear);
//	     ModelPlayer.copyModelAngles(model.bipedLeftArm, model.bipedLeftArmwear);
//	     ModelPlayer.copyModelAngles(model.bipedRightArm, model.bipedRightArmwear);
//	     ModelPlayer.copyModelAngles(model.bipedBody, model.bipedBodyWear);
		FakePlayerModel.copyModelAngles(model.bipedHead, model.bipedHeadwear);
	}

	public static void setRotationAngles(ModelBase modelBase, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		ModelBiped model = (ModelBiped) modelBase;
		if (entityIn instanceof EntityPlayer && entityIn.isEntityAlive()) {
			PlayerState state = null;
			float offest = 0.0F;
			// set leg to Default angle
			model.bipedRightLeg.rotateAngleZ = 0.0F;
			model.bipedLeftLeg.rotateAngleZ = 0.0F;
			if (entityIn == (Minecraft.getMinecraft()).thePlayer) {
				state = clientPlayerState;
				offest = cameraProbeOffset;
			} else {
				if (!ohterPlayerStateMap.containsKey(Integer.valueOf(entityIn.getEntityId()))) {
					return;
				}
				state = ohterPlayerStateMap.get(Integer.valueOf(entityIn.getEntityId()));
				offest = state.probeOffset;
			}

			if (state.isSitting) {
				model.bipedRightLeg.rotateAngleX = -1.4137167F;
				model.bipedRightLeg.rotateAngleY = 0.31415927F;
				model.bipedRightLeg.rotateAngleZ = 0.07853982F;
				model.bipedLeftLeg.rotateAngleX = -1.4137167F;
				model.bipedLeftLeg.rotateAngleY = -0.31415927F;
				model.bipedLeftLeg.rotateAngleZ = -0.07853982F;
			}

			if (state.isCrawling) {
				model.bipedHead.rotateAngleX = (float) (model.bipedHead.rotateAngleX - 1.2211111111111113D);
				model.bipedRightArm.rotateAngleX = (float) (model.bipedRightArm.rotateAngleX * 0.2D);
				model.bipedLeftArm.rotateAngleX = (float) (model.bipedLeftArm.rotateAngleX * 0.2D);
				model.bipedRightArm.rotateAngleX = (float) (model.bipedRightArm.rotateAngleX + 3.14D);
				model.bipedLeftArm.rotateAngleX = (float) (model.bipedLeftArm.rotateAngleX + 3.14D);
				if (entityIn instanceof AbstractClientPlayer) {
					ItemStack itemstack = ((AbstractClientPlayer) entityIn).getHeldItem();
					if (itemstack != null && ModContainer.mwfEnable
							&& itemstack.getItem() instanceof com.vicmatskiv.weaponlib.Weapon) {
						model.bipedLeftArm.rotateAngleY = 0.0F;
						model.bipedRightArm.rotateAngleY = 0.0F;
						model.bipedLeftArm.rotateAngleX = 3.14F;
						model.bipedRightArm.rotateAngleX = 3.14F;
					}
				}

				model.bipedRightLeg.rotateAngleX = (float) (model.bipedRightLeg.rotateAngleX * 0.2D);
				model.bipedLeftLeg.rotateAngleX = (float) (model.bipedLeftLeg.rotateAngleX * 0.2D);
			}
			if (offest >= 0.0F) {
				model.bipedRightLeg.rotateAngleZ = (float) (model.bipedRightLeg.rotateAngleZ
						+ (offest * 20.0F) * 3.14D / 180.0D);
			} else {
				model.bipedLeftLeg.rotateAngleZ = (float) (model.bipedLeftLeg.rotateAngleZ
						+ (offest * 20.0F) * 3.14D / 180.0D);
			}
		}
	}
	private static float eyeHeight=1.62f;
	private float getEyeHeight() {
		return eyeHeight;
	}
	private static float getDefaultEyeHeight()
	{
		return 1.62f;
	}

	// EntityRenderer//orientCamera
	static public boolean onCameraUpdate(EntityLivingBase entitylivingbase, float partialTicks) {
		//System.out.println("onCameraUpdate!!");
		float yaw = entitylivingbase.prevRotationYaw+ (entitylivingbase.rotationYaw - entitylivingbase.prevRotationYaw) * partialTicks + 180.0F;
		float pitch = entitylivingbase.prevRotationPitch+ (entitylivingbase.rotationPitch - entitylivingbase.prevRotationPitch) * partialTicks;
		float roll = 0.0F;
		if (clientPlayerSitMoveAmplifier > 0.0D && Minecraft.getMinecraft().renderViewEntity instanceof EntityPlayer) {
			EntityPlayer entityplayer = (EntityPlayer) Minecraft.getMinecraft().renderViewEntity;
			float f = entityplayer.distanceWalkedModified - entityplayer.prevDistanceWalkedModified;
			float f1 = -(entityplayer.distanceWalkedModified + f * partialTicks);
			float f2 = entityplayer.prevCameraYaw
					+ (entityplayer.cameraYaw - entityplayer.prevCameraYaw) * partialTicks;

			float f3 = entityplayer.prevCameraPitch
					+ (entityplayer.cameraPitch - entityplayer.prevCameraPitch) * partialTicks;

			GL11.glRotatef(f3, -1.0F, 0.0F, 0.0F);
			GL11.glRotatef(Math.abs(MathHelper.cos(f1 * 3.1415927F - 0.2F) * f2) * 5.0F, -1.0F, 0.0F, 0.0F);

			GL11.glRotatef(MathHelper.sin(f1 * 3.1415927F) * f2 * 3.0F, 0.0F, 0.0F, -1.0F);
			GL11.glTranslated(-MathHelper.sin(f1 * 3.1415927F) * f2 * 0.5F,
					Math.abs(MathHelper.cos(f1 * 3.1415927F) * f2), 0.0F);
		}

//	     float pitch = event.getPitch();
//	     float yaw = event.getYaw();
		// set to ZERO means cancel the default rotation
//	     event.setPitch(0.0F);
//	     event.setYaw(0.0F);
//	     event.setRoll(0.0F);
		GL11.glRotatef(10.0F * cameraProbeOffset, 0.0F, 0.0F, 1.0F);
		GL11.glRotatef(pitch, 1.0F, 0.0F, 0.0F);
		//GL11.glTranslated(0.0D, -cameraOffsetY, 0.0D);
		GL11.glTranslated(-0.6D * cameraProbeOffset, 0.0D, 0.0D);
		GL11.glRotatef(yaw, 0.0F, 1.0F, 0.0F);

		if (clientPlayerState.probe != 0) {
			float f = 0.2F;
			float f1 = 0.2F;
			boolean isS=clientPlayerState.isSitting;
			float eh=1.62f;
			if(isS)
				eh=1.1f;
			if(clientPlayerState.isCrawling)
				eh=0.4f;
			Vec3 vec3d = null;
			if (clientPlayerState.probe == -1) {
				vec3d = Vec3.createVectorHelper(0.6D, 0.0D, 0.0D);
				vec3d.rotateAroundY((float) (-Minecraft.getMinecraft().thePlayer.rotationYaw * Math.PI / 180.0D));
			}
			if (clientPlayerState.probe == 1) {
				vec3d = Vec3.createVectorHelper(-0.6D, 0.0D, 0.0D);
				vec3d.rotateAroundY((float) (-Minecraft.getMinecraft().thePlayer.rotationYaw * Math.PI / 180.0D));
			}
			AxisAlignedBB axisalignedbb = (Minecraft.getMinecraft()).thePlayer.boundingBox;
			axisalignedbb = AxisAlignedBB.getBoundingBox(
					(Minecraft.getMinecraft()).thePlayer.posX + vec3d.xCoord - f,
					(Minecraft.getMinecraft()).thePlayer.posY-(Minecraft.getMinecraft()).thePlayer.yOffset+eh-f1 ,
					(Minecraft.getMinecraft()).thePlayer.posZ + vec3d.zCoord - f,
					(Minecraft.getMinecraft()).thePlayer.posX + vec3d.xCoord + f,
					(Minecraft.getMinecraft()).thePlayer.posY -(Minecraft.getMinecraft()).thePlayer.yOffset+ eh+f1,
					(Minecraft.getMinecraft()).thePlayer.posZ + vec3d.zCoord + f);
			if (!(Minecraft.getMinecraft()).thePlayer.worldObj.func_147461_a(axisalignedbb).isEmpty()) {
				clientPlayerState.resetProbe();
				cameraProbeOffset = 0.0F;
			}
			//System.out.println("aabb finished!!");
		}

		EntityPlayerSP entityPlayerSP = (Minecraft.getMinecraft()).thePlayer;
		if (clientPlayerState.isSitting) {
			if (eyeHeight != 1.1F) {
				cameraOffsetY = (eyeHeight - 1.1F);
				eyeHeight = 1.1F;
				//entityPlayerSP.yOffset=1.1F;
			}
		} else if (clientPlayerState.isCrawling) {
			if (eyeHeight != 0.4F) {
				cameraOffsetY = (eyeHeight - 0.4F);
				eyeHeight = 0.4F;
				//entityPlayerSP.eyeHeight=0.4F;
				entityPlayerSP.eyeHeight=-1.22F;
				//entityPlayerSP.yOffset=0.4F;
			}
		} else if (eyeHeight == 0.4F) {
			cameraOffsetY = (eyeHeight - getDefaultEyeHeight());
			eyeHeight = getDefaultEyeHeight();
			entityPlayerSP.eyeHeight=entityPlayerSP.getDefaultEyeHeight();
			//entityPlayerSP.eyeHeight=0.12f;
			entityPlayerSP.yOffset=1.62f;
		} else if (eyeHeight == 1.1F) {
			cameraOffsetY = (eyeHeight - getDefaultEyeHeight());
			eyeHeight = getDefaultEyeHeight();
			entityPlayerSP.eyeHeight=entityPlayerSP.getDefaultEyeHeight();
			//entityPlayerSP.eyeHeight=0.12f;
			entityPlayerSP.yOffset=1.62f;
		}

		double amplifer = (Minecraft.getSystemTime() - lastSyncTime) * 0.06D;
		lastSyncTime = Minecraft.getSystemTime();
		if (clientPlayerState.probe == -1) {
			if (cameraProbeOffset > -1.0F) {
				cameraProbeOffset = (float) (cameraProbeOffset - 0.1D * amplifer);
			}
			if (cameraProbeOffset < -1.0F) {
				cameraProbeOffset = -1.0F;
			}
		}
		if (clientPlayerState.probe == 1) {
			if (cameraProbeOffset < 1.0F) {
				cameraProbeOffset = (float) (cameraProbeOffset + 0.1D * amplifer);
			}
			if (cameraProbeOffset > 1.0F) {
				cameraProbeOffset = 1.0F;
			}
		}

		if (clientPlayerState.probe == 0) {
			if (Math.abs(cameraProbeOffset) <= 0.1D * amplifer) {
				cameraProbeOffset = 0.0F;
			}
			if (cameraProbeOffset < 0.0F) {
				cameraProbeOffset = (float) (cameraProbeOffset + 0.1D * amplifer);
			}
			if (cameraProbeOffset > 0.0F) {
				cameraProbeOffset = (float) (cameraProbeOffset - 0.1D * amplifer);
			}
		}
		if (Math.abs(cameraOffsetY) <= 0.1D * amplifer) {
			cameraOffsetY = 0.0D;
		}
		if (cameraOffsetY < 0.0D) {
			cameraOffsetY += 0.1D * amplifer;
		}
		if (cameraOffsetY > 0.0D) {
			cameraOffsetY -= 0.1D * amplifer;
		}
		GL11.glTranslatef(0.0F,1.62f-eyeHeight , 0.0F);
		return true;
	}
	public static Vec3 onGetPositionEyes(EntityPlayer player, float partialTicks) {
		Vec3 result;
		if (partialTicks == 1.0F)
        {
			result= Vec3.createVectorHelper(player.posX, player.posY, player.posZ);
        }
        else
        {
            double d0 = player.prevPosX + (player.posX - player.prevPosX) * (double)partialTicks;
            double d1 = player.prevPosY + (player.posY - player.prevPosY) * (double)partialTicks;
            double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)partialTicks;
            result= Vec3.createVectorHelper(d0, d1, d2);
        }
		return onGetPositionEyes(player, partialTicks, result);
    }
	public static Vec3 onGetPositionEyes(EntityPlayer player, float partialTicks, Vec3 vec3d) {
		PlayerState state = null;
		float offest = 0.0F;
		if (player == (Minecraft.getMinecraft()).thePlayer) {
			state = clientPlayerState;
			offest = cameraProbeOffset;
		} else {
			if (!ohterPlayerStateMap.containsKey(Integer.valueOf(player.getEntityId()))) {
				return vec3d;
			}
			state = ohterPlayerStateMap.get(Integer.valueOf(player.getEntityId()));
			offest = state.probeOffset;
		}

		if (offest != 0.0F) {
			Vec3 tempVec3 = Vec3.createVectorHelper(offest * -0.6D, 0.0D, 0.0D);
			tempVec3.rotateAroundY((float) (-Minecraft.getMinecraft().thePlayer.rotationYaw * Math.PI / 180.0D));
			return vec3d.addVector(tempVec3.xCoord, tempVec3.yCoord, tempVec3.zCoord);
		}
		float fixY=(ClientHandler.clientPlayerState.isSitting?-0.52f:0.0f);
		if(ClientHandler.clientPlayerState.isCrawling)
			fixY=-1.22f;
		vec3d.yCoord+=fixY;
		return vec3d;
	}

	@SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = false)
	public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
		isSneaking = event.entityPlayer.isSneaking();
		if (!clientPlayerState.isCrawling && !clientPlayerState.isSitting) {
			return;
		}
		if (event.entityPlayer instanceof EntityPlayerSP) {
			((EntityPlayerSP) event.entityPlayer).movementInput.sneak = false;
		} else {
			event.entityPlayer.setSneaking(false);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onRenderPlayerPost(RenderPlayerEvent.Post event) {
		if (event.entityPlayer instanceof EntityPlayerSP) {
			((EntityPlayerSP) event.entityPlayer).movementInput.sneak = isSneaking;
		} else {
			event.entityPlayer.setSneaking(isSneaking);
		}
	}

	static public void onInputUpdate(MovementInput movementInput) {
		
		EntityPlayerSP entityPlayerSP = (Minecraft.getMinecraft()).thePlayer;

		if (clientPlayerState.isSitting) {
			movementInput.sneak=false;
			(movementInput).moveForward = (float) ((movementInput).moveForward * 0.3D);
			(movementInput).moveStrafe = (float) ((movementInput).moveStrafe * 0.3D);
			if ((movementInput).jump) {
				clientPlayerSitMoveAmplifier = 0.0D;
			}
			double d0 = 0.3D;
			if ((movementInput).moveForward != 0.0F
					&& isButtonDown((Minecraft.getMinecraft()).gameSettings.keyBindSprint.getKeyCode())
					&& clientPlayerSitMoveAmplifier < clientPlayerSitMoveLess * 2.0D
					&& !(Minecraft.getMinecraft()).thePlayer.isSneaking()
					&& ((EntityPlayer) entityPlayerSP).worldObj.func_147461_a(AxisAlignedBB.getBoundingBox(
							((EntityPlayer) entityPlayerSP).posX - d0,
							((EntityPlayer) entityPlayerSP).posY-((EntityPlayer) entityPlayerSP).yOffset,
							((EntityPlayer) entityPlayerSP).posZ - d0,
							((EntityPlayer) entityPlayerSP).posX + d0,
							((EntityPlayer) entityPlayerSP).posY +0.2D,//1122 origin + 1.8D
							((EntityPlayer) entityPlayerSP).posZ + d0))
							.isEmpty()) {
				clientPlayerState.disableSit();
			}
		}
		if (clientPlayerState.isCrawling) {
			movementInput.sneak=false;
			(movementInput).moveForward = (float) ((movementInput).moveForward * 0.4D);
			(movementInput).moveStrafe = (float) ((movementInput).moveStrafe * 0.4D);
			boolean jumpOrigin=(movementInput).jump;
			(movementInput).jump = false;
			double d0 = 0.3D;
			AxisAlignedBB aabb=AxisAlignedBB.getBoundingBox(
					((EntityPlayer) entityPlayerSP).posX - d0,
					((EntityPlayer) entityPlayerSP).posY-((EntityPlayer) entityPlayerSP).yOffset,
					((EntityPlayer) entityPlayerSP).posZ - d0,
					((EntityPlayer) entityPlayerSP).posX + d0,
					((EntityPlayer) entityPlayerSP).posY+0.2D ,//1122 origin + 1.8D
					((EntityPlayer) entityPlayerSP).posZ + d0);
			boolean headCheck=((EntityPlayer) entityPlayerSP).worldObj
					.func_147461_a(aabb)
					.isEmpty();
			if (clientPlayerState.canCrawl()&&jumpOrigin && headCheck) {
				clientPlayerState.disableCrawling();
			}
			
		}
		if (clientPlayerState.probe != 0) {
			(movementInput).moveForward = (float) ((movementInput).moveForward * 0.9D);
			(movementInput).moveStrafe = (float) ((movementInput).moveStrafe * 0.9D);
		}

		if (speedInAir!=null&&(Minecraft.getMinecraft()).thePlayer != null) {
			try {
				speedInAir.set((Minecraft.getMinecraft()).thePlayer, 0.02F);
			} catch (IllegalArgumentException | IllegalAccessException e) {

				e.printStackTrace();
			}
		}
	}

	@SubscribeEvent
	public void onTickRenderStart(RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			if (!isButtonDown(sit.getKeyCode())) {
				sitKeyLock = false;
			}
			if (isButtonDown(sit.getKeyCode()) && (Minecraft.getMinecraft()).thePlayer != null
					&& (Minecraft.getMinecraft()).thePlayer.fallDistance > 1.0F) {
				wannaSliding = true;
			}

			onSit();

			if (!isButtonDown(crawling.getKeyCode())) {
				crawlingKeyLock = false;
			}

			if (!isButtonDown(leftProbe.getKeyCode()) && !isButtonDown(rightProbe.getKeyCode())) {
				probeKeyLock = false;
				if (!ModContainer.CONFIG.lean.autoHold && clientPlayerState.probe != 0) {
					clientPlayerState.resetProbe();
				}
			} else if (isButtonDown(leftProbe.getKeyCode())) {
				if (!ModContainer.CONFIG.lean.autoHold && clientPlayerState.probe != -1) {
					probeKeyLock = false;
				}
			} else if (isButtonDown(rightProbe.getKeyCode()) && !ModContainer.CONFIG.lean.autoHold
					&& clientPlayerState.probe != 1) {
				probeKeyLock = false;
			}
		}else {
			//RenderGlobal.drawOutlinedBoundingBox(axisalignedbb, 16777215);
		}
	}

	private static boolean isButtonsDownAll(int... id) {
		boolean flag = true;
		for (int i = 0; i < id.length; i++) {
			flag = (flag && isButtonDown(id[i]));
			if (!flag) {
				break;
			}
		}
		return flag;
	}

	private static boolean isButtonsDownOne(int... id) {
		boolean flag = false;
		for (int i = 0; i < id.length; i++) {
			flag = (flag || isButtonDown(id[i]));
			if (flag) {
				break;
			}
		}
		return flag;
	}

	@SubscribeEvent
	public void onTickPlayer(TickEvent.PlayerTickEvent event) {
		if ((Minecraft.getMinecraft()).thePlayer == null) {
			return;
		}
		if (event.player != (Minecraft.getMinecraft()).thePlayer) {
			return;
		}
		if (!event.player.isEntityAlive()) {
			clientPlayerSitMoveAmplifier = 0.0D;
			if (clientPlayerState.isSitting) {
				clientPlayerState.disableSit();
			}
			if (clientPlayerState.isCrawling) {
				clientPlayerState.disableCrawling();
			}
		}
		if (event.phase != TickEvent.Phase.END) {
			if (event.player.isRiding()) {
				clientPlayerSitMoveAmplifier = 0.0D;
				if (clientPlayerState.isSitting) {
					clientPlayerState.disableSit();
				}
				if (clientPlayerState.isCrawling) {
					clientPlayerState.disableCrawling();
				}
				clientPlayerState.resetProbe();
			}
			if (event.player.isSneaking()) {
				clientPlayerSitMoveAmplifier = 0.0D;
			}
			if (clientPlayerSitMoveAmplifier > 0.0D) {
				if (clientPlayerState.isSitting && event.player == (Minecraft.getMinecraft()).thePlayer) {
					if (enableForceGravity && event.player.motionY <= 0.0D) {
						event.player.motionY = -forceGravity;
					}
					event.player.motionX = clientPlayerSitMoveVec3d.xCoord * clientPlayerSitMoveAmplifier
							* clientPlayerSitMoveAmplifierUser * clientPlayerSitMoveAmplifierCharged;

					event.player.motionZ = clientPlayerSitMoveVec3d.zCoord * clientPlayerSitMoveAmplifier
							* clientPlayerSitMoveAmplifierUser * clientPlayerSitMoveAmplifierCharged;

					if ((Minecraft.getMinecraft()).thePlayer.onGround) {
						(Minecraft.getMinecraft()).thePlayer.playSound(
								"dig.grass", 2.0F * (float) (clientPlayerSitMoveAmplifier
										* clientPlayerSitMoveAmplifierCharged / ModContainer.CONFIG.slide.maxForce),
								0.8F);
					}
				}
				clientPlayerSitMoveAmplifier -= clientPlayerSitMoveLess;
				if (!event.player.onGround && event.player.fallDistance > 1.0F && !event.player.isInWater()) {
					clientPlayerSitMoveAmplifier += clientPlayerSitMoveLess;
				}
				if (clientPlayerSitMoveAmplifier <= 0.0D) {
					if (ModContainer.CONFIG.sit.autoHold) {
						if (!isButtonDown(sit.getKeyCode())) {
							if(standingCheck())
								clientPlayerState.disableSit();
						}
					} else if (isButtonDown(sit.getKeyCode())) {
						if(standingCheck())
							clientPlayerState.disableSit();
					}
				}
			}

			return;
		}
		float f = event.player.width;
		float f1 = event.player.height;
		if (clientPlayerState.isSitting) {
			f1 = 1.2F;
		} else if (clientPlayerState.isCrawling) {
			f1 = 0.5F;
		}else {
			f1=1.8f;
		}
		if (f != event.player.width || f1 != event.player.height) {
			AxisAlignedBB axisalignedbb = event.player.boundingBox;
			axisalignedbb = AxisAlignedBB.getBoundingBox(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ,
					axisalignedbb.minX + f, axisalignedbb.minY + f1, axisalignedbb.minZ + f);

			if (event.player.worldObj.func_147461_a(axisalignedbb).isEmpty()) {
				try {
					 ServerListener.setSize.invoke(event.player, new Object[] { f,f1 });
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		// what for?
		if (clientPlayerSitMoveAmplifier > 0.0D) {
			 TacticalHandler.sendNoStep(100);
		}
		if (event.player.fallDistance > 1.0F && wannaSliding) {
			 TacticalHandler.sendNoFall();
		}
		 TacticalHandler.sendToServer(clientPlayerState.writeCode());
	}
	public static boolean standingCheck() {
		EntityPlayerSP entityPlayerSP = (Minecraft.getMinecraft()).thePlayer;
		double d0 = 0.3D;
		return ((EntityPlayer) entityPlayerSP).worldObj.func_147461_a(AxisAlignedBB.getBoundingBox(
				((EntityPlayer) entityPlayerSP).posX - d0,
				((EntityPlayer) entityPlayerSP).posY-((EntityPlayer) entityPlayerSP).yOffset,
				((EntityPlayer) entityPlayerSP).posZ - d0,
				((EntityPlayer) entityPlayerSP).posX + d0,
				((EntityPlayer) entityPlayerSP).posY +0.2D,//1122 origin + 1.8D
				((EntityPlayer) entityPlayerSP).posZ + d0))
				.isEmpty();
	}
	public static boolean isSitting(Integer id) {
		if (!ohterPlayerStateMap.containsKey(id)) {
			return false;
		}
		return ((PlayerState) ohterPlayerStateMap.get(id)).isSitting;
	}

	public static boolean isCrawling(Integer id) {
		if (!ohterPlayerStateMap.containsKey(id)) {
			return false;
		}
		return ((PlayerState) ohterPlayerStateMap.get(id)).isCrawling;
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onTickOtherPlayer(TickEvent.PlayerTickEvent event) {
		if (event.side == Side.CLIENT && event.phase == TickEvent.Phase.END) {
			float f = event.player.width;
			float f1 = event.player.height;
			if (isSitting(Integer.valueOf(event.player.getEntityId()))) {
				f1 = 1.4F;
			} else if (isCrawling(Integer.valueOf(event.player.getEntityId()))) {
				f1 = 0.5F;
			}else {
				f1=1.8F;
			}
			if (f != event.player.width || f1 != event.player.height) {
				AxisAlignedBB axisalignedbb = event.player.boundingBox;
				axisalignedbb = AxisAlignedBB.getBoundingBox(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ,
						axisalignedbb.minX + f, axisalignedbb.minY + f1, axisalignedbb.minZ + f);
				if (event.player.worldObj.func_147461_a(axisalignedbb).isEmpty()) {
					try {
						 ServerListener.setSize.invoke(event.player,new Object[] { Float.valueOf(f),Float.valueOf(f1) });
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			if(ohterPlayerStateMap.containsKey(event.player.getEntityId()))
			{
				getEntityBoundingBox(event.player,event.player.boundingBox);
				PlayerState state = ohterPlayerStateMap.get(Integer.valueOf(event.player.getEntityId()));
				if (state.probeOffset != 0.0F)
				{
					try {
						//ServerListener.setSize.invoke(event.player,new Object[] { Float.valueOf(f),Float.valueOf(f1) });
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@SubscribeEvent
	   public void onPlaySoundAtEntity(PlaySoundAtEntityEvent event) {
	     if ((Minecraft.getMinecraft()).thePlayer != null && 
	       event.entity == (Minecraft.getMinecraft()).thePlayer && 
	       clientPlayerSitMoveAmplifier > 0.0D && (
	    		   event.name.contains("step"))) {
	       event.setCanceled(true);
	     }
	   }
	public static AxisAlignedBB getEntityBoundingBox(Entity entity, AxisAlignedBB bb) {
		if (entity instanceof net.minecraft.client.entity.EntityOtherPlayerMP && !entity.isDead
				&& ohterPlayerStateMap.containsKey(Integer.valueOf(entity.getEntityId()))) {
			PlayerState state = ohterPlayerStateMap.get(Integer.valueOf(entity.getEntityId()));
			if (state.probeOffset != 0.0F) {
				Vec3 vec3d = Vec3.createVectorHelper(0.0D, 0.0D, 0.0D).addVector(state.probeOffset * -0.5D, 0.0D, 0.0D);
				vec3d.rotateAroundY(-entity.rotationYaw * 3.14F / 180.0F);
				//bb.minX=bb.minX + vec3d.xCoord;
				//bb.minZ=bb.minZ + vec3d.zCoord;
				//bb.maxX=bb.maxX + vec3d.xCoord;
				//bb.maxZ=bb.maxZ + vec3d.zCoord;
				//System.out.println(vec3d);
				entity.setPosition(entity.posX, entity.posY, entity.posZ);
				bb.offset(vec3d.xCoord, 0, vec3d.zCoord);
				return bb;
//				return AxisAlignedBB.getBoundingBox(
//						bb.minX + vec3d.xCoord, 
//						bb.minY,
//						bb.minZ + vec3d.zCoord,
//						bb.maxX + vec3d.xCoord,
//						bb.maxY,
//						bb.maxZ + vec3d.zCoord);
			}
		}
		return bb;
	}
}
