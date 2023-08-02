/*    */ package com.vicmatskiv.weaponlib.perspective;
/*    */ 
/*    */ import com.vicmatskiv.weaponlib.ClientModContext;
/*    */ import com.vicmatskiv.weaponlib.ItemScope;
/*    */ import com.vicmatskiv.weaponlib.PlayerItemInstance;
/*    */ import com.vicmatskiv.weaponlib.PlayerWeaponInstance;
/*    */ import com.vicmatskiv.weaponlib.RenderContext;
/*    */ import com.vicmatskiv.weaponlib.RenderableState;
/*    */ import com.vicmatskiv.weaponlib.compatibility.CompatibleRenderTickEvent;
/*    */ import com.vicmatskiv.weaponlib.shader.DynamicShaderContext;
/*    */ import com.vicmatskiv.weaponlib.shader.DynamicShaderPhase;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class OpticalScopePerspective
/*    */   extends FirstPersonPerspective<RenderableState>
/*    */ {
/*    */   private static final int DEFAULT_WIDTH = 400;
/*    */   private static final int DEFAULT_HEIGHT = 400;
/*    */   
/*    */   public void activate(ClientModContext modContext, PerspectiveManager manager) {
/* 24 */     PlayerWeaponInstance instance = modContext.getMainHeldWeapon();
/* 25 */     if (instance != null) {
/* 26 */       ItemScope scope = instance.getScope();
/* 27 */       if (scope.isOptical()) {
/* 28 */         setSize(scope.getWidth(), scope.getHeight());
/*    */       }
/*    */     } 
/* 31 */     super.activate(modContext, manager);
/*    */   }
/*    */ 
/*    */   
/*    */   public float getBrightness(RenderContext<RenderableState> renderContext) {
/* 36 */     float brightness = 0.0F;
/* 37 */     PlayerWeaponInstance instance = renderContext.getWeaponInstance();
/* 38 */     if (instance == null) {
/* 39 */       return 0.0F;
/*    */     }
/* 41 */     boolean aimed = (instance != null && instance.isAimed());
/* 42 */     float progress = Math.min(1.0F, renderContext.getTransitionProgress());
/*    */     
/* 44 */     if (isAimingState((RenderableState)renderContext.getFromState()) && isAimingState((RenderableState)renderContext.getToState())) {
/* 45 */       brightness = 1.0F;
/* 46 */     } else if (progress > 0.0F && aimed && isAimingState((RenderableState)renderContext.getToState())) {
/* 47 */       brightness = progress;
/* 48 */     } else if (isAimingState((RenderableState)renderContext.getFromState()) && progress > 0.0F && !aimed) {
/* 49 */       brightness = Math.max(1.0F - progress, 0.0F);
/*    */     } 
/* 51 */     return brightness;
/*    */   }
/*    */   
/*    */   private static boolean isAimingState(RenderableState renderableState) {
/* 55 */     return (renderableState == RenderableState.ZOOMING || renderableState == RenderableState.ZOOMING_RECOILED || renderableState == RenderableState.ZOOMING_SHOOTING);
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public void update(CompatibleRenderTickEvent event) {
/* 63 */     PlayerWeaponInstance instance = this.modContext.getMainHeldWeapon();
/* 64 */     if (instance != null && instance.isAimed()) {
/* 65 */       ItemScope scope = instance.getScope();
/* 66 */       if (scope.isOptical()) {
/* 67 */         setSize(scope.getWidth(), scope.getHeight());
/*    */       }
/* 69 */       super.update(event);
/*    */     } 
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected void prepareRenderWorld(CompatibleRenderTickEvent event) {
/* 79 */     DynamicShaderContext shaderContext = new DynamicShaderContext(DynamicShaderPhase.POST_WORLD_OPTICAL_SCOPE_RENDER, this.entityRenderer, this.framebuffer, event.getRenderTickTime());
/* 80 */     PlayerWeaponInstance instance = this.modContext.getMainHeldWeapon();
/* 81 */     this.shaderGroupManager.applyShader(shaderContext, (PlayerItemInstance)instance);
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected void postRenderWorld(CompatibleRenderTickEvent event) {
/* 90 */     DynamicShaderContext shaderContext = new DynamicShaderContext(DynamicShaderPhase.POST_WORLD_OPTICAL_SCOPE_RENDER, this.entityRenderer, this.framebuffer, event.getRenderTickTime());
/* 91 */     this.shaderGroupManager.removeStaleShaders(shaderContext);
/*    */   }
/*    */ }


/* Location:              I:\BON\mwmodify-deobf.jar!\com\vicmatskiv\weaponlib\perspective\OpticalScopePerspective.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */