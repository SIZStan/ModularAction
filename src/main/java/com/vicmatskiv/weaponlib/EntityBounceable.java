package com.vicmatskiv.weaponlib;

import com.vicmatskiv.weaponlib.compatibility.CompatibilityProvider;
import com.vicmatskiv.weaponlib.compatibility.CompatibleAxisAlignedBB;
import com.vicmatskiv.weaponlib.compatibility.CompatibleBlockPos;
import com.vicmatskiv.weaponlib.compatibility.CompatibleBlockState;
import com.vicmatskiv.weaponlib.compatibility.CompatibleIEntityAdditionalSpawnData;
import com.vicmatskiv.weaponlib.compatibility.CompatibleIThrowableEntity;
import com.vicmatskiv.weaponlib.compatibility.CompatibleMathHelper;
import com.vicmatskiv.weaponlib.compatibility.CompatibleRayTraceResult;
import com.vicmatskiv.weaponlib.compatibility.CompatibleRayTracing;
import com.vicmatskiv.weaponlib.compatibility.CompatibleVec3;

import indi.ma.server.ServerListener;
import io.netty.buffer.ByteBuf;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityBounceable extends Entity
		implements Contextual, CompatibleIEntityAdditionalSpawnData, CompatibleIThrowableEntity {
	private static final Logger logger = LogManager.getLogger(EntityBounceable.class);
	private static final int VELOCITY_HISTORY_SIZE = 10;
	private static final double STOP_THRESHOLD = 0.001d;
	private static final int MAX_TICKS = 2000;
	protected ModContext modContext;
	private float gravityVelocity;
	private float slowdownFactor;
	private int ticksInAir;
	private EntityLivingBase thrower;
	protected int bounceCount;
	private float initialYaw;
	private float initialPitch;
	private float xRotation;
	private float yRotation;
	private float zRotation;
	private float xRotationChange;
	private float yRotationChange;
	private float zRotationChange;
	private float rotationSlowdownFactor;
	private float maxRotationChange;
	protected boolean stopped;
	private Queue<Double> velocityHistory;

	public EntityBounceable(ModContext modContext, World world, EntityLivingBase thrower, float velocity,
			float gravityVelocity, float rotationSlowdownFactor) {
		super(world);
		this.slowdownFactor = 0.5f;
		this.rotationSlowdownFactor = 0.99f;
		this.maxRotationChange = 20.0f;
		this.velocityHistory = new ArrayDeque(10);
		this.modContext = modContext;
		this.thrower = thrower;
		this.gravityVelocity = gravityVelocity;
		this.rotationSlowdownFactor = rotationSlowdownFactor;
		setSize(0.3f, 0.3f);
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

		this.posX -= CompatibleMathHelper.cos((this.rotationYaw / 180.0f) * 3.1415927f) * 0.16f;
		this.posY -= 0.10000000149011612d;
		this.posZ -= CompatibleMathHelper.sin((this.rotationYaw / 180.0f) * 3.1415927f) * 0.16f;
		setPosition(this.posX, this.posY, this.posZ);
		this.motionX = (-CompatibleMathHelper.sin((this.rotationYaw / 180.0f) * 3.1415927f))
				* CompatibleMathHelper.cos((this.rotationPitch / 180.0f) * 3.1415927f) * 0.4f;
		this.motionZ = CompatibleMathHelper.cos((this.rotationYaw / 180.0f) * 3.1415927f)
				* CompatibleMathHelper.cos((this.rotationPitch / 180.0f) * 3.1415927f) * 0.4f;
		this.motionY = (-CompatibleMathHelper.sin(((this.rotationPitch + 0.0f) / 180.0f) * 3.1415927f)) * 0.4f;
		this.initialYaw = this.rotationYaw;
		this.initialPitch = this.rotationPitch;
		setThrowableHeading(this.motionX, this.motionY, this.motionZ, velocity, 10.0f);
		logger.debug("Throwing with position {}{}{}, rotation pitch {}, velocity {}, {}, {}",
				new Object[] { Double.valueOf(this.posX), Double.valueOf(this.posY), Double.valueOf(this.posZ),
						Float.valueOf(this.rotationPitch), Double.valueOf(this.motionX), Double.valueOf(this.motionY),
						Double.valueOf(this.motionZ) });
	}

	public void setThrowableHeading(double motionX, double motionY, double motionZ, float velocity, float inaccuracy) {
		float f2 = CompatibleMathHelper.sqrt_double((motionX * motionX) + (motionY * motionY) + (motionZ * motionZ));
		double motionX2 = motionX / f2;
		double motionY2 = motionY / f2;
		double motionZ2 = motionZ / f2;
		double motionX3 = motionX2 + (this.rand.nextGaussian() * 0.007499999832361937d * inaccuracy);
		double motionY3 = motionY2 + (this.rand.nextGaussian() * 0.007499999832361937d * inaccuracy);
		double motionZ3 = motionZ2 + (this.rand.nextGaussian() * 0.007499999832361937d * inaccuracy);
		double motionX4 = motionX3 * velocity;
		double motionY4 = motionY3 * velocity;
		double motionZ4 = motionZ3 * velocity;
		this.motionX = motionX4;
		this.motionY = motionY4;
		this.motionZ = motionZ4;
		float f3 = CompatibleMathHelper.sqrt_double((motionX4 * motionX4) + (motionZ4 * motionZ4));
		float atan2 = (float) ((Math.atan2(motionX4, motionZ4) * 180.0d) / 3.141592653589793d);
		this.rotationYaw = atan2;
		this.prevRotationYaw = atan2;
		float atan22 = (float) ((Math.atan2(motionY4, f3) * 180.0d) / 3.141592653589793d);
		this.rotationPitch = atan22;
		this.prevRotationPitch = atan22;
	}

	public EntityBounceable(World world) {
		super(world);
		this.slowdownFactor = 0.5f;
		this.rotationSlowdownFactor = 0.99f;
		this.maxRotationChange = 20.0f;
		this.velocityHistory = new ArrayDeque(10);
		setRotations();
	}

	private void setRotations() {
		this.xRotationChange = this.maxRotationChange * ((float) this.rand.nextGaussian());
		this.yRotationChange = this.maxRotationChange * ((float) this.rand.nextGaussian());
		this.zRotationChange = this.maxRotationChange * ((float) this.rand.nextGaussian());
	}

	public EntityLivingBase getThrower() {
		return this.thrower;
	}

	public void setThrower(Entity thrower) {
		this.thrower = (EntityLivingBase) thrower;
	}

	public void onUpdate() {
		if (!CompatibilityProvider.compatibility.world(this).isRemote && this.ticksExisted > 2000) {
			this.setDead();
			return;
		}

		this.xRotation += this.xRotationChange;
		this.yRotation += this.yRotationChange;
		this.zRotation += this.zRotationChange;
		this.xRotationChange *= this.rotationSlowdownFactor;
		this.yRotationChange *= this.rotationSlowdownFactor;
		this.zRotationChange *= this.rotationSlowdownFactor;
		this.lastTickPosX = this.posX;
		this.lastTickPosY = this.posY;
		this.lastTickPosZ = this.posZ;
		super.onUpdate();
		++this.ticksInAir;
		if (!this.stopped) {
			CompatibleVec3 vec3 = new CompatibleVec3(this.posX, this.posY, this.posZ);
			CompatibleVec3 vec31 = new CompatibleVec3(this.posX + this.motionX, this.posY + this.motionY,
					this.posZ + this.motionZ);
			CompatibleRayTraceResult movingobjectposition = CompatibleRayTracing.rayTraceBlocks(
					CompatibilityProvider.compatibility.world(this), vec3, vec31,
					(Block arg2, CompatibleBlockState arg3) -> this.canCollideWithBlock(arg2, arg3));
			CompatibleVec3 v3_1 = new CompatibleVec3(this.posX, this.posY, this.posZ);
			CompatibleVec3 v5_1 = new CompatibleVec3(this.posX + this.motionX, this.posY + this.motionY,
					this.posZ + this.motionZ);
			if (movingobjectposition != null) {
				v5_1 = CompatibleVec3.fromCompatibleVec3(movingobjectposition.getHitVec());
			}

			if (this.thrower != null) {
				Entity entity = null;
				List list = CompatibilityProvider.compatibility.getEntitiesWithinAABBExcludingEntity(
						CompatibilityProvider.compatibility.world(this), this,
						CompatibilityProvider.compatibility.getBoundingBox(this)
								.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0, 1.0, 1.0));
				double d0 = 0.0;
				EntityLivingBase entitylivingbase = this.getThrower();
				CompatibleRayTraceResult entityMovingObjectPosition = null;
				int j;
				for (j = 0; j < list.size(); ++j) {
					Entity entity1 = (Entity) list.get(j);
					if ((entity1.canBeCollidedWith()) && (entity1 != entitylivingbase || this.ticksInAir >= 5)) {
						CompatibleRayTraceResult movingobjectposition1 = CompatibilityProvider.compatibility
								.expandEntityBoundingBox(entity1, 0.3, 0.3, 0.3).calculateIntercept(v3_1, v5_1);
						if (movingobjectposition1 != null) {
							double d1 = v3_1.distanceTo(movingobjectposition1.getHitVec());
							if (d1 < d0 || d0 == 0.0) {
								entity = entity1;
								entityMovingObjectPosition = movingobjectposition1;
								d0 = d1;
							}
						}
					}
				}

				if (entity != null) {
					movingobjectposition = new CompatibleRayTraceResult(entity);
					movingobjectposition.setSideHit(entityMovingObjectPosition.getSideHit());
					movingobjectposition.setHitVec(entityMovingObjectPosition.getHitVec());
				}
			}

			EntityBounceable.logger.trace("Ori position to {}, {}, {}, motion {} {} {} ",
					new Object[] { ((double) this.posX), ((double) this.posY), ((double) this.posZ),
							((double) this.motionX), ((double) this.motionY), ((double) this.motionZ) });
			if (movingobjectposition != null
					&& (movingobjectposition.getTypeOfHit() == CompatibleRayTraceResult.Type.BLOCK
							|| movingobjectposition.getTypeOfHit() == CompatibleRayTraceResult.Type.ENTITY)) {
				Object[] v8 = { movingobjectposition.getTypeOfHit(),
						((double) movingobjectposition.getHitVec().getXCoord()),
						((double) movingobjectposition.getHitVec().getYCoord()),
						((double) movingobjectposition.getHitVec().getZCoord()) };
				EntityBounceable.logger.trace("Hit {}, vec set to {}, {}, {}", v8);
				Object[] v8_1 = { ((int) this.bounceCount), movingobjectposition.getSideHit(), ((double) this.motionX),
						((double) this.motionY), ((double) this.motionZ) };
				EntityBounceable.logger.trace("Before bouncing {}, side {}, motion set to {}, {}, {}", v8_1);
				this.posX = movingobjectposition.getHitVec().getXCoord();
				this.posY = movingobjectposition.getHitVec().getYCoord();
				this.posZ = movingobjectposition.getHitVec().getZCoord();
				switch (movingobjectposition.getSideHit()) {
				case DOWN:
					this.motionY = -this.motionY;
					this.posY += this.motionY;
					break;
				case UP:
					this.motionY = -this.motionY;
					break;
				case NORTH:
					this.motionZ = -this.motionZ;
					this.posZ += this.motionZ;
					break;
				case SOUTH:
					this.motionZ = -this.motionZ;
					break;
				case WEST:
					this.motionX = -this.motionX;
					this.posX += this.motionX;
					break;
				case EAST:
					this.motionX = -this.motionX;
					break;
				}

				this.setPosition(this.posX, this.posY, this.posZ);
				if (movingobjectposition.getTypeOfHit() == CompatibleRayTraceResult.Type.ENTITY) {
					this.avoidEntityCollisionAfterBounce(movingobjectposition);
				} else if (movingobjectposition.getTypeOfHit() == CompatibleRayTraceResult.Type.BLOCK) {
					this.avoidBlockCollisionAfterBounce(movingobjectposition);
				}

				EntityBounceable.logger.trace("After bouncing {}  motion set to {}, {}, {}",
						new Object[] { ((int) this.bounceCount), ((double) this.motionX), ((double) this.motionY),
								((double) this.motionZ) });
				onBounce(movingobjectposition);
				++this.bounceCount;
				if (this.isDead) {
					return;
				}
			} else {
				this.posX += this.motionX;
				this.posY += this.motionY;
				this.posZ += this.motionZ;
			}

			this.setPosition(this.posX, this.posY, this.posZ);
			float motionSquared = CompatibleMathHelper
					.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0 / 3.141593);
			this.rotationPitch = (float) (Math.atan2(this.motionY, motionSquared) * 180.0 / 3.141593);
			while (this.rotationPitch - this.prevRotationPitch < -180.0f) {
				this.prevRotationPitch -= 360.0f;
			}

			while (this.rotationPitch - this.prevRotationPitch >= 180.0f) {
				this.prevRotationPitch += 360.0f;
			}

			while (this.rotationYaw - this.prevRotationYaw < -180.0f) {
				this.prevRotationYaw -= 360.0f;
			}

			while (this.rotationYaw - this.prevRotationYaw >= 180.0f) {
				this.prevRotationYaw += 360.0f;
			}

			this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2f;
			this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2f;
			float f2 = 0.99f;
			float currentGravityVelocity = this.getGravityVelocity();
			if (this.isInWater()) {
				int i;
				for (i = 0; i < 4; ++i) {
					CompatibilityProvider.compatibility.spawnParticle(CompatibilityProvider.compatibility.world(this),
							"bubble", this.posX - this.motionX * 0.25, this.posY - this.motionY * 0.25,
							this.posZ - this.motionZ * 0.25, this.motionX, this.motionY, this.motionZ);
				}

				f2 = 0.8f;
			}

			if (movingobjectposition != null
					&& (movingobjectposition.getTypeOfHit() == CompatibleRayTraceResult.Type.BLOCK
							|| movingobjectposition.getTypeOfHit() == CompatibleRayTraceResult.Type.ENTITY)) {
				f2 = this.slowdownFactor;
				this.rotationSlowdownFactor *= this.slowdownFactor * 1.5f;
			}

			this.motionX *= (double) f2;
			this.motionY *= (double) f2;
			this.motionZ *= (double) f2;
			this.recordVelocityHistory();
			if (this.velocityHistory.stream().anyMatch((Double arg4) -> ((double) arg4) > 0.001)) {
				this.motionY -= (double) currentGravityVelocity;
			} else {
				this.motionZ = 0.0;
				this.motionY = 0.0;
				this.motionX = 0.0;
				this.stopped = true;
				EntityBounceable.logger.trace("Stopping {}", new Object[] { this });
				onStop();
			}

			EntityBounceable.logger.trace("Set position to {}, {}, {}, motion {} {} {} ",
					new Object[] { ((double) this.posX), ((double) this.posY), ((double) this.posZ),
							((double) this.motionX), ((double) this.motionY), ((double) this.motionZ) });
			return;
		}
	}

	public void onStop() {
	}

	public void onBounce(CompatibleRayTraceResult movingobjectposition) {
	}

	private void avoidBlockCollisionAfterBounce(CompatibleRayTraceResult movingobjectposition) {
		if (movingobjectposition.getTypeOfHit() == CompatibleRayTraceResult.Type.BLOCK) {
			double dX = Math.signum(this.motionX) * 0.05d;
			double dY = Math.signum(this.motionY) * 0.05d;
			double dZ = Math.signum(this.motionZ) * 0.05d;
			for (int i = 0; i < 10; i++) {
				double projectedXPos = this.posX + (dX * i);
				double projectedYPos = this.posY + (dY * i);
				double projectedZPos = this.posZ + (dZ * i);
				CompatibleVec3 projectedPos = new CompatibleVec3(projectedXPos, projectedYPos, projectedZPos);
				CompatibleBlockPos blockPos = new CompatibleBlockPos(projectedPos);
				CompatibleAxisAlignedBB projectedEntityBoundingBox = CompatibilityProvider.compatibility
						.getBoundingBox(this).offset(dX * i, dY * i, dZ * i);
				if (CompatibilityProvider.compatibility.isAirBlock(CompatibilityProvider.compatibility.world(this),
						blockPos)
						|| !new CompatibleAxisAlignedBB(blockPos).intersectsWith(projectedEntityBoundingBox)) {
					this.posX = projectedXPos;
					this.posY = projectedYPos;
					this.posZ = projectedZPos;
					logger.trace("Found non-intercepting post-bounce position on iteration {}",
							new Object[] { Integer.valueOf(i) });
					return;
				}
			}
		}
	}

	private void avoidEntityCollisionAfterBounce(CompatibleRayTraceResult movingobjectposition) {
		if (movingobjectposition.getEntityHit() == null) {
			return;
		}

		this.slowdownFactor = 0.3F;
		double dX = Math.signum(this.motionX) * 0.05D;
		double dY = Math.signum(this.motionY) * 0.05D;
		double dZ = Math.signum(this.motionZ) * 0.05D;

		float f = 0.3F;

		CompatibleAxisAlignedBB axisalignedbb = CompatibilityProvider.compatibility
				.getBoundingBox(movingobjectposition.getEntityHit()).expand(f, f, f);
		CompatibleRayTraceResult intercept = movingobjectposition;
		for (int i = 0; i < 10; i++) {
			CompatibleVec3 currentPos = new CompatibleVec3(this.posX + dX * i, this.posY + dY * i, this.posZ + dY * i);
			CompatibleVec3 projectedPos = new CompatibleVec3(this.posX + dX * (i + 1), this.posY + dY * (i + 1),
					this.posZ + dZ * (i + 1));
			intercept = axisalignedbb.calculateIntercept(currentPos, projectedPos);
			if (intercept == null) {

				CompatibleBlockPos blockPos = new CompatibleBlockPos(projectedPos);
				CompatibleBlockState blockState;
				if ((blockState = CompatibilityProvider.compatibility
						.getBlockAtPosition(CompatibilityProvider.compatibility.world(this), blockPos)) != null
						&& !CompatibilityProvider.compatibility.isAirBlock(blockState)) {
					logger.debug("Found non-intercept position colliding with block {}", new Object[] { blockState });
					intercept = movingobjectposition;
					break;
				}
				this.posX = projectedPos.getXCoord();
				this.posY = projectedPos.getYCoord();
				this.posZ = projectedPos.getZCoord();

				break;
			}
		}

		if (intercept != null) {
			logger.debug("Could not find non-intercept position after bounce");
		}
	}

	protected float getGravityVelocity() {
		return this.gravityVelocity;
	}

	protected void entityInit() {
	}

	public void readEntityFromNBT(NBTTagCompound tagCompound) {
	}

	public void writeEntityToNBT(NBTTagCompound tagCompound) {
	}

	public void writeSpawnData(ByteBuf buffer) {
		buffer.writeInt(this.thrower != null ? this.thrower.getEntityId() : -1);
		buffer.writeDouble(this.posX);
		buffer.writeDouble(this.posY);
		buffer.writeDouble(this.posZ);
		buffer.writeDouble(this.motionX);
		buffer.writeDouble(this.motionY);
		buffer.writeDouble(this.motionZ);
		buffer.writeFloat(this.gravityVelocity);
		buffer.writeFloat(this.rotationSlowdownFactor);
		buffer.writeFloat(this.initialYaw);
		buffer.writeFloat(this.initialPitch);
	}

	public void readSpawnData(ByteBuf buffer) {
		int entityId = buffer.readInt();
		if (this.thrower == null && entityId >= 0) {
			EntityPlayer entityByID = (EntityPlayer) CompatibilityProvider.compatibility.world(this)
					.getEntityByID(entityId);
			if (entityByID instanceof EntityLivingBase) {
				this.thrower = entityByID;
			}
		}
		this.posX = buffer.readDouble();
		this.posY = buffer.readDouble();
		this.posZ = buffer.readDouble();
		this.motionX = buffer.readDouble();
		this.motionY = buffer.readDouble();
		this.motionZ = buffer.readDouble();
		this.gravityVelocity = buffer.readFloat();
		this.rotationSlowdownFactor = buffer.readFloat();
		this.initialYaw = buffer.readFloat();
		this.initialPitch = buffer.readFloat();
		setPosition(this.posX, this.posY, this.posZ);
		logger.debug("Restoring with position {}{}{}, rotation pitch {}, velocity {}, {}, {}",
				new Object[] { Double.valueOf(this.posX), Double.valueOf(this.posY), Double.valueOf(this.posZ),
						Float.valueOf(this.rotationPitch), Double.valueOf(this.motionX), Double.valueOf(this.motionY),
						Double.valueOf(this.motionZ) });
	}

	public float getXRotation() {
		return this.xRotation;
	}

	public float getYRotation() {
		return (this.yRotation - this.initialYaw) - 90.0f;
	}

	public float getZRotation() {
		return this.zRotation;
	}

	public boolean canCollideWithBlock(Block block, CompatibleBlockState metadata) {
		return CompatibilityProvider.compatibility.canCollideCheck(block, metadata, false);
	}

	private void recordVelocityHistory() {
		double velocity = (this.motionX * this.motionX) + (this.motionY * this.motionY) + (this.motionZ * this.motionZ);
		this.velocityHistory.add(Double.valueOf(velocity));
		if (this.velocityHistory.size() > 10) {
			this.velocityHistory.poll();
		}
	}

	@Override // com.vicmatskiv.weaponlib.Contextual
	public void setContext(ModContext modContext) {
		if (this.modContext == null) {
			this.modContext = modContext;
		}
	}
}
