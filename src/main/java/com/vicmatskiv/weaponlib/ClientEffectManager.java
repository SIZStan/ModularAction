 package com.vicmatskiv.weaponlib;
 
 import com.vicmatskiv.weaponlib.compatibility.CompatibilityProvider;
 import com.vicmatskiv.weaponlib.compatibility.CompatibleVec3;
 import com.vicmatskiv.weaponlib.compatibility.Interceptors;
 import com.vicmatskiv.weaponlib.particle.ExplosionParticleFX;
 import com.vicmatskiv.weaponlib.particle.ExplosionSmokeFX;
 import com.vicmatskiv.weaponlib.particle.FlashFX;
 import com.vicmatskiv.weaponlib.particle.SmokeFX;

import indi.ma.client.ClientHandler;
import net.minecraft.client.Minecraft;
 import net.minecraft.client.particle.EntityFX;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
 
 
 
 
 
 final class ClientEffectManager
   implements EffectManager
 {
   public void spawnSmokeParticle(EntityLivingBase player, float xOffset, float yOffset) {
     if (CompatibilityProvider.compatibility.isShadersModEnabled()) {
       return;
     }
     
     double motionX = (CompatibilityProvider.compatibility.world((Entity)player)).rand.nextGaussian() * 0.003D;
     double motionY = (CompatibilityProvider.compatibility.world((Entity)player)).rand.nextGaussian() * 0.003D;
     double motionZ = (CompatibilityProvider.compatibility.world((Entity)player)).rand.nextGaussian() * 0.003D;
     
     CompatibleVec3 look = CompatibilityProvider.compatibility.getLookVec(player);
     float distance = 0.3F;
     float scale = 1.0F * CompatibilityProvider.compatibility.getSmokeEffectScaleFactor();
     float positionRandomizationFactor = 0.01F;
     Vec3 vec3 = Vec3.createVectorHelper(ClientHandler.cameraProbeOffset * -0.6, 0.0, 0.0);
		vec3.rotateAroundY(((float) (((double) (-player.rotationYaw)) * 3.141593 / 180.0)));
		float fixY=(ClientHandler.clientPlayerState.isSitting?-0.52f:0.0f);
		if(ClientHandler.clientPlayerState.isCrawling)
			fixY=-1.22f;
     double posX = player.posX+vec3.xCoord + look.getXCoord() * distance + (((CompatibilityProvider.compatibility.world((Entity)player)).rand.nextFloat() * 2.0F - 1.0F) * positionRandomizationFactor) + -look.getZCoord() * xOffset;
     double posY = player.posY+fixY + look.getYCoord() * distance + (((CompatibilityProvider.compatibility.world((Entity)player)).rand.nextFloat() * 2.0F - 1.0F) * positionRandomizationFactor) - yOffset;
     double posZ = player.posZ+vec3.zCoord + look.getZCoord() * distance + (((CompatibilityProvider.compatibility.world((Entity)player)).rand.nextFloat() * 2.0F - 1.0F) * positionRandomizationFactor) + look.getXCoord() * xOffset;
     
     if (player instanceof EntityPlayer) {
       if (player.isSneaking()) {
         posY -= 0.10000000149011612D;
       } else if (Interceptors.isProning((EntityPlayer)player)) {
         posY -= 1.2000000476837158D;
       } 
     }
 
     
     SmokeFX smokeParticle = new SmokeFX(CompatibilityProvider.compatibility.world((Entity)player), posX, posY, posZ, scale, (float)motionX, (float)motionY, (float)motionZ);
     (Minecraft.getMinecraft()).effectRenderer.addEffect((EntityFX)smokeParticle);
   }
 
 
 
   
   public void spawnFlashParticle(EntityLivingBase player, float flashIntensity, float flashScale, float xOffset, float yOffset, String texture) {
     if (CompatibilityProvider.compatibility.isShadersModEnabled()) {
       return;
     }
     
     float distance = 0.5F;
     
     float scale = 0.8F * CompatibilityProvider.compatibility.getEffectScaleFactor() * flashScale;
     float positionRandomizationFactor = 0.003F;
     
     CompatibleVec3 look = CompatibilityProvider.compatibility.getLookVec(player);
     
     float motionX = (float)(CompatibilityProvider.compatibility.world((Entity)player)).rand.nextGaussian() * 0.003F;
     float motionY = (float)(CompatibilityProvider.compatibility.world((Entity)player)).rand.nextGaussian() * 0.003F;
     float motionZ = (float)(CompatibilityProvider.compatibility.world((Entity)player)).rand.nextGaussian() * 0.003F;
     Vec3 vec3 = Vec3.createVectorHelper(ClientHandler.cameraProbeOffset * -0.6, 0.0, 0.0);
		vec3.rotateAroundY(((float) (((double) (-player.rotationYaw)) * 3.141593 / 180.0)));
	float fixY=(ClientHandler.clientPlayerState.isSitting?-0.52f:0.0f);
	if(ClientHandler.clientPlayerState.isCrawling)
		fixY=-1.22f;
     double posX = player.posX+vec3.xCoord + look.getXCoord() * distance + (((CompatibilityProvider.compatibility.world((Entity)player)).rand.nextFloat() * 2.0F - 1.0F) * positionRandomizationFactor) + -look.getZCoord() * xOffset;
     double posY = player.posY+fixY + look.getYCoord() * distance + (((CompatibilityProvider.compatibility.world((Entity)player)).rand.nextFloat() * 2.0F - 1.0F) * positionRandomizationFactor) - yOffset;
     double posZ = player.posZ+vec3.zCoord + look.getZCoord() * distance + (((CompatibilityProvider.compatibility.world((Entity)player)).rand.nextFloat() * 2.0F - 1.0F) * positionRandomizationFactor) + look.getXCoord() * xOffset;
     
     if (player instanceof EntityPlayer) {
       if (player.isSneaking()) {
         posY -= 0.10000000149011612D;
       } else if (Interceptors.isProning((EntityPlayer)player)) {
         posY -= 1.2000000476837158D;
       } 
     }
 
 
 
 
 
 
     
     FlashFX flashParticle = new FlashFX(CompatibilityProvider.compatibility.world((Entity)player), posX, posY, posZ, scale, flashIntensity * CompatibilityProvider.compatibility.getFlashIntencityFactor(), motionX, motionY, motionZ, texture);
 
 
 
 
     
     (Minecraft.getMinecraft()).effectRenderer.addEffect((EntityFX)flashParticle);
   }
 
 
 
 
 
 
 
   
   public void spawnExplosionSmoke(double posX, double posY, double posZ, double motionX, double motionY, double motionZ, float scale, int maxAge, ExplosionSmokeFX.Behavior behavior, String particleTexture) {
     World world = CompatibilityProvider.compatibility.world((Entity)CompatibilityProvider.compatibility.clientPlayer());
     ExplosionSmokeFX smokeParticle = new ExplosionSmokeFX(world, posX, posY, posZ, scale, (float)motionX, (float)motionY, (float)motionZ, maxAge, ExplosionSmokeFX.Behavior.SMOKE_GRENADE, particleTexture);
 
 
 
 
 
 
 
 
 
 
 
     
     (Minecraft.getMinecraft()).effectRenderer.addEffect((EntityFX)smokeParticle);
   }
 
 
   
   public void spawnExplosionParticle(double posX, double posY, double posZ, double motionX, double motionY, double motionZ, float scale, int maxAge, String particleTexture) {
     World world = CompatibilityProvider.compatibility.world((Entity)CompatibilityProvider.compatibility.clientPlayer());
     ExplosionParticleFX explosionParticle = new ExplosionParticleFX(world, posX, posY, posZ, scale, motionX, motionY, motionZ, maxAge, particleTexture);
 
 
 
 
 
 
 
 
     
     (Minecraft.getMinecraft()).effectRenderer.addEffect((EntityFX)explosionParticle);
   }
 }