/*     */ package com.vicmatskiv.weaponlib;
/*     */ 
/*     */ import com.vicmatskiv.weaponlib.animation.DebugPositioner;
/*     */ import com.vicmatskiv.weaponlib.compatibility.CompatibilityProvider;
/*     */ import net.minecraft.client.settings.KeyBinding;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class KeyBindings
/*     */ {
/*     */   public static KeyBinding reloadKey;
/*     */   public static KeyBinding unloadKey;
/*     */   public static KeyBinding inspectKey;
/*     */   public static KeyBinding attachmentKey;
/*     */   public static KeyBinding upArrowKey;
/*     */   public static KeyBinding downArrowKey;
/*     */   public static KeyBinding leftArrowKey;
/*     */   public static KeyBinding rightArrowKey;
/*     */   public static KeyBinding laserSwitchKey;
/*     */   public static KeyBinding nightVisionSwitchKey;
/*     */   public static KeyBinding proningSwitchKey;
/*     */   public static KeyBinding laserAttachmentKey;
/*     */   public static KeyBinding periodKey;
/*     */   public static KeyBinding addKey;
/*     */   public static KeyBinding subtractKey;
/*     */   public static KeyBinding fireModeKey;
/*     */   public static KeyBinding customInventoryKey;
/*     */   public static KeyBinding jDebugKey;
/*     */   public static KeyBinding kDebugKey;
/*     */   public static KeyBinding minusDebugKey;
/*     */   public static KeyBinding equalsDebugKey;
/*     */   public static KeyBinding lBracketDebugKey;
/*     */   public static KeyBinding rBracketDebugKey;
/*     */   public static KeyBinding semicolonDebugKey;
/*     */   public static KeyBinding apostropheDebugKey;
/*     */   public static KeyBinding deleteDebugKey;
/*     */   
/*     */   public static void init() {
/*  51 */     reloadKey = new KeyBinding("key.reload", 19, "key.categories.weaponlib");
/*     */ 
/*     */     
/*  54 */     unloadKey = new KeyBinding("key.unload", 22, "key.categories.weaponlib");
/*     */ 
/*     */     
/*  57 */     inspectKey = new KeyBinding("key.inspect", 25, "key.categories.weaponlib");
/*     */ 
/*     */     
/*  60 */     laserSwitchKey = new KeyBinding("key.laser", 38, "key.categories.weaponlib");
/*     */ 
/*     */     
/*  63 */     nightVisionSwitchKey = new KeyBinding("key.nightVision", 49, "key.categories.weaponlib");
/*     */ 
/*     */     
/*  66 */     attachmentKey = new KeyBinding("key.attachment", 50, "key.categories.weaponlib");
/*     */ 
/*     */     
/*  69 */     upArrowKey = new KeyBinding("key.scope", 200, "key.categories.weaponlib");
/*     */ 
/*     */     
/*  72 */     downArrowKey = new KeyBinding("key.recoil_fitter", 208, "key.categories.weaponlib");
/*     */ 
/*     */     
/*  75 */     leftArrowKey = new KeyBinding("key.silencer", 203, "key.categories.weaponlib");
/*     */ 
/*     */     
/*  78 */     rightArrowKey = new KeyBinding("key.texture_change", 205, "key.categories.weaponlib");
/*     */ 
/*     */     
/*  81 */     addKey = new KeyBinding("key.add", 23, "key.categories.weaponlib");
/*     */ 
/*     */     
/*  84 */     subtractKey = new KeyBinding("key.subtract", 24, "key.categories.weaponlib");
/*     */ 
/*     */     
/*  87 */     fireModeKey = new KeyBinding("key.fire_mode", 48, "key.categories.weaponlib");
/*     */ 
/*     */     
/*  90 */     proningSwitchKey = new KeyBinding("key.proning", 44, "key.categories.weaponlib");
/*     */ 
/*     */     
/*  93 */     laserAttachmentKey = new KeyBinding("key.attach_laser", 54, "key.categories.weaponlib");
/*     */ 
/*     */     
/*  96 */     customInventoryKey = new KeyBinding("key.custom_inventory", 45, "key.categories.weaponlib");
/*     */ 
/*     */     
/*  99 */     periodKey = new KeyBinding("key.sight", 52, "key.categories.weaponlib");
/*     */ 
/*     */     
/* 102 */     CompatibilityProvider.compatibility.registerKeyBinding(reloadKey);
/* 103 */     CompatibilityProvider.compatibility.registerKeyBinding(unloadKey);
/* 104 */     CompatibilityProvider.compatibility.registerKeyBinding(inspectKey);
/* 105 */     CompatibilityProvider.compatibility.registerKeyBinding(attachmentKey);
/* 106 */     CompatibilityProvider.compatibility.registerKeyBinding(upArrowKey);
/* 107 */     CompatibilityProvider.compatibility.registerKeyBinding(downArrowKey);
/* 108 */     CompatibilityProvider.compatibility.registerKeyBinding(leftArrowKey);
/* 109 */     CompatibilityProvider.compatibility.registerKeyBinding(rightArrowKey);
/* 110 */     CompatibilityProvider.compatibility.registerKeyBinding(laserSwitchKey);
/* 111 */     CompatibilityProvider.compatibility.registerKeyBinding(nightVisionSwitchKey);
/* 112 */     CompatibilityProvider.compatibility.registerKeyBinding(addKey);
/* 113 */     CompatibilityProvider.compatibility.registerKeyBinding(subtractKey);
/* 114 */     CompatibilityProvider.compatibility.registerKeyBinding(fireModeKey);
/* 115 */     CompatibilityProvider.compatibility.registerKeyBinding(proningSwitchKey);
/* 116 */     CompatibilityProvider.compatibility.registerKeyBinding(laserAttachmentKey);
/* 117 */     CompatibilityProvider.compatibility.registerKeyBinding(periodKey);
/* 118 */     CompatibilityProvider.compatibility.registerKeyBinding(customInventoryKey);
/*     */     
/* 120 */     if (DebugPositioner.isDebugModeEnabled()) {
/* 121 */       bindDebugKeys();
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   public static void bindDebugKeys() {
/* 127 */     jDebugKey = new KeyBinding("key.jDebugKey", 36, "key.categories.weaponlib");
/*     */ 
/*     */     
/* 130 */     kDebugKey = new KeyBinding("key.klDebugKey", 37, "key.categories.weaponlib");
/*     */ 
/*     */     
/* 133 */     minusDebugKey = new KeyBinding("key.minusDebugKey", 12, "key.categories.weaponlib");
/*     */ 
/*     */     
/* 136 */     equalsDebugKey = new KeyBinding("key.equalsDebugKey", 13, "key.categories.weaponlib");
/*     */ 
/*     */     
/* 139 */     lBracketDebugKey = new KeyBinding("key.lBracketDebugKey", 26, "key.categories.weaponlib");
/*     */ 
/*     */     
/* 142 */     rBracketDebugKey = new KeyBinding("key.rBracketDebugKey", 27, "key.categories.weaponlib");
/*     */ 
/*     */     
/* 145 */     semicolonDebugKey = new KeyBinding("key.semicolonDebugKey", 39, "key.categories.weaponlib");
/*     */ 
/*     */     
/* 148 */     apostropheDebugKey = new KeyBinding("key.apostropheDebugKey", 40, "key.categories.weaponlib");
/*     */ 
/*     */     
/* 151 */     deleteDebugKey = new KeyBinding("key.deleteDebugKey", 14, "key.categories.weaponlib");
/*     */ 
/*     */     
/* 154 */     CompatibilityProvider.compatibility.registerKeyBinding(jDebugKey);
/* 155 */     CompatibilityProvider.compatibility.registerKeyBinding(kDebugKey);
/*     */     
/* 157 */     CompatibilityProvider.compatibility.registerKeyBinding(lBracketDebugKey);
/* 158 */     CompatibilityProvider.compatibility.registerKeyBinding(rBracketDebugKey);
/*     */     
/* 160 */     CompatibilityProvider.compatibility.registerKeyBinding(semicolonDebugKey);
/* 161 */     CompatibilityProvider.compatibility.registerKeyBinding(apostropheDebugKey);
/*     */     
/* 163 */     CompatibilityProvider.compatibility.registerKeyBinding(minusDebugKey);
/* 164 */     CompatibilityProvider.compatibility.registerKeyBinding(equalsDebugKey);
/*     */     
/* 166 */     CompatibilityProvider.compatibility.registerKeyBinding(deleteDebugKey);
/*     */   }
/*     */ }


/* Location:              I:\BON\mwmodify-deobf.jar!\com\vicmatskiv\weaponlib\KeyBindings.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */