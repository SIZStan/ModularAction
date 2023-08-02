/*     */ package com.landb.mwoverhaul.core.mechanics;
/*     */ 
/*     */ import com.landb.mwoverhaul.MWOverhaul;
/*     */ import com.landb.mwoverhaul.network.CPacketTriggerGun;
/*     */ import com.landb.mwoverhaul.util.MinecraftUtils;
/*     */ import com.landb.mwoverhaul.util.Raytracer;
/*     */ import com.vicmatskiv.mw.ModernWarfareMod;
/*     */ import com.vicmatskiv.weaponlib.AttachmentCategory;
/*     */ import com.vicmatskiv.weaponlib.HookUtils;
/*     */ import com.vicmatskiv.weaponlib.PlayerWeaponInstance;
/*     */ import com.vicmatskiv.weaponlib.Weapon;
/*     */ import cpw.mods.fml.common.network.simpleimpl.IMessage;
import indi.ma.client.ClientHandler;
/*     */ import net.minecraft.block.Block;
/*     */ import net.minecraft.client.Minecraft;
/*     */ import net.minecraft.client.entity.EntityClientPlayerMP;
/*     */ import net.minecraft.entity.EntityLivingBase;
/*     */ import net.minecraft.entity.player.EntityPlayer;
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
/*     */ public class MechSpread
/*     */   implements QuickTickMechanic
/*     */ {
/*     */   private double appliedSpread;
/*     */   private double walkingFactor;
/*     */   
/*     */   public void onTick(long tickCount) {
/*  36 */     EntityClientPlayerMP entityClientPlayerMP = (Minecraft.getMinecraft()).thePlayer;
/*  37 */     if (entityClientPlayerMP == null) {
/*     */       return;
/*     */     }
/*     */     
/*  41 */     if (Math.max(Math.abs(((EntityPlayer)entityClientPlayerMP).motionX), Math.abs(((EntityPlayer)entityClientPlayerMP).motionZ)) > 0.065D) {
/*  42 */       this.walkingFactor += 0.05D;
/*  43 */       if (this.walkingFactor > 2.0D) {
/*  44 */         this.walkingFactor = 2.0D;
/*     */       }
/*     */     } else {
/*  47 */       this.walkingFactor -= 0.2D;
/*  48 */       if (this.walkingFactor < 0.0D) {
/*  49 */         this.walkingFactor = 0.0D;
/*     */       }
/*     */     } 
/*     */     
/*  53 */     Block blockAtPlayer = ((EntityPlayer)entityClientPlayerMP).worldObj.getBlock((int)((EntityPlayer)entityClientPlayerMP).posX, (int)((EntityPlayer)entityClientPlayerMP).posY, (int)((EntityPlayer)entityClientPlayerMP).posZ);
/*  54 */     boolean onLadder = MinecraftUtils.isLivingOnLadder(blockAtPlayer, ((EntityPlayer)entityClientPlayerMP).worldObj, (EntityLivingBase)entityClientPlayerMP);
/*     */     
/*  56 */     if (this.appliedSpread > 0.0D) {
/*  57 */       if (((EntityPlayer)entityClientPlayerMP).onGround || ((EntityPlayer)entityClientPlayerMP).ridingEntity != null) {
/*  58 */         this.appliedSpread -= 0.6D;
/*  59 */         if (entityClientPlayerMP.isSneaking()||ClientHandler.clientPlayerState.isCrawling||ClientHandler.clientPlayerState.isSitting) {
/*  60 */           this.appliedSpread -= 0.5D;
/*     */         }
/*  62 */       } else if (onLadder) {
/*  63 */         this.appliedSpread -= 0.3D;
/*     */       } 
/*     */     }
/*     */     
/*  67 */     if (this.appliedSpread < 0.0D) {
/*  68 */       this.appliedSpread = 0.0D;
/*     */     }
/*     */     
/*  71 */     if (!((EntityPlayer)entityClientPlayerMP).onGround && !onLadder && ((EntityPlayer)entityClientPlayerMP).ridingEntity == null) {
/*  72 */       applySpread(0.35D);
/*     */     }
/*  74 */     if (entityClientPlayerMP.isSprinting()) {
/*  75 */       applySpread(0.9D);
/*     */     }
/*     */   }
/*     */   
/*     */   public void fireBullet(PlayerWeaponInstance instance) {
/*  80 */     MWOverhaul.getNetworkHandler().getWrapper().sendToServer((IMessage)new CPacketTriggerGun(instance.getUuid()));
/*     */     
/*  82 */     applySpread(instance);
/*     */     
/*  84 */     for (int i = 0; i < HookUtils.getPelletsAmount(instance.getWeapon()); i++) {
/*     */       try {
/*  86 */         (new Raytracer()).spawnBullet((EntityPlayer)instance.getPlayer());
/*  87 */       } catch (Exception e) {
/*  88 */         e.printStackTrace();
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   public void applySpread(double amount) {
/*  94 */     if (amount <= 0.0D) {
/*  95 */       throw new IllegalArgumentException("异常扩散 owo");
/*     */     }
/*  97 */     this.appliedSpread += amount;
/*  98 */     if (this.appliedSpread >= 35.0D) {
/*  99 */       this.appliedSpread = 35.0D;
/*     */     }
/*     */   }
/*     */   
/*     */   public void applySpread(PlayerWeaponInstance instance) {
/* 104 */     Weapon playerWeapon = instance.getWeapon();
/*     */     
/* 106 */     float factor = 1.0F;
/* 107 */     float recoil = playerWeapon.getRecoil() / 2.0F + playerWeapon.getInaccuracy() / 2.0F;
/* 108 */     boolean isAiming = HookUtils.isAiming();
/*     */ 
/*     */     
/* 111 */     if (HookUtils.isShotgun(playerWeapon) && 
/* 112 */       isAiming) {
/* 113 */       isAiming = false;
/* 114 */       factor -= 0.2F;
/*     */     } 
/*     */ 
/*     */     
/* 118 */     if (instance.getPlayer().isSneaking()||ClientHandler.clientPlayerState.isCrawling||ClientHandler.clientPlayerState.isSitting) {
/* 119 */       factor -= 0.1F;
/*     */     }
/* 121 */     if (HookUtils.hasAttachmentType((EntityPlayer)instance.getPlayer(), AttachmentCategory.GRIP)) {
/* 122 */       factor -= 0.2F;
/*     */     }
/* 124 */     if (factor < 0.0F) {
/* 125 */       factor = 0.0F;
/*     */     }
/*     */     
/* 128 */     applySpread((recoil * 4.0F * factor));
/*     */   }
/*     */   
/*     */   public double getFullSpread() {
/* 132 */     PlayerWeaponInstance instance = ModernWarfareMod.MOD_CONTEXT.getMainHeldWeapon();
/* 133 */     float firstSpread = 0.0F;
/* 134 */     float laserSpread = 1.0F;
/* 135 */     float crosshairInaccuracy = 0.0F;
/* 136 */     double walkFactor = this.walkingFactor;
/*     */     
/* 138 */     if (instance != null) {
/* 139 */       firstSpread = instance.getWeapon().getInaccuracy() / 1.5F;
/* 140 */       if (HookUtils.isLaserActive()) {
/* 141 */         laserSpread = 0.0F;
/* 142 */         walkFactor = 0.0D;
/*     */       } 
/* 144 */       if (!HookUtils.canHaveCrosshair(instance)) {
/* 145 */         crosshairInaccuracy = 20.0F;
/*     */       }
/* 147 */       if (HookUtils.isShotgun(instance.getWeapon())) {
/* 148 */         firstSpread += instance.getWeapon().getSpawnEntityDamage() * 2.0F;
/*     */       }
/*     */     } 
/*     */     
/* 152 */     return (crosshairInaccuracy + firstSpread * laserSpread) + this.appliedSpread + walkFactor;
/*     */   }
/*     */ }