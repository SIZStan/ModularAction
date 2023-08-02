/*    */ package com.landb.mwoverhaul;
/*    */ 
/*    */ import com.landb.mwoverhaul.client.ClientConfiguration;
/*    */ import com.landb.mwoverhaul.client.PredicateKeybinding;
/*    */ import com.landb.mwoverhaul.client.render.InvisibleRender;
/*    */ import com.landb.mwoverhaul.client.render.RenderAirdrop;
/*    */ import com.landb.mwoverhaul.client.render.RenderBlockHandler;
/*    */ import com.landb.mwoverhaul.client.render.RenderCorpse;
/*    */ import com.landb.mwoverhaul.client.render.RenderGroundItem;
/*    */ import com.landb.mwoverhaul.client.render.RenderZombie;
/*    */ import com.landb.mwoverhaul.entity.EntityAirdrop;
/*    */ import com.landb.mwoverhaul.entity.EntityDeadBody;
/*    */ import com.landb.mwoverhaul.entity.EntityGroundItem;
/*    */ import com.landb.mwoverhaul.entity.EntityHead;
/*    */ import com.landb.mwoverhaul.entity.EntityMWOZombie;
/*    */ import com.landb.mwoverhaul.listener.ClientKeyListener;
/*    */ import com.landb.mwoverhaul.listener.ClientListener;
/*    */ import cpw.mods.fml.client.registry.RenderingRegistry;
/*    */ import net.minecraft.client.model.ModelBiped;
/*    */ import net.minecraft.client.renderer.entity.Render;
/*    */ import org.lwjgl.opengl.Display;
/*    */ 
/*    */ 
/*    */ 
/*    */ public class ClientManager
/*    */ {
/*    */   public static ClientListener listener;
/*    */   private static ClientKeyListener keyListener;
/*    */   private static RenderBlockHandler renderBlockHandler;
/*    */   
/*    */   public static void init() {
/* 32 */     Display.setTitle(Display.getTitle() + " 丨 幸存者II 丨 官方Q群: 966237931" + " 丨 MineMc工作室 丨 服主Q: 513960309" + " ");
/*    */ 
/*    */ 
/*    */     
/* 36 */     listener = new ClientListener();
/* 37 */     keyListener = new ClientKeyListener();
/* 38 */     registerEntityRenderers();
/*    */     
/* 40 */     ClientConfiguration.loadConfigs();
/* 41 */     //PredicateKeybinding.disableUnwantedKeys();
/*    */   }
/*    */   
/*    */   public static RenderBlockHandler getRenderBlockHandler() {
/* 45 */     if (renderBlockHandler == null)
/* 46 */       renderBlockHandler = new RenderBlockHandler(); 
/* 47 */     return renderBlockHandler;
/*    */   }
/*    */   
/*    */   private static void registerEntityRenderers() {
/* 51 */     RenderingRegistry.registerEntityRenderingHandler(EntityMWOZombie.class, (Render)new RenderZombie(new ModelBiped(), 0.0F));
/* 52 */     RenderingRegistry.registerEntityRenderingHandler(EntityHead.class, (Render)new InvisibleRender());
/* 53 */     RenderingRegistry.registerEntityRenderingHandler(EntityGroundItem.class, (Render)new RenderGroundItem());
/* 54 */     RenderingRegistry.registerEntityRenderingHandler(EntityDeadBody.class, (Render)new RenderCorpse());
/* 55 */     RenderingRegistry.registerEntityRenderingHandler(EntityAirdrop.class, (Render)new RenderAirdrop());
/*    */   }
/*    */ 
/*    */   
/*    */   public static void loadComplete() {}
/*    */ 
/*    */   
/*    */   public static ClientKeyListener getKeyListener() {
/* 63 */     return keyListener;
/*    */   }
/*    */ }


/* Location:              I:\BON\维克附属-deobf.jar!\com\landb\mwoverhaul\ClientManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */