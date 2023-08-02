package com.vicmatskiv.weaponlib;

import com.vicmatskiv.weaponlib.animation.DebugPositioner;
import com.vicmatskiv.weaponlib.animation.MatrixHelper;
import com.vicmatskiv.weaponlib.animation.MultipartPositioning;
import com.vicmatskiv.weaponlib.animation.MultipartRenderStateDescriptor;
import com.vicmatskiv.weaponlib.animation.MultipartRenderStateManager;
import com.vicmatskiv.weaponlib.animation.MultipartTransition;
import com.vicmatskiv.weaponlib.compatibility.CompatibilityProvider;
import com.vicmatskiv.weaponlib.compatibility.CompatibleEnumHandSide;
import com.vicmatskiv.weaponlib.compatibility.CompatibleExtraEntityFlags;
import com.vicmatskiv.weaponlib.compatibility.CompatibleTransformType;

import indi.ma.client.ClientHandler;
import indi.ma.util.BaiscMath;

import java.nio.FloatBuffer;
import java.util.List;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

public class PlayerRenderer {
  private static final Logger logger = LogManager.getLogger(PlayerRenderer.class);
  
  private PlayerTransitionProvider transitionProvider;
  
  protected static class StateDescriptor implements MultipartRenderStateDescriptor<RenderableState, Part, RenderContext<RenderableState>> {
    protected MultipartRenderStateManager<RenderableState, Part, RenderContext<RenderableState>> stateManager;
    
    protected float rate;
    
    protected float amplitude = 0.04F;
    
    public StateDescriptor(MultipartRenderStateManager<RenderableState, Part, RenderContext<RenderableState>> stateManager, float rate, float amplitude) {
      this.stateManager = stateManager;
      this.rate = rate;
      this.amplitude = amplitude;
    }
    
    public MultipartRenderStateManager<RenderableState, Part, RenderContext<RenderableState>> getStateManager() {
      return this.stateManager;
    }
  }
  
  private static class EquippedPlayerTransitionProvider extends PlayerTransitionProvider {
    private PlayerTransitionProvider delegate;
    
    private EntityPlayer player;
    
    EquippedPlayerTransitionProvider(EntityPlayer player, PlayerTransitionProvider delegate) {
      this.player = player;
      this.delegate = delegate;
    }
    
    public List<MultipartTransition<Part, RenderContext<RenderableState>>> getTransitions(RenderableState state) {
      ItemStack heldStack = CompatibilityProvider.compatibility.getHeldItemMainHand((EntityLivingBase)this.player);
      if (heldStack == null || heldStack.getItem() instanceof Weapon);
      return this.delegate.getTransitions(state);
    }
  }
  
  private class PositionerDescriptor {
    MultipartPositioning.Positioner<Part, RenderContext<RenderableState>> positioner;
    
    boolean leftHandPositioned;
    
    boolean rightHandPositioned;
    
    PositionerDescriptor(MultipartPositioning.Positioner<Part, RenderContext<RenderableState>> positioner) {
      this.positioner = positioner;
    }
  }
  
  private ThreadLocal<PositionerDescriptor> currentPositioner = new ThreadLocal<>();
  
  private int currentFlags;
  
  private int newFlags;
  
  private long renderingStartTimestamp;
  
  private long playerStopMovingTimestamp;
  
  private ClientModContext clientModContext;
  
  private MultipartRenderStateManager<RenderableState, Part, RenderContext<RenderableState>> generalPlayerStateManager;
  
  public PlayerRenderer(EntityPlayer player, ClientModContext clientModContext) {
    this.clientModContext = clientModContext;
    this
      
      .transitionProvider = new EquippedPlayerTransitionProvider(player, clientModContext.getPlayerTransitionProvider());
  }
  
  public MultipartPositioning.Positioner<Part, RenderContext<RenderableState>> getCurrentPositioner() {
    PositionerDescriptor descriptor = this.currentPositioner.get();
    return (descriptor != null) ? descriptor.positioner : null;
  }
  
  private MultipartRenderStateDescriptor<RenderableState, Part, RenderContext<RenderableState>> getStateDescriptor(EntityPlayer player) {
    if (this.currentFlags != this.newFlags)
      this.generalPlayerStateManager = null; 
    boolean isProning = ((this.newFlags & CompatibleExtraEntityFlags.PRONING) != 0);
    if (this.generalPlayerStateManager == null) {
      this.generalPlayerStateManager = new MultipartRenderStateManager(RenderableState.NORMAL, this.transitionProvider, () -> Long.valueOf(currentTime(player)));
    } else if (isProning && player.distanceWalkedModified == player.prevDistanceWalkedModified) {
      this.generalPlayerStateManager.setState(RenderableState.PRONING_AIMING, true, true, true);
    } else if (isProning) {
      this.generalPlayerStateManager.setCycleState(RenderableState.PRONING, false);
    } else {
      ItemStack heldStack = CompatibilityProvider.compatibility.getHeldItemMainHand((EntityLivingBase)player);
      if (heldStack != null && heldStack.getItem() instanceof Weapon)
        return (MultipartRenderStateDescriptor<RenderableState, Part, RenderContext<RenderableState>>)((Weapon)heldStack.getItem()).getRenderer().getThirdPersonStateDescriptor((EntityLivingBase)player, heldStack); 
      this.generalPlayerStateManager.setState(RenderableState.NORMAL, true, false);
    } 
    return new StateDescriptor(this.generalPlayerStateManager, 0.0F, 0.0F);
  }
  
  private long currentTime(EntityPlayer player) {
    long elapseRenderingStart = System.currentTimeMillis() - this.renderingStartTimestamp;
    int renderingStartThreshold = 400;
    if (elapseRenderingStart < renderingStartThreshold)
      return elapseRenderingStart; 
    long afterStopMovingTimeout = 0L;
    if (player.distanceWalkedModified == player.prevDistanceWalkedModified) {
      if (this.playerStopMovingTimestamp == 0L) {
        this.playerStopMovingTimestamp = System.currentTimeMillis();
      } else if (afterStopMovingTimeout < 1000L) {
        afterStopMovingTimeout = System.currentTimeMillis() - this.playerStopMovingTimestamp;
      } 
    } else {
      this.playerStopMovingTimestamp = 0L;
    } 
    return (long)(renderingStartThreshold + player.distanceWalkedModified * 600.0F + (float)afterStopMovingTimeout);
  }
  
  public void renderModel(ModelBiped modelPlayer, EntityPlayer player, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
    this.newFlags = CompatibleExtraEntityFlags.getFlags((Entity)player);
    if (this.newFlags != this.currentFlags)
      this.renderingStartTimestamp = System.currentTimeMillis(); 
    this.currentPositioner.remove();
    renderAnimatedModel(modelPlayer, player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
    this.currentFlags = this.newFlags;
  }
  
  private void renderBipedModel(ModelBiped model, Entity entity, float limbSwing, float limbSwingAmount, float p_78088_4_, float p_78088_5_, float p_78088_6_, float p_78088_7_) {
    model.setRotationAngles(limbSwing, limbSwingAmount, p_78088_4_, p_78088_5_, p_78088_6_, p_78088_7_, entity);
    if (model.isChild) {
      float f6 = 2.0F;
      GL11.glPushMatrix();
      GL11.glScalef(1.5F / f6, 1.5F / f6, 1.5F / f6);
      GL11.glTranslatef(0.0F, 16.0F * p_78088_7_, 0.0F);
      model.bipedHead.render(p_78088_7_);
      GL11.glPopMatrix();
      GL11.glPushMatrix();
      GL11.glScalef(1.0F / f6, 1.0F / f6, 1.0F / f6);
      GL11.glTranslatef(0.0F, 24.0F * p_78088_7_, 0.0F);
      model.bipedBody.render(p_78088_7_);
      model.bipedRightArm.render(p_78088_7_);
      model.bipedLeftArm.render(p_78088_7_);
      model.bipedRightLeg.render(p_78088_7_);
      model.bipedLeftLeg.render(p_78088_7_);
      model.bipedHeadwear.render(p_78088_7_);
      GL11.glPopMatrix();
    } else {
      model.bipedHead.render(p_78088_7_);
      model.bipedBody.render(p_78088_7_);
      model.bipedRightArm.render(p_78088_7_);
      model.bipedLeftArm.render(p_78088_7_);
      model.bipedRightLeg.render(p_78088_7_);
      model.bipedLeftLeg.render(p_78088_7_);
      model.bipedHeadwear.render(p_78088_7_);
    } 
  }
  
  private void renderAnimatedModel(ModelBiped modelPlayer, EntityPlayer player, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
    MultipartRenderStateDescriptor<RenderableState, Part, RenderContext<RenderableState>> stateDescriptor = getStateDescriptor(player);
    MultipartPositioning<Part, RenderContext<RenderableState>> multipartPositioning = stateDescriptor.getStateManager().nextPositioning();
    MultipartPositioning.Positioner<Part, RenderContext<RenderableState>> positioner = multipartPositioning.getPositioner();
    this.currentPositioner.set(new PositionerDescriptor(positioner));
    GL11.glPushMatrix();
    RenderContext<RenderableState> renderContext = new RenderContext<>(this.clientModContext, (EntityLivingBase)player, null);
    renderContext.setLimbSwing(limbSwing);
    renderContext.setFlimbSwingAmount(limbSwingAmount);
    renderContext.setAgeInTicks(ageInTicks);
    renderContext.setScale(scale);
    renderContext.setNetHeadYaw(netHeadYaw);
    renderContext.setHeadPitch(headPitch);
    renderContext.setCompatibleTransformType(CompatibleTransformType.NONE);
    positioner.position(Part.MAIN, renderContext);
    if(true)
    {
    	float probeOffset=0.8f;
    	if(probeOffset != 0.0f) {
        	//example code
//        	float f2 = BaiscMath.interpolateRotation(player.prevRenderYawOffset, player.renderYawOffset, 1);
//            GL11.glRotatef(180.0f - player.rotationYawHead, 0.0f, 1.0f, 0.0f);
//            GL11.glTranslated(((double)probeOffset) * 0.1, 0.0, 0.0);
//            GL11.glRotatef(180.0f - player.rotationYawHead, 0.0f, -1.0f, 0.0f);
//            //GL11.glRotatef(180.0f - f2, 0.0f, 1.0f, 0.0f);
//            GL11.glRotatef(probeOffset * -20.0f, 0.0f, 0.0f, 1.0f);
        }
    	//ClientHandler.applyRotations(player);
    }
    modelPlayer.setRotationAngles(renderContext.getLimbSwing(), renderContext.getFlimbSwingAmount(), renderContext.getAgeInTicks(), renderContext.getNetHeadYaw(), renderContext.getHeadPitch(), renderContext.getScale(), (Entity)renderContext.getPlayer());
	  
    //probe fix part
	if(true)
	{
		//example code
//		float v3=probeOffset;
//		if(v3 >= 0.0f) {
//			modelPlayer.bipedRightLeg.rotateAngleZ = (float)(((double)modelPlayer.bipedRightLeg.rotateAngleZ) + ((double)(20.0f * v3)) * 3.14 / 180.0);
//			modelPlayer.bipedRightLeg.rotateAngleZ=0.2f;
//          }
//		else {
//			modelPlayer.bipedLeftLeg.rotateAngleZ = (float)(((double)modelPlayer.bipedLeftLeg.rotateAngleZ) + ((double)(20.0f * v3)) * 3.14 / 180.0);
//			modelPlayer.bipedLeftLeg.rotateAngleZ=-0.2f;
//		}
		ClientHandler.setRotationAngles(modelPlayer,renderContext.getLimbSwing(), renderContext.getFlimbSwingAmount(), renderContext.getAgeInTicks(), renderContext.getNetHeadYaw(), renderContext.getHeadPitch(), renderContext.getScale(), (Entity)renderContext.getPlayer());
		//ClientHandler.setRotationAngles(modelPlayer, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, player);
		//setRotationAngles
	}
	
    
    renderBody(positioner, modelPlayer, renderContext);
    renderHead(positioner, modelPlayer, renderContext);
    renderLeftArm(positioner, modelPlayer, renderContext);
    renderRightArm(positioner, modelPlayer, renderContext);
    renderLeftLeg(positioner, modelPlayer, renderContext);
    renderRightLeg(positioner, modelPlayer, renderContext);
    GL11.glPopMatrix();
  }
  
  private void renderBody(MultipartPositioning.Positioner<Part, RenderContext<RenderableState>> positioner, ModelBiped modelPlayer, RenderContext<RenderableState> renderContext) {
    GL11.glPushMatrix();
    modelPlayer.bipedBody.render(renderContext.getScale());
    CompatibilityProvider.compatibility.renderBodywear(modelPlayer, renderContext.getScale());
    GL11.glPopMatrix();
  }
  
  private void renderHead(MultipartPositioning.Positioner<Part, RenderContext<RenderableState>> positioner, ModelBiped modelPlayer, RenderContext<RenderableState> renderContext) {
    GL11.glPushMatrix();
    FloatBuffer preBuf = MatrixHelper.getModelViewMatrixBuffer();
    positioner.position(Part.HEAD, renderContext);
    FloatBuffer postBuf = MatrixHelper.getModelViewMatrixBuffer();
    if (!preBuf.equals(postBuf));
    modelPlayer.bipedHead.render(renderContext.getScale());
    CompatibilityProvider.compatibility.renderHeadwear(modelPlayer, renderContext.getScale());
    GL11.glPopMatrix();
  }
  
  private void renderRightArm(MultipartPositioning.Positioner<Part, RenderContext<RenderableState>> positioner, ModelBiped modelPlayer, RenderContext<RenderableState> renderContext) {
    GL11.glPushMatrix();
    FloatBuffer preBuf = MatrixHelper.getModelViewMatrixBuffer();
    positioner.position(Part.RIGHT_HAND, renderContext);
    if (DebugPositioner.isDebugModeEnabled())
      DebugPositioner.position(Part.RIGHT_HAND, renderContext); 
    FloatBuffer postBuf = MatrixHelper.getModelViewMatrixBuffer();
    if (!preBuf.equals(postBuf)) {
      modelPlayer.bipedRightArm.rotateAngleX = modelPlayer.bipedRightArm.rotateAngleY = modelPlayer.bipedRightArm.rotateAngleZ = 0.0F;
      ((PositionerDescriptor)this.currentPositioner.get()).rightHandPositioned = true;
    } 
    modelPlayer.bipedRightArm.render(renderContext.getScale());
    CompatibilityProvider.compatibility.renderRightArmwear(modelPlayer, renderContext.getScale());
    GL11.glPopMatrix();
  }
  
  private void renderLeftArm(MultipartPositioning.Positioner<Part, RenderContext<RenderableState>> positioner, ModelBiped modelPlayer, RenderContext<RenderableState> renderContext) {
    GL11.glPushMatrix();
    FloatBuffer preBuf = MatrixHelper.getModelViewMatrixBuffer();
    positioner.position(Part.LEFT_HAND, renderContext);
    if (DebugPositioner.isDebugModeEnabled())
      DebugPositioner.position(Part.LEFT_HAND, renderContext); 
    FloatBuffer postBuf = MatrixHelper.getModelViewMatrixBuffer();
    if (!preBuf.equals(postBuf)) {
      modelPlayer.bipedLeftArm.rotateAngleX = modelPlayer.bipedLeftArm.rotateAngleY = modelPlayer.bipedLeftArm.rotateAngleZ = 0.0F;
      ((PositionerDescriptor)this.currentPositioner.get()).leftHandPositioned = true;
    } 
    modelPlayer.bipedLeftArm.render(renderContext.getScale());
    CompatibilityProvider.compatibility.renderLeftArmwear(modelPlayer, renderContext.getScale());
    GL11.glPopMatrix();
  }
  
  private void renderRightLeg(MultipartPositioning.Positioner<Part, RenderContext<RenderableState>> positioner, ModelBiped modelPlayer, RenderContext<RenderableState> renderContext) {
    GL11.glPushMatrix();
    FloatBuffer preBuf = MatrixHelper.getModelViewMatrixBuffer();
    positioner.position(Part.RIGHT_LEG, renderContext);
    FloatBuffer postBuf = MatrixHelper.getModelViewMatrixBuffer();
    if (!preBuf.equals(postBuf))
      modelPlayer.bipedRightLeg.rotateAngleX = modelPlayer.bipedRightLeg.rotateAngleY = modelPlayer.bipedRightLeg.rotateAngleZ = 0.0F; 
    modelPlayer.bipedRightLeg.render(renderContext.getScale());
    CompatibilityProvider.compatibility.renderRightLegwear(modelPlayer, renderContext.getScale());
    GL11.glPopMatrix();
  }
  
  private void renderLeftLeg(MultipartPositioning.Positioner<Part, RenderContext<RenderableState>> positioner, ModelBiped modelPlayer, RenderContext<RenderableState> renderContext) {
    GL11.glPushMatrix();
    FloatBuffer preBuf = MatrixHelper.getModelViewMatrixBuffer();
    positioner.position(Part.LEFT_LEG, renderContext);
    FloatBuffer postBuf = MatrixHelper.getModelViewMatrixBuffer();
    if (!preBuf.equals(postBuf))
      modelPlayer.bipedLeftLeg.rotateAngleX = modelPlayer.bipedLeftLeg.rotateAngleY = modelPlayer.bipedLeftLeg.rotateAngleZ = 0.0F; 
    modelPlayer.bipedLeftLeg.render(renderContext.getScale());
    CompatibilityProvider.compatibility.renderLeftLegwear(modelPlayer, renderContext.getScale());
    GL11.glPopMatrix();
  }
  
  public boolean renderArmor(ModelBiped modelPlayer, EntityPlayer player, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
    PositionerDescriptor descriptor = this.currentPositioner.get();
    if (descriptor != null) {
      MultipartPositioning.Positioner<Part, RenderContext<RenderableState>> positioner = descriptor.positioner;
      GL11.glPushMatrix();
      RenderContext<RenderableState> renderContext = new RenderContext<>(this.clientModContext, (EntityLivingBase)player, null);
      renderContext.setAgeInTicks(ageInTicks);
      renderContext.setScale(scale);
      renderContext.setLimbSwing(limbSwing);
      renderContext.setFlimbSwingAmount(limbSwingAmount);
      renderContext.setNetHeadYaw(netHeadYaw);
      renderContext.setHeadPitch(headPitch);
      renderContext.setCompatibleTransformType(CompatibleTransformType.NONE);
      modelPlayer.setRotationAngles(renderContext.getLimbSwing(), renderContext.getFlimbSwingAmount(), renderContext
          .getAgeInTicks(), renderContext.getNetHeadYaw(), renderContext
          .getHeadPitch(), renderContext.getScale(), (Entity)renderContext.getPlayer());
      positioner.position(Part.MAIN, renderContext);
      renderBody(positioner, modelPlayer, renderContext);
      renderHead(positioner, modelPlayer, renderContext);
      renderLeftArm(positioner, modelPlayer, renderContext);
      renderRightArm(positioner, modelPlayer, renderContext);
      renderLeftLeg(positioner, modelPlayer, renderContext);
      renderRightLeg(positioner, modelPlayer, renderContext);
      GL11.glPopMatrix();
    } 
    return (descriptor != null);
  }
  
  public boolean positionItemSide(EntityPlayer player, ItemStack itemStack, CompatibleTransformType transformType, CompatibleEnumHandSide handSide) {
    PositionerDescriptor descriptor = this.currentPositioner.get();
    if (descriptor != null) {
      if ((handSide == null || handSide == CompatibleEnumHandSide.RIGHT) && !descriptor.rightHandPositioned)
        return false; 
      if (handSide == CompatibleEnumHandSide.LEFT && !descriptor.leftHandPositioned)
        return false; 
      MultipartPositioning.Positioner<Part, RenderContext<RenderableState>> positioner = descriptor.positioner;
      RenderContext<RenderableState> renderContext = new RenderContext<>(this.clientModContext, (EntityLivingBase)player, null);
      positioner.position(Part.MAIN, renderContext);
      if (handSide == CompatibleEnumHandSide.LEFT) {
        positioner.position(Part.LEFT_HAND, renderContext);
      } else if (handSide == null || handSide == CompatibleEnumHandSide.RIGHT) {
        positioner.position(Part.RIGHT_HAND, renderContext);
      } 
      GL11.glTranslatef(-0.35F, 0.1F, -0.0F);
      GL11.glRotatef(-378.0F, 1.0F, 0.0F, 0.0F);
      GL11.glRotatef(360.0F, 0.0F, 1.0F, 0.0F);
      GL11.glRotatef(0.0F, 0.0F, 0.0F, 1.0F);
    } 
    return (descriptor != null);
  }
}
