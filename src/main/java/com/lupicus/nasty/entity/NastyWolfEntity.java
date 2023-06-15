package com.lupicus.nasty.entity;

import com.lupicus.nasty.config.MyConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

public class NastyWolfEntity extends Wolf implements Enemy // Monster
{
	public NastyWolfEntity(EntityType<? extends Wolf> type, Level worldIn)
	{
		super(type, worldIn);
	}

	@Override
	protected void registerGoals()
	{
		this.goalSelector.addGoal(1, new FloatGoal(this));
		//this.goalSelector.addGoal(1, new Wolf.WolfPanicGoal(1.5D));
		//this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
		//this.goalSelector.addGoal(3, new Wolf.WolfAvoidEntityGoal<>(this, Llama.class, 24.0F, 1.5D, 1.5D));
		this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
		this.goalSelector.addGoal(5, new AttackGoal(this, 1.0D, true));
		//this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
		//this.goalSelector.addGoal(7, new BreedGoal(this, 1.0D));
		this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0D));
		//this.goalSelector.addGoal(9, new BegGoal(this, 8.0F));
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
		//this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
		//this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
		//this.targetSelector.addGoal(3, (new HurtByTargetGoal(this)).setAlertOthers());
		//this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::func_233680_b_));
		this.targetSelector.addGoal(5, new NonTameRandomTargetGoal<>(this, Animal.class, false, PREY_SELECTOR));		
		//this.targetSelector.addGoal(6, new NonTameRandomTargetGoal<>(this, Turtle.class, false, Turtle.BABY_ON_LAND_SELECTOR));
		//this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, AbstractSkeleton.class, false));
		//this.targetSelector.addGoal(8, new ResetUniversalAngerTargetGoal<>(this, true));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Wolf.createAttributes()
				.add(Attributes.MOVEMENT_SPEED, (double) 0.35F)
				.add(Attributes.MAX_HEALTH, 20.0D)
				.add(Attributes.ATTACK_DAMAGE, 4.0D)
				.add(Attributes.FOLLOW_RANGE, 32.0D);
	}

	@Override
	public MobType getMobType()
	{
		return MobType.UNDEAD;
	}

	@Override
	public SoundSource getSoundSource()
	{
		return SoundSource.HOSTILE;
	}

	public static boolean checkSpawnRules(EntityType<? extends NastyWolfEntity> type, ServerLevelAccessor worldIn, MobSpawnType reason,
			BlockPos pos, RandomSource randomIn)
	{
		return worldIn.getDifficulty() != Difficulty.PEACEFUL && Monster.isDarkEnoughToSpawn(worldIn, pos, randomIn) && checkMobSpawnRules(type, worldIn, reason, pos, randomIn);
	}

	@Override
	public float getWalkTargetValue(BlockPos pos, LevelReader worldIn)
	{
		return -worldIn.getPathfindingCostFromLightLevels(pos);
	}

	@Override
	public int getMaxSpawnClusterSize()
	{
		return 4;
	}

	@Override
	public boolean removeWhenFarAway(double distance)
	{
		return true;
	}

	@Override
	protected boolean shouldDespawnInPeaceful()
	{
		return true;
	}

	@Override
	public boolean isInterested()
	{
		return false;
	}

	@Override
	public boolean isTame()
	{
		return false;
	}

	@Override
	public void tame(Player player)
	{
	}

	@Override
	public boolean canBeLeashed(Player player)
	{
		return false;
	}

	@Override
	public boolean canMate(Animal otherAnimal)
	{
		return false;
	}

	@Override
	public boolean canBreed()
	{
		return false;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand)
	{
		return InteractionResult.PASS;
	}

	@Override
	public boolean isAlliedTo(Entity entityIn)
	{
		if (entityIn instanceof NastySkeletonEntity)
			return true;
		return false;
	}

	public static boolean canInfect(Wolf mob)
	{
		if (MyConfig.virusMode2 != MyConfig.VMode.OFF && mob.getRandom().nextFloat() < MyConfig.virusChance2)
		{
			if (MyConfig.virusMode2 == MyConfig.VMode.WILD)
				return !mob.isTame();
			return !mob.hasCustomName();
		}
		return false;
	}

	public static void onInfect(Wolf mob)
	{
		ServerLevel world = (ServerLevel) mob.level();
		Vec3 mobpos = mob.position();
		if (mob.onGround())
			mob.setDeltaMovement(0, 0, 0);

		NastyWolfEntity newmob = ModEntities.NASTY_WOLF.create(world);
		if (newmob == null)
			return;
		if (mob.hasCustomName())
		{
			newmob.setCustomName(mob.getCustomName());
			newmob.setCustomNameVisible(mob.isCustomNameVisible());
		}
		newmob.setInvisible(mob.isInvisible());
		newmob.setInvulnerable(mob.isInvulnerable());
		newmob.moveTo(mobpos.x(), mobpos.y(), mobpos.z(), mob.getYRot(), mob.getXRot());

		newmob.finalizeSpawn(world, world.getCurrentDifficultyAt(BlockPos.containing(mobpos)), MobSpawnType.CONVERSION, (SpawnGroupData) null,
				(CompoundTag) null);

		newmob.setAge(mob.getAge());

		if (mob instanceof Mob)
		{
			Mob from = (Mob) mob;
			if (from.isPersistenceRequired())
				newmob.setPersistenceRequired();
			newmob.setNoAi(from.isNoAi());
			//newmob.setLastHurtByMob(from.getLastHurtByMob());
			newmob.yHeadRot = from.yHeadRot;
			newmob.yBodyRot = from.yHeadRot;
		}
		newmob.setDeltaMovement(mob.getDeltaMovement());

		// SoundEvents.ZOMBIE_VILLAGER_CONVERTED
		world.levelEvent((Player) null, 1027, newmob.blockPosition(), 0);
		world.addFreshEntity(newmob);
		mob.discard();
	}

	public static class AttackGoal extends MeleeAttackGoal
	{
		private double speed;

		public AttackGoal(PathfinderMob creature, double speedIn, boolean useLongMemory) {
			super(creature, speedIn, useLongMemory);
			speed = speedIn;
		}

		@Override
		protected void checkAndPerformAttack(LivingEntity enemy, double distToEnemySqr) {
			if (!isTimeToAttack())
				return;
			double d0 = this.getAttackReachSqr(enemy);
			if (distToEnemySqr <= d0) {
				resetAttackCooldown(); // reset attackTick
				this.mob.swing(InteractionHand.MAIN_HAND);
				this.mob.doHurtTarget(enemy);
			}
			else if (d0 < 4.0 && mob.getNavigation().isDone())
			{
				// (in blind spot) move the mob closer so it can attack
				double x = enemy.getX();
				double y = enemy.getY();
				double z = enemy.getZ();
				x = (x - mob.getX()) * 0.5 + x;
				y = (y - mob.getY()) * 0.5 + y;
				z = (z - mob.getZ()) * 0.5 + z;
				mob.getMoveControl().setWantedPosition(x, y, z, speed);
			}
		}
	}
}
