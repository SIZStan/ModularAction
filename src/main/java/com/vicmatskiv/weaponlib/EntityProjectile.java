 package com.vicmatskiv.weaponlib;
 
 import com.vicmatskiv.weaponlib.compatibility.CompatibilityProvider;
 import com.vicmatskiv.weaponlib.compatibility.CompatibleAxisAlignedBB;
 import com.vicmatskiv.weaponlib.compatibility.CompatibleBlockState;
 import com.vicmatskiv.weaponlib.compatibility.CompatibleIEntityAdditionalSpawnData;
 import com.vicmatskiv.weaponlib.compatibility.CompatibleMathHelper;
 import com.vicmatskiv.weaponlib.compatibility.CompatibleRayTraceResult;
 import com.vicmatskiv.weaponlib.compatibility.CompatibleRayTracing;
 import com.vicmatskiv.weaponlib.compatibility.CompatibleVec3;

import indi.ma.server.ServerListener;
import io.netty.buffer.ByteBuf;
 import java.util.List;
 import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.IProjectile;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 
 
 
 
 public abstract class EntityProjectile
   extends Entity
   implements IProjectile, CompatibleIEntityAdditionalSpawnData
 {
   private static final Logger logger = LogManager.getLogger(EntityProjectile.class);
   
   private static final String TAG_GRAVITY_VELOCITY = "gravityVelocity";
   
   private static final int MAX_TICKS = 200;
   
   private static final int DEFAULT_MAX_LIFETIME = 5000;
   
   private int xTile = -1;
   private int yTile = -1;
   private int zTile = -1;
   
   protected boolean inGround;
   
   public int throwableShake;
   
   protected EntityLivingBase thrower;
   
   private String throwerName;
   
   private int ticksInAir;
   
   protected float gravityVelocity;
   
   protected float velocity;
   protected float inaccuracy;
   private long timestamp;
   private double aimTan;
   protected long maxLifetime = 5000L;
   
   public EntityProjectile(World world) {
     super(world);
     setSize(0.25F, 0.25F);
     this.timestamp = System.currentTimeMillis();
   }
   
   public EntityProjectile(World world, EntityLivingBase thrower, float velocity, float gravityVelocity, float inaccuracy) {
     this(world);
     this.thrower = thrower;
     this.velocity = velocity;
     this.gravityVelocity = gravityVelocity;
     this.inaccuracy = inaccuracy;
   }
 
 
 
 
 
 
 
 
 
 
 
 
   
   public void setPositionAndDirection() {
	   
	   Vec3 vecRotY = Vec3.createVectorHelper(
				ServerListener.getCameraProbeOffset(Integer.valueOf(thrower.getEntityId())) * -0.6, 0.0, 0.0);
		vecRotY.rotateAroundY(
				((float) (((double) (-thrower.rotationYaw)) * 3.141593 / 180.0)));
		
		if ((thrower instanceof EntityPlayer)
				&& ServerListener.getCameraProbeOffset(Integer.valueOf(thrower.getEntityId())) != 0.0) {
			setLocationAndAngles(thrower.posX + vecRotY.xCoord, thrower.posY + vecRotY.yCoord + thrower.getEyeHeight(),
					thrower.posZ + vecRotY.zCoord,
					CompatibilityProvider.compatibility.getCompatibleAimingRotationYaw(thrower), thrower.rotationPitch);
		} else {
			setLocationAndAngles(thrower.posX, thrower.posY + thrower.getEyeHeight(), thrower.posZ,
					CompatibilityProvider.compatibility.getCompatibleAimingRotationYaw(thrower), thrower.rotationPitch);
		}
//     setLocationAndAngles(this.thrower.posX, this.thrower.posY + this.thrower.getEyeHeight(), this.thrower.posZ, CompatibilityProvider.compatibility
//         .getCompatibleAimingRotationYaw(this.thrower), this.thrower.rotationPitch);
     
     this.posX -= (CompatibleMathHelper.cos(this.rotationYaw / 180.0F * 3.1415927F) * 0.16F);
     this.posY -= 0.10000000149011612D;
     this.posZ -= (CompatibleMathHelper.sin(this.rotationYaw / 180.0F * 3.1415927F) * 0.16F);
     setPosition(this.posX, this.posY, this.posZ);
 
     
     float f = this.velocity;
     this
       .motionX = (-CompatibleMathHelper.sin(this.rotationYaw / 180.0F * 3.1415927F) * CompatibleMathHelper.cos(this.rotationPitch / 180.0F * 3.1415927F) * f);
     this
       .motionZ = (CompatibleMathHelper.cos(this.rotationYaw / 180.0F * 3.1415927F) * CompatibleMathHelper.cos(this.rotationPitch / 180.0F * 3.1415927F) * f);
     this
       .motionY = (-CompatibleMathHelper.sin((this.rotationPitch + getPitchOffset()) / 180.0F * 3.1415927F) * f);
     setThrowableHeading(this.motionX, this.motionY, this.motionZ, this.velocity, this.inaccuracy);
   }
 
   
   public void setPositionAndDirection(double x, double y, double z, float rotationYaw, float rotationPitch) {
     setLocationAndAngles(x, y + this.thrower.getEyeHeight(), z, rotationYaw, rotationPitch);
     
     this.posX -= (CompatibleMathHelper.cos(this.rotationYaw / 180.0F * 3.1415927F) * 0.16F);
     this.posY -= 0.10000000149011612D;
     this.posZ -= (CompatibleMathHelper.sin(this.rotationYaw / 180.0F * 3.1415927F) * 0.16F);
     setPosition(this.posX, this.posY, this.posZ);
 
     
     float f = this.velocity;
     this
       .motionX = (-CompatibleMathHelper.sin(this.rotationYaw / 180.0F * 3.1415927F) * CompatibleMathHelper.cos(this.rotationPitch / 180.0F * 3.1415927F) * f);
     this
       .motionZ = (CompatibleMathHelper.cos(this.rotationYaw / 180.0F * 3.1415927F) * CompatibleMathHelper.cos(this.rotationPitch / 180.0F * 3.1415927F) * f);
     this
       .motionY = (-CompatibleMathHelper.sin((this.rotationPitch + getPitchOffset()) / 180.0F * 3.1415927F) * f);
     setThrowableHeading(this.motionX, this.motionY, this.motionZ, this.velocity, this.inaccuracy);
   }
   
   public EntityProjectile(World world, double posX, double posY, double posZ) {
     super(world);
     
     setSize(0.25F, 0.25F);
     setPosition(posX, posY, posZ);
   }
 
   
   protected float getPitchOffset() {
     return 0.0F;
   }
 
 
 
 
 
   
   public void setThrowableHeading(double x, double y, double z, float velocity, float inaccuracy) {
     float f2 = CompatibleMathHelper.sqrt_double(x * x + y * y + z * z);
     x /= f2;
     y /= f2;
     z /= f2;
     x += this.rand.nextGaussian() * 0.007499999832361937D * inaccuracy;
     y += this.rand.nextGaussian() * 0.007499999832361937D * inaccuracy;
     z += this.rand.nextGaussian() * 0.007499999832361937D * inaccuracy;
     x *= velocity;
     y *= velocity;
     z *= velocity;
     this.motionX = x;
     this.motionY = y;
     this.motionZ = z;
     float f3 = CompatibleMathHelper.sqrt_double(x * x + z * z);
     this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(x, z) * 180.0D / Math.PI);
     this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(y, f3) * 180.0D / Math.PI);
   }
 
 
 
 
 
   
   public void setVelocity(double mX, double mY, double mZ) {
     this.motionX = mX;
     this.motionY = mY;
     this.motionZ = mZ;
     
     if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
       float f = CompatibleMathHelper.sqrt_double(mX * mX + mZ * mZ);
       this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(mX, mZ) * 180.0D / Math.PI);
       this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(mY, f) * 180.0D / Math.PI);
     } 
   }
 
 
 
 
   
   public void onUpdate() {
     if (this.ticksExisted > 200) {
       setDead();
       return;
     } 
     this.lastTickPosX = this.posX;
     this.lastTickPosY = this.posY;
     this.lastTickPosZ = this.posZ;
     super.onUpdate();
     
     if (this.throwableShake > 0) {
       this.throwableShake--;
     }
     
     if (this.inGround) {
 
 
 
 
 
 
 
 
 
 
       
       this.inGround = false;
       this.motionX *= (this.rand.nextFloat() * 0.2F);
       this.motionY *= (this.rand.nextFloat() * 0.2F);
       this.motionZ *= (this.rand.nextFloat() * 0.2F);
       
       this.ticksInAir = 0;
     } else {
       this.ticksInAir++;
     } 
     
     CompatibleVec3 vec3 = new CompatibleVec3(this.posX, this.posY, this.posZ);
     CompatibleVec3 vec31 = new CompatibleVec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
 
     
     CompatibleRayTraceResult movingobjectposition = CompatibleRayTracing.rayTraceBlocks(CompatibilityProvider.compatibility.world(this), vec3, vec31, (block, blockMetadata) -> canCollideWithBlock(block, blockMetadata));
 
 
     
     vec3 = new CompatibleVec3(this.posX, this.posY, this.posZ);
     vec31 = new CompatibleVec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
     
     if (movingobjectposition != null) {
       vec31 = CompatibleVec3.fromCompatibleVec3(movingobjectposition.getHitVec());
     }
     
     if (!(CompatibilityProvider.compatibility.world(this)).isRemote) {
       Entity entity = getRayTraceEntities(vec3, vec31);
       
       if (entity != null) {
         movingobjectposition = new CompatibleRayTraceResult(entity);
       }
     } 
     
     if (movingobjectposition != null) {
       onImpact(movingobjectposition);
     }
     
     this.posX += this.motionX;
     this.posY += this.motionY;
     this.posZ += this.motionZ;
     float f1 = CompatibleMathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
     this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
     
     this.rotationPitch = (float)(Math.atan2(this.motionY, f1) * 180.0D / Math.PI);
     for (; this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F);
 
 
     
     while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
       this.prevRotationPitch += 360.0F;
     }
     
     while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
       this.prevRotationYaw -= 360.0F;
     }
     
     while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
       this.prevRotationYaw += 360.0F;
     }
     
     this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
     this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
     float f2 = 0.99F;
     float f3 = this.gravityVelocity;
     
     if (isInWater()) {
       for (int i = 0; i < 4; i++) {
         float f4 = 0.25F;
         CompatibilityProvider.compatibility.spawnParticle(CompatibilityProvider.compatibility.world(this), "bubble", this.posX - this.motionX * f4, this.posY - this.motionY * f4, this.posZ - this.motionZ * f4, this.motionX, this.motionY, this.motionZ);
       } 
 
 
       
       f2 = 0.8F;
     } 
     
     this.motionX *= f2;
     this.motionY *= f2;
     this.motionZ *= f2;
     this.motionY -= f3;
     setPosition(this.posX, this.posY, this.posZ);
   }
   
   private Entity getRayTraceEntities(CompatibleVec3 vec3, CompatibleVec3 vec31) {
     Entity entity = null;
     List<?> list = CompatibilityProvider.compatibility.getEntitiesWithinAABBExcludingEntity(CompatibilityProvider.compatibility.world(this), this, CompatibilityProvider.compatibility
         .getBoundingBox(this).addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
     
     double d0 = 0.0D;
     EntityLivingBase entitylivingbase = getThrower();
     
     for (int j = 0; j < list.size(); j++) {
       Entity entity1 = (Entity)list.get(j);
       
       if (entity1.canBeCollidedWith() && (entity1 != entitylivingbase || this.ticksInAir >= 5)) {
         float f = 0.3F;
         CompatibleAxisAlignedBB axisalignedbb = CompatibilityProvider.compatibility.expandEntityBoundingBox(entity1, f, f, f);
         
         CompatibleRayTraceResult movingobjectposition1 = axisalignedbb.calculateIntercept(vec3, vec31);
         
         if (movingobjectposition1 != null) {
           double d1 = vec3.distanceTo(movingobjectposition1.getHitVec());
           
           if (d1 < d0 || d0 == 0.0D) {
             entity = entity1;
             d0 = d1;
           } 
         } 
       } 
     } 
     return entity;
   }
 
 
 
 
 
 
 
 
   
   public void writeEntityToNBT(NBTTagCompound tagCompound) {
     tagCompound.setLong("timestamp", this.timestamp);
     tagCompound.setShort("xTile", (short)this.xTile);
     tagCompound.setShort("yTile", (short)this.yTile);
     tagCompound.setShort("zTile", (short)this.zTile);
     
     tagCompound.setByte("shake", (byte)this.throwableShake);
     tagCompound.setByte("inGround", (byte)(this.inGround ? 1 : 0));
     
     if ((this.throwerName == null || this.throwerName.length() == 0) && this.thrower != null && this.thrower instanceof EntityPlayer)
     {
       this.throwerName = CompatibilityProvider.compatibility.getPlayerName((EntityPlayer)this.thrower);
     }
     
     tagCompound.setString("ownerName", (this.throwerName == null) ? "" : this.throwerName);
     tagCompound.setFloat("gravityVelocity", this.gravityVelocity);
     tagCompound.setDouble("aimTan", this.aimTan);
   }
 
 
 
 
   
   public void readEntityFromNBT(NBTTagCompound tagCompound) {
     this.xTile = tagCompound.getShort("xTile");
     this.yTile = tagCompound.getShort("yTile");
     this.zTile = tagCompound.getShort("zTile");
     
     this.throwableShake = tagCompound.getByte("shake") & 0xFF;
     this.inGround = (tagCompound.getByte("inGround") == 1);
     this.throwerName = tagCompound.getString("ownerName");
     
     if (this.throwerName != null && this.throwerName.length() == 0) {
       this.throwerName = null;
     }
     this.gravityVelocity = tagCompound.getFloat("gravityVelocity");
     this.timestamp = tagCompound.getLong("timestamp");
     this.aimTan = tagCompound.getDouble("aimTan");
     
     if (System.currentTimeMillis() > this.timestamp + this.maxLifetime) {
       setDead();
     }
   }
 
   
   public void writeSpawnData(ByteBuf buffer) {
     buffer.writeFloat(this.gravityVelocity);
     buffer.writeDouble(this.aimTan);
   }
 
   
   public void readSpawnData(ByteBuf buffer) {
     this.gravityVelocity = buffer.readFloat();
     this.aimTan = buffer.readDouble();
   }
 
   
   public float getShadowSize() {
     return 0.0F;
   }
   
   public double getAimTan() {
     return this.aimTan;
   }
   
   public EntityLivingBase getThrower() {
     if (this.thrower == null && this.throwerName != null && this.throwerName.length() > 0) {
       this.thrower = (EntityLivingBase)CompatibilityProvider.compatibility.world(this).getPlayerEntityByName(this.throwerName);
     }
     
     return this.thrower;
   }
 
 
 
 
   
   protected void entityInit() {}
 
 
 
 
   
   public boolean isInRangeToRenderDist(double p_70112_1_) {
     double d1 = CompatibilityProvider.compatibility.getBoundingBox(this).getAverageEdgeLength() * 4.0D;
     d1 *= 64.0D;
     return (p_70112_1_ < d1 * d1);
   }
   
   public boolean canCollideWithBlock(Block block, CompatibleBlockState metadata) {
     return CompatibilityProvider.compatibility.canCollideCheck(block, metadata, false);
   }
   
   protected abstract void onImpact(CompatibleRayTraceResult paramCompatibleRayTraceResult);
 }