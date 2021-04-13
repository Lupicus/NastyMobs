package com.lupicus.nasty.entity;

import java.util.Random;

import com.lupicus.nasty.config.MyConfig;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class NastyWolfEntity extends WolfEntity // MonsterEntity
{
	public NastyWolfEntity(EntityType<? extends WolfEntity> type, World worldIn)
	{
		super(type, worldIn);
	}

	@Override
	protected void registerGoals()
	{
		this.sitGoal = new SitGoal(this);
		this.goalSelector.addGoal(1, new SwimGoal(this));
		//this.goalSelector.addGoal(2, this.sitGoal);
		//this.goalSelector.addGoal(3, new WolfEntity.AvoidEntityGoal(this, LlamaEntity.class, 24.0F, 1.5D, 1.5D));
		this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
		this.goalSelector.addGoal(5, new AttackGoal(this, 1.0D, true));
		//this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
		//this.goalSelector.addGoal(7, new BreedGoal(this, 1.0D));
		this.goalSelector.addGoal(8, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
		//this.goalSelector.addGoal(9, new BegGoal(this, 8.0F));
		this.goalSelector.addGoal(10, new LookAtGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.addGoal(10, new LookRandomlyGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
		//this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
		//this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
		//this.targetSelector.addGoal(3, (new HurtByTargetGoal(this)).setCallsForHelp());
		this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AnimalEntity.class, 10, false, false, field_213441_bD));
		//this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, TurtleEntity.class, 10, false, false, TurtleEntity.TARGET_DRY_BABY));
		//this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, AbstractSkeletonEntity.class, false));
	}

	@Override
	protected void registerAttributes()
	{
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((double) 0.35F);
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
		this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
		this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
	}

	@Override
	public CreatureAttribute getCreatureAttribute()
	{
		return CreatureAttribute.UNDEAD;
	}

	public static boolean canSpawn(EntityType<? extends NastyWolfEntity> type, IWorld worldIn, SpawnReason reason,
			BlockPos pos, Random randomIn)
	{
		return worldIn.getDifficulty() != Difficulty.PEACEFUL && MonsterEntity.isValidLightLevel(worldIn, pos, randomIn) && canSpawnOn(type, worldIn, reason, pos, randomIn);
	}

	@SuppressWarnings("deprecation")
	@Override
	public float getBlockPathWeight(BlockPos pos, IWorldReader worldIn)
	{
		return 0.5F - worldIn.getBrightness(pos);
	}

	@Override
	public int getMaxSpawnedInChunk()
	{
		return 4;
	}

	@Override
	public boolean canDespawn(double distance)
	{
		return true;
	}

	@Override
	protected boolean isDespawnPeaceful()
	{
		return true;
	}

	@Override
	public boolean isBegging()
	{
		return false;
	}

	@Override
	public boolean isTamed()
	{
		return false;
	}

	@Override
	public void setTamedBy(PlayerEntity player)
	{
	}

	@Override
	public boolean canBeLeashedTo(PlayerEntity player)
	{
		return false;
	}

	@Override
	public boolean canMateWith(AnimalEntity otherAnimal)
	{
		return false;
	}

	@Override
	public boolean canBreed()
	{
		return false;
	}

	@Override
	public boolean processInteract(PlayerEntity player, Hand hand)
	{
		return false;
	}

	@Override
	public boolean isOnSameTeam(Entity entityIn)
	{
		if (entityIn instanceof NastySkeletonEntity)
			return true;
		return false;
	}

	public static boolean canInfect(WolfEntity mob)
	{
		if (MyConfig.virusMode2 != MyConfig.VMode.OFF && mob.getRNG().nextFloat() < MyConfig.virusChance2)
		{
			if (MyConfig.virusMode2 == MyConfig.VMode.WILD)
				return !mob.isTamed();
			return !mob.hasCustomName();
		}
		return false;
	}

	public static void onInfect(WolfEntity mob)
	{
		World world = mob.world;
		Vec3d mobpos = mob.getPositionVec();
		if (mob.onGround)
			mob.setMotion(0, 0, 0);

		NastyWolfEntity newmob = ModEntities.NASTY_WOLF.create(world);
		if (mob.hasCustomName())
		{
			newmob.setCustomName(mob.getCustomName());
			newmob.setCustomNameVisible(mob.isCustomNameVisible());
		}
		newmob.setInvisible(mob.isInvisible());
		newmob.setInvulnerable(mob.isInvulnerable());
		newmob.setLocationAndAngles(mobpos.getX(), mobpos.getY(), mobpos.getZ(), mob.rotationYaw, mob.rotationPitch);

		newmob.onInitialSpawn(world, world.getDifficultyForLocation(new BlockPos(mobpos)), SpawnReason.CONVERSION, (ILivingEntityData) null,
				(CompoundNBT) null);

		newmob.setGrowingAge(mob.getGrowingAge());

		if (mob instanceof MobEntity)
		{
			MobEntity from = (MobEntity) mob;
			if (from.isNoDespawnRequired())
				newmob.enablePersistence();
		    newmob.setNoAI(from.isAIDisabled());
		    //newmob.setRevengeTarget(from.getRevengeTarget());
			newmob.rotationYawHead = from.rotationYawHead;
			newmob.renderYawOffset = from.rotationYawHead;
		}
		newmob.setMotion(mob.getMotion());

		// SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED
	    world.playEvent((PlayerEntity)null, 1027, new BlockPos(newmob), 0);
		world.addEntity(newmob);
		mob.remove();
	}

	public static class AttackGoal extends MeleeAttackGoal
	{
		private double speed;

		public AttackGoal(CreatureEntity creature, double speedIn, boolean useLongMemory) {
			super(creature, speedIn, useLongMemory);
			speed = speedIn;
		}

		@Override
		protected void checkAndPerformAttack(LivingEntity enemy, double distToEnemySqr) {
			if (this.attackTick > 0)
				return;
			double d0 = this.getAttackReachSqr(enemy);
			if (distToEnemySqr <= d0) {
				this.attackTick = 20;
				this.attacker.swingArm(Hand.MAIN_HAND);
				this.attacker.attackEntityAsMob(enemy);
			}
			else if (d0 < 4.0 && attacker.getNavigator().noPath())
			{
				// (in blind spot) move the mob closer so it can attack
				double x = enemy.getPosX();
				double y = enemy.getPosY();
				double z = enemy.getPosZ();
				x = (x - attacker.getPosX()) * 0.5 + x;
				y = (y - attacker.getPosY()) * 0.5 + y;
				z = (z - attacker.getPosZ()) * 0.5 + z;
				attacker.getMoveHelper().setMoveTo(x, y, z, speed);
			}
		}
	}
}
