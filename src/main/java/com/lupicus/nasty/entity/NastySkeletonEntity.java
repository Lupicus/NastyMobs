package com.lupicus.nasty.entity;

import java.util.HashMap;
import java.util.Random;

import com.lupicus.nasty.config.MyConfig;
import com.lupicus.nasty.entity.ai.controller.JumpMovementController;
import com.lupicus.nasty.entity.ai.goal.SpreadVirusGoal;
import com.lupicus.nasty.item.ModItems;
import com.lupicus.nasty.pathfinding.JumpPathNavigator;
import com.lupicus.nasty.util.ArrowHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.goal.FleeSunGoal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.entity.ai.goal.RestrictSunGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

public class NastySkeletonEntity extends AbstractSkeletonEntity implements IHasVirus
{
	public static final int NVARIANTS = 6;
	private static int defVariant = 0;
	private static boolean singleVariant = false;
	private static HashMap<String,AdjParms> biomeMap = new HashMap<>();
	private static final DataParameter<Integer> SUB_TYPE = EntityDataManager.createKey(NastySkeletonEntity.class, DataSerializers.VARINT);
	private final RangedBowAttackGoal<NastySkeletonEntity> aiArrowAttack = new RangedBowAttackGoal<>(this, 1.0D, 20, 40.0F);
	private final MeleeAttackGoal aiAttackOnCollide = new MeleeAttackGoal(this, 1.2D, false) {
	    /**
	     * Reset the task's internal state. Called when this task is interrupted by another one
	     */
	    @Override
		public void resetTask() {
	    	super.resetTask();
	    	NastySkeletonEntity.this.setAggroed(false);
	    }

	    /**
	     * Execute a one shot task or start executing a continuous task
	     */
	    @Override
		public void startExecuting() {
	    	super.startExecuting();
	    	NastySkeletonEntity.this.setAggroed(true);
	    }
	};

	public NastySkeletonEntity(EntityType<? extends NastySkeletonEntity> type, World worldIn)
	{
		super(type, worldIn);
		setCombatTask();
		moveController = new JumpMovementController(this);
	}

	@Override
	protected SoundEvent getAmbientSound()
	{
		return SoundEvents.ENTITY_SKELETON_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn)
	{
		return SoundEvents.ENTITY_SKELETON_HURT;
	}

	@Override
	protected SoundEvent getDeathSound()
	{
		return SoundEvents.ENTITY_SKELETON_DEATH;
	}

	@Override
	protected SoundEvent getStepSound()
	{
		return SoundEvents.ENTITY_SKELETON_STEP;
	}

	@Override
	protected void registerAttributes()
	{
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.30D);
		this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(24.0D);
		this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
	}

	@Override
	protected void registerGoals()
	{
		if (MyConfig.virusChance > 0 && MyConfig.virusDistance > 0)
			this.goalSelector.addGoal(1, new SpreadVirusGoal(this, SkeletonEntity.class, MyConfig.virusDistance, MyConfig.virusChance));
		this.goalSelector.addGoal(2, new RestrictSunGoal(this));
	    this.goalSelector.addGoal(3, new FleeSunGoal(this, 1.0D));
	    //this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, WolfEntity.class, 6.0F, 1.0D, 1.2D));
	    this.goalSelector.addGoal(5, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
	    this.goalSelector.addGoal(6, new LookAtGoal(this, PlayerEntity.class, 8.0F));
	    this.goalSelector.addGoal(6, new LookRandomlyGoal(this));
	    this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
	    this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
	    this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolemEntity.class, true));
	    //this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, TurtleEntity.class, 10, true, false, TurtleEntity.TARGET_DRY_BABY));
	}

	@Override
	protected void registerData()
	{
		super.registerData();
		this.dataManager.register(SUB_TYPE, 0);
	}

	@Override
	protected PathNavigator createNavigator(World worldIn)
	{
		return new JumpPathNavigator(this, worldIn);
	}

	public int getSubType()
	{
		return dataManager.get(SUB_TYPE);
	}

	@Override
	public void writeAdditional(CompoundNBT compound)
	{
		super.writeAdditional(compound);
		compound.putByte("SubType", (byte) getSubType());
	}

	@Override
	public void readAdditional(CompoundNBT compound)
	{
		super.readAdditional(compound);
		dataManager.set(SUB_TYPE, (int) compound.getByte("SubType"));
	}

	@Override
	protected ResourceLocation getLootTable()
	{
		ResourceLocation res = super.getLootTable();
		return new ResourceLocation(res.getNamespace(), res.getPath() + "/" + getSubType());
	}

	/**
	 * Copied from AbstractSkeletonEntity, so we can use our version of aiArrowAttack
	 */
	@Override
	public void setCombatTask()
	{
		if (this.world != null && !this.world.isRemote) {
			if (aiAttackOnCollide == null)
				return;
	        this.goalSelector.removeGoal(this.aiAttackOnCollide);
	        this.goalSelector.removeGoal(this.aiArrowAttack);
	        ItemStack itemstack = this.getHeldItem(ProjectileHelper.getHandWith(this, Items.BOW));
	        if (itemstack.getItem() instanceof BowItem) {
	            int i = 20;
	            if (this.world.getDifficulty() != Difficulty.HARD) {
	                i = 40;
	            }

	            this.aiArrowAttack.setAttackCooldown(i);
	            this.goalSelector.addGoal(4, this.aiArrowAttack);
	        } else {
	        	this.goalSelector.addGoal(4, this.aiAttackOnCollide);
	        }
		}
	}

	@Override
	public ILivingEntityData onInitialSpawn(IWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason,
			ILivingEntityData spawnDataIn, CompoundNBT dataTag)
	{
		Biome biome = worldIn.getBiome(getPosition());
		if (MyConfig.spawnTempBased && (reason == SpawnReason.NATURAL || reason == SpawnReason.CHUNK_GENERATION))
		{
			float temp = biome.getTemperature(getPosition());
			int subtype = (singleVariant) ? defVariant : randomTempBased(temp);
			dataManager.set(SUB_TYPE, subtype);
		}
		else if (reason != SpawnReason.CONVERSION)
		{
			int subtype = (singleVariant) ? defVariant : randomVariant();
			dataManager.set(SUB_TYPE, subtype);
		}

		if (difficultyIn.getDifficulty() == Difficulty.HARD)
		{
			this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).applyModifier(new AttributeModifier("difficulty", 3.0, Operation.ADDITION));
		}

		spawnDataIn = super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);

		// calculate new health using horizontal distance (i.e. ignore Y)
		BlockPos pos = this.getPosition();
		BlockPos spos = worldIn.getDimension().getSpawnPoint();
		double x0 = (double) spos.getX();
		double z0 = (double) spos.getZ();
		double xzf = worldIn.getDimension().getMovementFactor();
		if (xzf != 1.0)
		{
			x0 /= xzf;
			z0 /= xzf;
		}
		double dx = (double) pos.getX() - x0;
		double dz = (double) pos.getZ() - z0;
		double d2 = dx * dx + dz * dz;
		double temp;
		if (d2 >= MyConfig.maxDistanceSq)
			temp = MyConfig.maxHealth;
		else if (d2 <= MyConfig.minDistanceSq)
			temp = MyConfig.minHealth;
		else
		{
			double p = (Math.sqrt(d2) - MyConfig.minDistance) / (MyConfig.maxDistance - MyConfig.minDistance);
			temp = ((MyConfig.maxHealth - MyConfig.minHealth) * p + MyConfig.minHealth);
		}
		float health = (float) Math.floor(temp);
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(health);
		this.setHealth(getMaxHealth());

		// adjust by biome
		String keyPrefix = biome.getRegistryName() + ":";
		AdjParms adj = biomeMap.get(keyPrefix + getSubType());
		if (adj == null)
			adj = biomeMap.get(keyPrefix + "*");
		if (adj != null)
		{
			double val = adj.hp;
			if (val != 0.0)
			{
				this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(new AttributeModifier("biome", val, Operation.MULTIPLY_BASE));
				this.setHealth(getMaxHealth());
			}
			val = adj.speed;
			if (val != 0.0)
			{
				this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(new AttributeModifier("biome", val, Operation.MULTIPLY_BASE));
			}
		}

		setPotionsBasedOnDifficulty(difficultyIn);

		return spawnDataIn;
	}

	public static void configUpdate()
	{
		setupWeights();
		setupBiomes();
	}

	private static void setupBiomes()
	{
		String[] array = MyConfig.biomeAdjustments;
		biomeMap.clear();
		for (int i = 0; i < array.length; ++i)
		{
			String line = array[i].trim();
			String[] fields = line.split(",");
			try
			{
				// biome, variant, hp, speed
				if (fields.length != 4)
					throw new Exception("bad number of fields");
				String biomeName = fields[0].trim();
				String variant = fields[1].trim();
				Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(biomeName));
				if (biome == null)
					throw new Exception("bad biome value");
				if (!variant.equals("*"))
				{
					int j = Integer.parseInt(variant);
					if (j < 0 || j >= NVARIANTS)
						throw new Exception("bad variant value");
					variant = Integer.toString(j);
				}
				biomeMap.put(biomeName + ":" + variant, new AdjParms(Double.parseDouble(fields[2]), Double.parseDouble(fields[3])));
			}
			catch (Exception e)
			{
				LOGGER.warn("Skipping bad Biome adjustment= " + line);
				LOGGER.warn("Reason= " + e.getMessage());
			}
		}
	}

	private static void setupWeights()
	{
		int[] array = MyConfig.spawnVariantWeights;
		int count = 0;
		boolean first = true;
		for (int i = 0; i < NVARIANTS; ++i)
		{
			if (array[i] > 0)
			{
				++count;
				if (first)
				{
					first = false;
					defVariant = i;
				}
			}
		}
		if (count <= 1)
			singleVariant = true;
	}

	private int randomVariant()
	{
		return randomWeight(MyConfig.spawnVariantWeights);
	}

	private int randomTempBased(float temp)
	{
		int[] standard = MyConfig.spawnVariantWeights;
		int[] choices = new int[NVARIANTS];

		for (int i = 0; i < NVARIANTS; ++i)
			choices[i] = standard[i];
		if (!(temp > 0.2F && temp < 1.2F)) choices[0] = 0;
		if (!(temp > 1.0F)) choices[1] = 0;
		if (!(temp < 0.4F)) choices[2] = 0;

		return randomWeight(choices);
	}

	private int randomWeight(int[] weights)
	{
		int sum = 0;
		for (int i = 0; i < NVARIANTS; ++i)
			sum += weights[i];
		if (sum > 0)
		{
			int j = rand.nextInt(sum);
			for (int i = 0; i < NVARIANTS; ++i)
			{
				if (j < weights[i])
					return i;
				j -= weights[i];
			}
		}
		return defVariant;
	}

	public static boolean isValidLightLevel(IWorld worldIn, BlockPos pos, Random randomIn)
	{
		if (worldIn.getLightFor(LightType.SKY, pos) > randomIn.nextInt(32))
			return false;
		int i = worldIn.getWorld().isThundering() ? worldIn.getNeighborAwareLightSubtracted(pos, 10)
				: worldIn.getLight(pos);
		return i <= randomIn.nextInt(MyConfig.spawnLightLevel + 1);
	}

	public static boolean canSpawn(EntityType<? extends NastySkeletonEntity> type, IWorld worldIn, SpawnReason reason,
			BlockPos pos, Random randomIn)
	{
		return worldIn.getDifficulty() != Difficulty.PEACEFUL && isValidLightLevel(worldIn, pos, randomIn) && canSpawnOn(type, worldIn, reason, pos, randomIn);
	}

	protected void setPotionsBasedOnDifficulty(DifficultyInstance difficulty)
	{
		int amp = 2;
		if (difficulty.getDifficulty() == Difficulty.HARD)
			++amp;
		this.addPotionEffect(new EffectInstance(Effects.JUMP_BOOST, Integer.MAX_VALUE, amp));
	}

	@Override
	protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty)
	{
		super.setEquipmentBasedOnDifficulty(difficulty);

		ItemStack stack = getItemStackFromSlot(EquipmentSlotType.HEAD);
		if (stack.isEmpty() || stack.getItem() == Items.LEATHER_HELMET)
		{
			stack = new ItemStack(Items.LEATHER_HELMET);
			CompoundNBT nbt = stack.getOrCreateChildTag("display");
			int color;
			int subtype = getSubType();
			if (subtype == 0)
				color = 0x00FF00;
			else if (subtype == 1)
				color = 0xFF0000;
			else if (subtype == 2)
				color = 0xFF00FF;
			else if (subtype == 3)
				color = 0x0080FF;
			else if (subtype == 4)
				color = 0xFF6A00;
			else
				color = 0xFFD800;
			nbt.putInt("color", color);
			this.setItemStackToSlot(EquipmentSlotType.HEAD, stack);
		}
	}

	@Override
	public void attackEntityWithRangedAttack(LivingEntity target, float distanceFactor)
	{
		ItemStack itemstack = this.findAmmo(this.getHeldItem(ProjectileHelper.getHandWith(this, Items.BOW)));
		AbstractArrowEntity abstractarrowentity = this.fireArrow(itemstack, distanceFactor);
		if (this.getHeldItemMainhand().getItem() instanceof BowItem)
			abstractarrowentity = ((BowItem) this.getHeldItemMainhand().getItem())
					.customeArrow(abstractarrowentity);
		enchantArrow(abstractarrowentity);
		double d0 = target.getPosX() - this.getPosX();
		double d1 = target.getPosYHeight(0.3333333333333333D) - abstractarrowentity.getPosY();
		double d2 = target.getPosZ() - this.getPosZ();
		double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
		if (d3 > 40.0)
			return;
		double velocity = 1.6;
		d1 = ArrowHelper.computeY(d3, d1, velocity, abstractarrowentity.getPosY(), target.getPosY(), target.getHeight());
		float inaccuracy = 0.0F;
		if (rand.nextDouble() < MyConfig.addInaccuracy)
			inaccuracy = (float) (14 - world.getDifficulty().getId() * 4);
		abstractarrowentity.shoot(d0, d1, d2, (float) velocity, inaccuracy);
		this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
		this.world.addEntity(abstractarrowentity);
	}

	@Override
	public ItemStack findAmmo(ItemStack shootable)
	{
		if (getSubType() == 4)
			return new ItemStack(ModItems.EXPLOSIVE_ARROW);
		return super.findAmmo(shootable);
	}

	protected void enchantArrow(AbstractArrowEntity entity)
	{
		// innate power and knockback
		int i;
		int j = 1;
		if (world.getDifficulty() == Difficulty.HARD)
		{
			j += 1;
			i = MyConfig.bonusHardPower;
		}
		else
			i = MyConfig.bonusPower;

		if (i > 0)
			entity.setDamage(entity.getDamage() + (double) (i + 1) * 0.5D);

		if (j > 0)
			entity.setKnockbackStrength(j);

		// set custom enchantment based on subtype
		int subtype = getSubType();
		if (subtype == 0)
		{
			((ArrowEntity) entity).addEffect(new EffectInstance(Effects.POISON, 160));
		}
		else if (subtype == 1)
		{
			entity.setFire(100);
		}
		else if (subtype == 2)
		{
			((ArrowEntity) entity).addEffect(new EffectInstance(Effects.INSTANT_DAMAGE, 1));
		}
		else if (subtype == 3)
		{
			((ArrowEntity) entity).addEffect(new EffectInstance(Effects.LEVITATION, 140));
		}
		else if (subtype == 5)
		{
			((ArrowEntity) entity).addEffect(new EffectInstance(Effects.NAUSEA, 140, 1));
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount)
	{
		Entity entity = source.getTrueSource();
		if (amount > 0.0F && entity != null && entity instanceof ServerPlayerEntity)
		{
			PlayerEntity player = (PlayerEntity) entity;
			IAttributeInstance attr = this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
			double new_range = 64.0;
			if (new_range > attr.getBaseValue())
				attr.setBaseValue(new_range);
			this.setRevengeTarget(player);
		}
		// Infect wolves that attack this skeleton
		boolean flag = super.attackEntityFrom(source, amount);
		entity = source.getImmediateSource();
		if (flag && entity instanceof WolfEntity)
		{
			WolfEntity mob = (WolfEntity) entity;
			if (NastyWolfEntity.canInfect(mob))
				NastyWolfEntity.onInfect(mob);
		}
		return flag;
	}

	@Override
	protected void damageEntity(DamageSource source, float damageAmount)
	{
		if (source.isProjectile())
			damageAmount *= MyConfig.arrowDamageMultiplier;
		super.damageEntity(source, damageAmount);
	}

	private static final EquipmentSlotType[] copyList = {EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS,
			EquipmentSlotType.FEET, EquipmentSlotType.MAINHAND, EquipmentSlotType.OFFHAND};

	@Override
	public void onInfect(Entity mob)
	{
		World world = mob.world;
		Vec3d mobpos = mob.getPositionVec();
		if (mob.onGround)
			mob.setMotion(0, 0, 0);

		NastySkeletonEntity newmob = ModEntities.NASTY_SKELETON.create(world);
		if (mob.hasCustomName()) {
			newmob.setCustomName(mob.getCustomName());
			newmob.setCustomNameVisible(mob.isCustomNameVisible());
		}
		newmob.setInvisible(mob.isInvisible());
		newmob.setInvulnerable(mob.isInvulnerable());
		newmob.setLocationAndAngles(mobpos.getX(), mobpos.getY(), mobpos.getZ(), mob.rotationYaw, mob.rotationPitch);
		newmob.dataManager.set(SUB_TYPE, getSubType());

		newmob.onInitialSpawn(world, world.getDifficultyForLocation(new BlockPos(mobpos)), SpawnReason.CONVERSION, (ILivingEntityData) null,
				(CompoundNBT) null);

		if (mob instanceof MobEntity)
		{
			MobEntity from = (MobEntity) mob;
			ItemStack stack;
			for (int i = 0; i < copyList.length; ++i)
			{
				EquipmentSlotType slot = copyList[i];
				stack = from.getItemStackFromSlot(slot);
				newmob.setItemStackToSlot(slot, stack);
			}
			if (from.isNoDespawnRequired())
				newmob.enablePersistence();
		    newmob.setNoAI(from.isAIDisabled());
		    newmob.setRevengeTarget(from.getRevengeTarget());
			newmob.rotationYawHead = from.rotationYawHead;
			newmob.renderYawOffset = from.rotationYawHead;
		}
		Entity riding = mob.getRidingEntity();
		if (riding != null)
		{
			mob.stopRiding();
			newmob.startRiding(riding, true);
		}
		else
			newmob.setMotion(mob.getMotion());

		// SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED
	    world.playEvent((PlayerEntity)null, 1027, new BlockPos(newmob), 0);
		world.addEntity(newmob);
		mob.remove();
	}
	
	private static class AdjParms
	{
		public double hp;
		public double speed;
		public AdjParms(double hp, double speed)
		{
			this.hp = hp;
			this.speed = speed;
		}
	}
}
