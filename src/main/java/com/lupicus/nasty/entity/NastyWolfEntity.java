package com.lupicus.nasty.entity;

import com.lupicus.nasty.config.MyConfig;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.NonTamedTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
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
		this.goalSelector.addGoal(1, new SwimGoal(this));
		//this.goalSelector.addGoal(2, new SitGoal(this));
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
		//this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, 10, true, false, this::func_233680_b_));
		this.targetSelector.addGoal(5, new NonTamedTargetGoal<>(this, AnimalEntity.class, false, TARGET_ENTITIES));		
		//this.targetSelector.addGoal(6, new NonTamedTargetGoal<>(this, TurtleEntity.class, false, TurtleEntity.TARGET_DRY_BABY));
		//this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, AbstractSkeletonEntity.class, false));
		//this.targetSelector.addGoal(8, new ResetAngerGoal<>(this, true));
	}

	public static AttributeModifierMap.MutableAttribute registerAttibutes() {
		return WolfEntity.func_234233_eS_()
				.func_233815_a_(Attributes.field_233821_d_, (double) 0.35F) // movement_speed
				.func_233815_a_(Attributes.field_233818_a_, 20.0D) // max_health
				.func_233815_a_(Attributes.field_233823_f_, 4.0D) // attack_damage
				.func_233815_a_(Attributes.field_233819_b_, 32.0D); // follow_range
	}

	@Override
	public CreatureAttribute getCreatureAttribute()
	{
		return CreatureAttribute.UNDEAD;
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
	public ActionResultType func_230254_b_(PlayerEntity player, Hand hand) // processInteract
	{
		return ActionResultType.PASS;
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
		Vector3d mobpos = mob.getPositionVec();
		if (mob.func_233570_aj_()) // onGround
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
	    world.playEvent((PlayerEntity)null, 1027, newmob.func_233580_cy_(), 0);
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
			if (func_234041_j_() > 0) // get attackTick
				return;
			double d0 = this.getAttackReachSqr(enemy);
			if (distToEnemySqr <= d0) {
				func_234039_g_(); // reset attackTick
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
