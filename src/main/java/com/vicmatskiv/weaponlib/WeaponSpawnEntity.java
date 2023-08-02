 package com.vicmatskiv.weaponlib;
 
 import com.vicmatskiv.weaponlib.compatibility.CompatibilityProvider;
 import com.vicmatskiv.weaponlib.compatibility.CompatibleBlockState;
 import com.vicmatskiv.weaponlib.compatibility.CompatibleMessage;
 import com.vicmatskiv.weaponlib.compatibility.CompatibleRayTraceResult;
 import com.vicmatskiv.weaponlib.compatibility.CompatibleTargetPoint;
 import com.vicmatskiv.weaponlib.config.Projectiles;
 import com.vicmatskiv.weaponlib.particle.SpawnParticleMessage;
 import io.netty.buffer.ByteBuf;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.item.Item;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.DamageSource;
 import net.minecraft.world.World;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 
 
 public class WeaponSpawnEntity
   extends EntityProjectile
 {
   private static final Logger logger = LogManager.getLogger(WeaponSpawnEntity.class);
   
   private static final String TAG_ENTITY_ITEM = "entityItem";
   
   private static final String TAG_DAMAGE = "damage";
   private static final String TAG_EXPLOSION_RADIUS = "explosionRadius";
   private static final String TAG_EXPLOSION_IS_DESTROYING_BLOCKS = "destroyBlocks";
   private static final String TAG_EXPLOSION_PARTICLE_AGE_COEFFICIENT = "epac";
   private static final String TAG_SMOKE_PARTICLE_AGE_COEFFICIENT = "spac";
   private static final String TAG_EXPLOSION_PARTICLE_SCALE_COEFFICIENT = "epsc";
   private static final String TAG_SMOKE_PARTICLE_SCALE_COEFFICIENT = "spsc";
   private static final String TAG_EXPLOSION_PARTICLE_TEXTURE_ID = "epti";
   private static final String TAG_SMOKE_PARTICLE_TEXTURE_ID = "spti";
   private float explosionRadius;
   private float damage = 6.0F;
   private boolean isDestroyingBlocks;
   private float explosionParticleAgeCoefficient;
   private float smokeParticleAgeCoefficient;
   private float explosionParticleScaleCoefficient;
   private float smokeParticleScaleCoefficient;
   private Weapon weapon;
   private int explosionParticleTextureId;
   private int smokeParticleTextureId;
   
   public WeaponSpawnEntity(World world) {
     super(world);
   }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
   
   public WeaponSpawnEntity(Weapon weapon, World world, EntityLivingBase player, float speed, float gravityVelocity, float inaccuracy, float damage, float explosionRadius, boolean isDestroyingBlocks, float explosionParticleAgeCoefficient, float smokeParticleAgeCoefficient, float explosionParticleScaleCoefficient, float smokeParticleScaleCoefficient, int explosionParticleTextureId, int smokeParticleTextureId, Material... damageableBlockMaterials) {
     super(world, player, speed, gravityVelocity, inaccuracy);
     this.weapon = weapon;
     this.damage = damage;
     this.explosionRadius = explosionRadius;
     this.isDestroyingBlocks = isDestroyingBlocks;
     this.explosionParticleAgeCoefficient = explosionParticleAgeCoefficient;
     this.smokeParticleAgeCoefficient = smokeParticleAgeCoefficient;
     this.explosionParticleScaleCoefficient = explosionParticleScaleCoefficient;
     this.smokeParticleScaleCoefficient = smokeParticleScaleCoefficient;
     this.explosionParticleTextureId = explosionParticleTextureId;
     this.smokeParticleTextureId = smokeParticleTextureId;
   }
 
   
   public void onUpdate() {
     super.onUpdate();
   }
 
 
 
 
 
   
   protected void onImpact(CompatibleRayTraceResult position) {
     if ((CompatibilityProvider.compatibility.world(this)).isRemote) {
       return;
     }
     
     if (this.weapon == null) {
       return;
     }
     
     if (this.explosionRadius > 0.0F) {
       Explosion.createServerSideExplosion(this.weapon.getModContext(), CompatibilityProvider.compatibility.world(this), this, position
           .getHitVec().getXCoord(), position.getHitVec().getYCoord(), position.getHitVec().getZCoord(), this.explosionRadius, false, true, this.isDestroyingBlocks, this.explosionParticleAgeCoefficient, this.smokeParticleAgeCoefficient, this.explosionParticleScaleCoefficient, this.smokeParticleScaleCoefficient, this.weapon
 
           
           .getModContext().getRegisteredTexture(this.explosionParticleTextureId), this.weapon
           .getModContext().getRegisteredTexture(this.smokeParticleTextureId));
     } else if (position.getEntityHit() != null) {
       
       Projectiles projectilesConfig = this.weapon.getModContext().getConfigurationManager().getProjectiles();
       
       if (getThrower() != null && (projectilesConfig
         .isKnockbackOnHit() == null || projectilesConfig.isKnockbackOnHit().booleanValue())) {
         position.getEntityHit().attackEntityFrom(DamageSource.causeThrownDamage(this, (Entity)getThrower()), this.damage);
       } else if (getThrower() instanceof EntityLivingBase && !(getThrower() instanceof net.minecraft.entity.player.EntityPlayer)) {
         position.getEntityHit().attackEntityFrom(CompatibilityProvider.compatibility.mobDamageSource(getThrower()), this.damage);
       } else {
         position.getEntityHit().attackEntityFrom(CompatibilityProvider.compatibility.genericDamageSource(), this.damage);
       } 
       
       (position.getEntityHit()).hurtResistantTime = 0;
       (position.getEntityHit()).prevRotationYaw = (float)((position.getEntityHit()).prevRotationYaw - 0.3D);
       
       logger.debug("Hit entity {}", new Object[] { position.getEntityHit() });
       
       CompatibleTargetPoint point = new CompatibleTargetPoint((position.getEntityHit()).dimension, this.posX, this.posY, this.posZ, 100.0D);
 
       
       double magnitude = Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ) + 1.0D;
       
       float bleedingCoefficient = this.weapon.getBleedingCoefficient();
       
       if (projectilesConfig.getBleedingOnHit() != null) {
         bleedingCoefficient *= projectilesConfig.getBleedingOnHit().floatValue();
       }
       
       if (bleedingCoefficient > 0.0F) {
         int count = (int)(getParticleCount(this.damage) * bleedingCoefficient);
         logger.debug("Generating {} particle(s) per damage {}", new Object[] { Integer.valueOf(count), Float.valueOf(this.damage) });
         this.weapon.getModContext().getChannel().sendToAllAround((CompatibleMessage)new SpawnParticleMessage(SpawnParticleMessage.ParticleType.BLOOD, count, 
 
               
               (position.getEntityHit()).posX - this.motionX / magnitude, 
               (position.getEntityHit()).posY - this.motionY / magnitude, 
               (position.getEntityHit()).posZ - this.motionZ / magnitude), point);
       }
     
     }
     else if (position.getTypeOfHit() == CompatibleRayTraceResult.Type.BLOCK) {
       this.weapon.onSpawnEntityBlockImpact(CompatibilityProvider.compatibility.world(this), null, this, position);
     } 
     
     setDead();
   }
 
   
   public void writeSpawnData(ByteBuf buffer) {
     super.writeSpawnData(buffer);
     buffer.writeInt(Item.getIdFromItem((Item)this.weapon));
     buffer.writeFloat(this.damage);
     buffer.writeFloat(this.explosionRadius);
     buffer.writeBoolean(this.isDestroyingBlocks);
     buffer.writeFloat(this.explosionParticleAgeCoefficient);
     buffer.writeFloat(this.smokeParticleAgeCoefficient);
     buffer.writeFloat(this.explosionParticleScaleCoefficient);
     buffer.writeFloat(this.smokeParticleScaleCoefficient);
   }
 
   
   public void readSpawnData(ByteBuf buffer) {
     super.readSpawnData(buffer);
     this.weapon = (Weapon)Item.getItemById(buffer.readInt());
     this.damage = buffer.readFloat();
     this.explosionRadius = buffer.readFloat();
     this.isDestroyingBlocks = buffer.readBoolean();
     this.explosionParticleAgeCoefficient = buffer.readFloat();
     this.smokeParticleAgeCoefficient = buffer.readFloat();
     this.explosionParticleScaleCoefficient = buffer.readFloat();
     this.smokeParticleScaleCoefficient = buffer.readFloat();
   }
 
   
   public void readEntityFromNBT(NBTTagCompound tagCompound) {
     super.readEntityFromNBT(tagCompound);
     Item item = Item.getItemById(tagCompound.getInteger("entityItem"));
     if (item instanceof Weapon) {
       this.weapon = (Weapon)item;
     }
     this.damage = tagCompound.getFloat("damage");
     this.explosionRadius = tagCompound.getFloat("explosionRadius");
     this.isDestroyingBlocks = tagCompound.getBoolean("destroyBlocks");
     this.explosionParticleAgeCoefficient = tagCompound.getFloat("epac");
     this.smokeParticleAgeCoefficient = tagCompound.getFloat("spac");
     this.explosionParticleScaleCoefficient = tagCompound.getFloat("epsc");
     this.smokeParticleScaleCoefficient = tagCompound.getFloat("spsc");
     this.explosionParticleTextureId = tagCompound.getInteger("epti");
     this.smokeParticleTextureId = tagCompound.getInteger("spti");
   }
 
   
   public void writeEntityToNBT(NBTTagCompound tagCompound) {
     super.writeEntityToNBT(tagCompound);
     tagCompound.setInteger("entityItem", Item.getIdFromItem((Item)this.weapon));
     tagCompound.setFloat("damage", this.damage);
     tagCompound.setFloat("explosionRadius", this.explosionRadius);
     tagCompound.setBoolean("destroyBlocks", this.isDestroyingBlocks);
     tagCompound.setFloat("epac", this.explosionParticleAgeCoefficient);
     tagCompound.setFloat("spac", this.smokeParticleAgeCoefficient);
     tagCompound.setFloat("epsc", this.explosionParticleScaleCoefficient);
     tagCompound.setFloat("spsc", this.smokeParticleScaleCoefficient);
     tagCompound.setInteger("epti", this.explosionParticleTextureId);
     tagCompound.setInteger("spti", this.smokeParticleTextureId);
   }
   
   Weapon getWeapon() {
     return this.weapon;
   }
   
   boolean isDamageableEntity(Entity entity) {
     return false;
   }
   
   int getParticleCount(float damage) {
     return (int)(damage - 1.0F);
   }
 
   
   public boolean canCollideWithBlock(Block block, CompatibleBlockState metadata) {
     return (!CompatibilityProvider.compatibility.isBlockPenetratableByBullets(block) && super.canCollideWithBlock(block, metadata));
   }
 
   
   public Item getSpawnedItem() {
     return null;
   }
 }


/* Location:              I:\BON\mwmodify-deobf.jar!\com\vicmatskiv\weaponlib\WeaponSpawnEntity.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */