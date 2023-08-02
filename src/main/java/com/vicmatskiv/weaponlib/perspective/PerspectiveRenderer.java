/*     */ package com.vicmatskiv.weaponlib.perspective;
/*     */ 
/*     */ import com.vicmatskiv.weaponlib.ClientModContext;
/*     */ import com.vicmatskiv.weaponlib.CustomRenderer;
/*     */ import com.vicmatskiv.weaponlib.RenderContext;
/*     */ import com.vicmatskiv.weaponlib.RenderableState;
/*     */ import com.vicmatskiv.weaponlib.ViewfinderModel;
/*     */ import com.vicmatskiv.weaponlib.compatibility.CompatibilityProvider;
/*     */ import com.vicmatskiv.weaponlib.compatibility.CompatibleRenderTickEvent;
/*     */ import com.vicmatskiv.weaponlib.compatibility.CompatibleTransformType;
/*     */ import java.util.function.BiConsumer;
/*     */ import net.minecraft.client.Minecraft;
/*     */ import net.minecraft.client.renderer.texture.ITextureObject;
/*     */ import net.minecraft.entity.Entity;
/*     */ import net.minecraft.entity.EntityLivingBase;
/*     */ import net.minecraft.item.ItemStack;
/*     */ import net.minecraft.util.ResourceLocation;
/*     */ import org.lwjgl.opengl.GL11;
/*     */ 
/*     */ 
/*     */ public class PerspectiveRenderer
/*     */   implements CustomRenderer<RenderableState>
/*     */ {
/*     */   private static class StaticTexturePerspective
/*     */     extends Perspective<RenderableState>
/*     */   {
/*     */     private Integer textureId;
/*     */     
/*     */     private StaticTexturePerspective() {}
/*     */     
/*     */     public void update(CompatibleRenderTickEvent event) {}
/*     */     
/*     */     public int getTexture(RenderContext<RenderableState> context) {
/*  34 */       if (this.textureId == null) {
/*  35 */         ResourceLocation textureResource = new ResourceLocation("weaponlib:/com/vicmatskiv/weaponlib/resources/dark-screen.png");
/*  36 */         Minecraft.getMinecraft().getTextureManager().bindTexture(textureResource);
/*  37 */         ITextureObject textureObject = Minecraft.getMinecraft().getTextureManager().getTexture(textureResource);
/*  38 */         if (textureObject != null) {
/*  39 */           this.textureId = Integer.valueOf(textureObject.getGlTextureId());
/*     */         }
/*     */       } 
/*     */       
/*  43 */       return this.textureId.intValue();
/*     */     }
/*     */ 
/*     */     
/*     */     public float getBrightness(RenderContext<RenderableState> context) {
/*  48 */       return 0.0F;
/*     */     }
/*     */   }
/*     */   
/*  52 */   private static Perspective<RenderableState> STATIC_TEXTURE_PERSPECTIVE = new StaticTexturePerspective();
/*     */   
/*  54 */   private ViewfinderModel model = new ViewfinderModel();
/*     */   
/*     */   private BiConsumer<EntityLivingBase, ItemStack> positioning;
/*     */   
/*     */   public PerspectiveRenderer(BiConsumer<EntityLivingBase, ItemStack> positioning) {
/*  59 */     this.positioning = positioning;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void render(RenderContext<RenderableState> renderContext) {
/*  65 */     if (renderContext.getCompatibleTransformType() != CompatibleTransformType.FIRST_PERSON_RIGHT_HAND && renderContext
/*  66 */       .getCompatibleTransformType() != CompatibleTransformType.FIRST_PERSON_LEFT_HAND) {
/*     */       return;
/*     */     }
/*     */     
/*  70 */     if (renderContext.getModContext() == null) {
/*     */       return;
/*     */     }
/*     */     
/*  74 */     ClientModContext clientModContext = (ClientModContext)renderContext.getModContext();
/*     */ 
/*     */ 
/*     */     
/*  78 */     Perspective<RenderableState> perspective = (Perspective)clientModContext.getViewManager().getPerspective(renderContext.getPlayerItemInstance(), false);
/*  79 */     if (perspective == null) {
/*  80 */       perspective = STATIC_TEXTURE_PERSPECTIVE;
/*     */     }
/*     */     
/*  83 */     float brightness = perspective.getBrightness(renderContext);
/*  84 */     GL11.glPushMatrix();
/*  85 */     GL11.glPushAttrib(8193);
/*     */     
/*  87 */     this.positioning.accept(renderContext.getPlayer(), renderContext.getWeapon());
/*     */     
/*  89 */     GL11.glBindTexture(3553, perspective.getTexture(renderContext));
/*  90 */     CompatibilityProvider.compatibility.disableLightMap();
/*  91 */     GL11.glEnable(2929);
/*     */     
/*  93 */     GL11.glDisable(2896);
/*  94 */     GL11.glDisable(3008);
/*  95 */     GL11.glDisable(3042);
/*     */     
/*  97 */     GL11.glColor4f(brightness, brightness, brightness, 1.0F);
/*  98 */     this.model.render((Entity)renderContext.getPlayer(), renderContext
/*  99 */         .getLimbSwing(), renderContext
/* 100 */         .getFlimbSwingAmount(), renderContext
/* 101 */         .getAgeInTicks(), renderContext
/* 102 */         .getNetHeadYaw(), renderContext
/* 103 */         .getHeadPitch(), renderContext
/* 104 */         .getScale());
/*     */     
/* 106 */     CompatibilityProvider.compatibility.enableLightMap();
/* 107 */     GL11.glPopAttrib();
/* 108 */     GL11.glPopMatrix();
/*     */   }
/*     */ }


/* Location:              I:\BON\mwmodify-deobf.jar!\com\vicmatskiv\weaponlib\perspective\PerspectiveRenderer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */