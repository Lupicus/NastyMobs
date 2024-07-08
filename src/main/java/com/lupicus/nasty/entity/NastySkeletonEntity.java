package com.lupicus.nasty.entity;

import java.util.HashMap;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.lupicus.nasty.Main;
import com.lupicus.nasty.config.MyConfig;
import com.lupicus.nasty.entity.ai.controller.JumpMovementController;
import com.lupicus.nasty.entity.ai.goal.SpreadVirusGoal;
import com.lupicus.nasty.item.ModItems;
import com.lupicus.nasty.pathfinding.JumpPathNavigator;
import com.lupicus.nasty.util.ArrowHelper;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments.Mutable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;

public class NastySkeletonEntity extends AbstractSkeleton implements IHasVirus
{
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final int NVARIANTS = 6;
	private static int defVariant = 0;
	private static boolean singleVariant = false;
	private static HashMap<String,AdjParms> biomeMap = new HashMap<>();
	private static final EntityDataAccessor<Integer> SUB_TYPE = SynchedEntityData.defineId(NastySkeletonEntity.class, EntityDataSerializers.INT);
	private static final ResourceLocation HARD_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(Main.MODID, "difficulty");
	private static final ResourceLocation BIOME_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(Main.MODID, "biome");
	private int adjPunch = 0;
	private ItemEnchantments bowEnchantments = null;
	private ItemEnchantments adjEnchantments = null;
	private final RangedBowAttackGoal<NastySkeletonEntity> bowGoal = new RangedBowAttackGoal<>(this, 1.0D, 20, 40.0F);
	private final MeleeAttackGoal meleeGoal = new MeleeAttackGoal(this, 1.2D, false) {
		/**
		 * Reset the task's internal state. Called when this task is interrupted by another one
		 */
		@Override
		public void stop() {
			super.stop();
			NastySkeletonEntity.this.setAggressive(false);
		}

		/**
		 * Execute a one shot task or start executing a continuous task
		 */
		@Override
		public void start() {
			super.start();
			NastySkeletonEntity.this.setAggressive(true);
		}
	};

	public NastySkeletonEntity(EntityType<? extends NastySkeletonEntity> type, Level worldIn)
	{
		super(type, worldIn);
		reassessWeaponGoal();
		moveControl = new JumpMovementController(this);
	}

	@Override
	protected SoundEvent getAmbientSound()
	{
		return SoundEvents.SKELETON_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn)
	{
		return SoundEvents.SKELETON_HURT;
	}

	@Override
	protected SoundEvent getDeathSound()
	{
		return SoundEvents.SKELETON_DEATH;
	}

	@Override
	protected SoundEvent getStepSound()
	{
		return SoundEvents.SKELETON_STEP;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return AbstractSkeleton.createAttributes()
				.add(Attributes.MOVEMENT_SPEED, 0.30D)
				.add(Attributes.FOLLOW_RANGE, 24.0D)
				.add(Attributes.ATTACK_DAMAGE, 4.0D);
	}

	@Override
	protected void registerGoals()
	{
		if (MyConfig.virusChance > 0 && MyConfig.virusDistance > 0)
			this.goalSelector.addGoal(1, new SpreadVirusGoal(this, Skeleton.class, MyConfig.virusDistance, MyConfig.virusChance));
		this.goalSelector.addGoal(2, new RestrictSunGoal(this));
		this.goalSelector.addGoal(3, new FleeSunGoal(this, 1.0D));
		//this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Wolf.class, 6.0F, 1.0D, 1.2D));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
		//this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder b)
	{
		super.defineSynchedData(b);
		b.define(SUB_TYPE, 0);
	}

	@Override
	protected PathNavigation createNavigation(Level worldIn)
	{
		return new JumpPathNavigator(this, worldIn);
	}

	public int getSubType()
	{
		return entityData.get(SUB_TYPE);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound)
	{
		super.addAdditionalSaveData(compound);
		compound.putByte("SubType", (byte) getSubType());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound)
	{
		super.readAdditionalSaveData(compound);
		entityData.set(SUB_TYPE, (int) compound.getByte("SubType"));
	}

	@Override
	public ResourceKey<LootTable> getDefaultLootTable()
	{
		ResourceLocation res = super.getDefaultLootTable().location();
		return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(res.getNamespace(), res.getPath() + "/" + getSubType()));
	}

	/**
	 * Copied from AbstractSkeleton, so we can use our version of bowGoal
	 */
	@Override
	public void reassessWeaponGoal()
	{
		Level level = this.level();
		if (level != null && !level.isClientSide) {
			if (meleeGoal == null)
				return;
			this.goalSelector.removeGoal(this.meleeGoal);
			this.goalSelector.removeGoal(this.bowGoal);
			ItemStack itemstack = getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof BowItem));
			if (itemstack.getItem() instanceof BowItem) {
				int i = getHardAttackInterval();
				if (level.getDifficulty() != Difficulty.HARD) {
					i = getAttackInterval();
				}

				this.bowGoal.setMinAttackInterval(i);
				this.goalSelector.addGoal(4, this.bowGoal);
			}
			else {
				this.goalSelector.addGoal(4, this.meleeGoal);
			}
		}
	}

	@Override
	@Nullable
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason,
			@Nullable SpawnGroupData spawnDataIn)
	{
		BlockPos pos = blockPosition();
		Biome biome = worldIn.getBiome(pos).value();
		if (MyConfig.spawnTempBased && (reason == MobSpawnType.NATURAL || reason == MobSpawnType.CHUNK_GENERATION))
		{
			@SuppressWarnings("deprecation")
			float temp = biome.getTemperature(pos);
			int subtype = (singleVariant) ? defVariant : randomTempBased(temp);
			entityData.set(SUB_TYPE, subtype);
		}
		else if (reason != MobSpawnType.CONVERSION)
		{
			int subtype = (singleVariant) ? defVariant : randomVariant();
			entityData.set(SUB_TYPE, subtype);
		}

		if (difficultyIn.getDifficulty() == Difficulty.HARD)
		{
			this.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier(HARD_MODIFIER_ID, 3.0, Operation.ADD_VALUE));
		}

		spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn);

		// calculate new health using horizontal distance (i.e. ignore Y)
		LevelData winfo = worldIn.getLevelData();
		BlockPos spos = winfo.getSpawnPos();
		double x0 = (double) spos.getX();
		double z0 = (double) spos.getZ();
		double xzf = worldIn.dimensionType().coordinateScale();
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
		this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
		this.setHealth(getMaxHealth());

		// adjust by biome
		String keyPrefix = worldIn.registryAccess().registryOrThrow(Registries.BIOME).getKey(biome) + ":";
		AdjParms adj = biomeMap.get(keyPrefix + getSubType());
		if (adj == null)
			adj = biomeMap.get(keyPrefix + "*");
		if (adj != null)
		{
			double val = adj.hp;
			if (val != 0.0)
			{
				this.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier(BIOME_MODIFIER_ID, val, Operation.ADD_MULTIPLIED_BASE));
				this.setHealth(getMaxHealth());
			}
			val = adj.speed;
			if (val != 0.0)
			{
				this.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(new AttributeModifier(BIOME_MODIFIER_ID, val, Operation.ADD_MULTIPLIED_BASE));
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
				ResourceLocation res = ResourceLocation.parse(biomeName);
//				Biome biome = ForgeRegistries.BIOMES.getValue(res);
//				if (biome == null)
//					throw new Exception("bad biome value");
				biomeName = res.toString();
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
			int j = random.nextInt(sum);
			for (int i = 0; i < NVARIANTS; ++i)
			{
				if (j < weights[i])
					return i;
				j -= weights[i];
			}
		}
		return defVariant;
	}

	public static boolean isDarkEnoughToSpawn(ServerLevelAccessor worldIn, BlockPos pos, RandomSource randomIn)
	{
		if (worldIn.getBrightness(LightLayer.SKY, pos) > randomIn.nextInt(32))
			return false;
		DimensionType dimensiontype = worldIn.dimensionType();
		int i = dimensiontype.monsterSpawnBlockLightLimit() + MyConfig.spawnLightAdj2;
		if (i < 15 && worldIn.getBrightness(LightLayer.BLOCK, pos) > i)
			return false;
		int j = worldIn.getLevel().isThundering() ? worldIn.getMaxLocalRawBrightness(pos, 10)
				: worldIn.getMaxLocalRawBrightness(pos);
		return j <= dimensiontype.monsterSpawnLightTest().sample(randomIn) + randomIn.nextInt(MyConfig.spawnLightAdj + 1);
	}

	public static boolean checkSpawnRules(EntityType<? extends NastySkeletonEntity> type, ServerLevelAccessor worldIn, MobSpawnType reason,
			BlockPos pos, RandomSource randomIn)
	{
		return worldIn.getDifficulty() != Difficulty.PEACEFUL && (MobSpawnType.ignoresLightRequirements(reason) || isDarkEnoughToSpawn(worldIn, pos, randomIn)) && checkMobSpawnRules(type, worldIn, reason, pos, randomIn);
	}

	protected void setPotionsBasedOnDifficulty(DifficultyInstance difficulty)
	{
		int amp = 2;
		if (difficulty.getDifficulty() == Difficulty.HARD)
			++amp;
		this.addEffect(new MobEffectInstance(MobEffects.JUMP, MobEffectInstance.INFINITE_DURATION, amp));
	}

	@Override
	protected void populateDefaultEquipmentSlots(RandomSource randomIn, DifficultyInstance difficulty)
	{
		super.populateDefaultEquipmentSlots(randomIn, difficulty);

		ItemStack stack = getItemBySlot(EquipmentSlot.HEAD);
		if (stack.isEmpty() || stack.getItem() == Items.LEATHER_HELMET)
		{
			stack = new ItemStack(Items.LEATHER_HELMET);
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
			stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color, true));
			this.setItemSlot(EquipmentSlot.HEAD, stack);
		}
	}

	@Override
	public void performRangedAttack(LivingEntity target, float distanceFactor)
	{
		ItemStack bow = getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof BowItem));
		ItemStack itemstack = getProjectile(bow);
		AbstractArrow abstractarrowentity = this.getArrow(itemstack, distanceFactor, bow);
		if (bow.getItem() instanceof BowItem bowItem)
			abstractarrowentity = bowItem.customArrow(abstractarrowentity);
		enchantArrow(abstractarrowentity);
		double d0 = target.getX() - this.getX();
		double d1 = target.getY(0.3333333333333333D) - abstractarrowentity.getY();
		double d2 = target.getZ() - this.getZ();
		double d3 = Math.sqrt(d0 * d0 + d2 * d2);
		if (d3 > 40.0)
			return;
		double velocity = 1.6;
		d1 = ArrowHelper.computeY(d3, d1, velocity, abstractarrowentity.getY(), target.getY(), target.getBbHeight());
		float inaccuracy = 0.0F;
		if (random.nextDouble() < MyConfig.addInaccuracy)
			inaccuracy = (float) (14 - level().getDifficulty().getId() * 4);
		abstractarrowentity.shoot(d0, d1, d2, (float) velocity, inaccuracy);
		this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
		this.level().addFreshEntity(abstractarrowentity);
	}

	@Override
	public boolean canFireProjectileWeapon(ProjectileWeaponItem item) {
		return item instanceof BowItem;
	}

	@Override
	public ItemStack getProjectile(ItemStack shootable)
	{
		int subtype = getSubType();
		if (subtype == 2)
			return new ItemStack(ModItems.MAGIC_ARROW);
		if (subtype == 4)
			return new ItemStack(ModItems.EXPLOSIVE_ARROW);
		return super.getProjectile(shootable);
	}

	protected void enchantArrow(AbstractArrow entity)
	{
		// innate power and knockback
		int i;
		int j = 1;
		if (level().getDifficulty() == Difficulty.HARD)
		{
			j += 1;
			i = MyConfig.bonusHardPower;
		}
		else
			i = MyConfig.bonusPower;

		if (i > 0)
			entity.setBaseDamage(entity.getBaseDamage() + (double) (i + 1) * 0.5D);

		if (j > 0)
		{
			// add innate punch to bow copy
			ItemStack bow = entity.getWeaponItem();
			if (bow != null)
			{
				ItemEnchantments enchantments = bow.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
				if (j != adjPunch || enchantments != bowEnchantments)
				{
					adjPunch = j;
					bowEnchantments = enchantments;
					Mutable mutable = new ItemEnchantments.Mutable(enchantments);
					Reference<Enchantment> punch = level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PUNCH);
					mutable.upgrade(punch, j);
					adjEnchantments = mutable.toImmutable();
				}
				bow.set(DataComponents.ENCHANTMENTS, adjEnchantments);
			}
		}

		// set custom enchantment based on subtype
		int subtype = getSubType();
		if (subtype == 0)
		{
			((Arrow) entity).addEffect(new MobEffectInstance(MobEffects.POISON, MyConfig.poisonTime));
		}
		else if (subtype == 1)
		{
			entity.setRemainingFireTicks(100);
		}
//		else if (subtype == 2)
//		{
//			((ArrowEntity) entity).addEffect(new EffectInstance(Effects.INSTANT_DAMAGE, 1));
//		}
		else if (subtype == 3)
		{
			((Arrow) entity).addEffect(new MobEffectInstance(MobEffects.LEVITATION, 140));
		}
		else if (subtype == 5)
		{
			Holder<MobEffect> effect = MyConfig.useBlindness ? MobEffects.BLINDNESS : MobEffects.CONFUSION;
			((Arrow) entity).addEffect(new MobEffectInstance(effect, MyConfig.yellowTime, 1));
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount)
	{
		Entity entity = source.getEntity();
		if (amount > 0.0F && entity != null && entity instanceof ServerPlayer)
		{
			Player player = (Player) entity;
			AttributeInstance attr = this.getAttribute(Attributes.FOLLOW_RANGE);
			double new_range = 64.0;
			if (new_range > attr.getBaseValue())
				attr.setBaseValue(new_range);
			this.setLastHurtByMob(player);
		}
		// Infect wolves that attack this skeleton
		boolean flag = super.hurt(source, amount);
		entity = source.getDirectEntity();
		if (flag && entity instanceof Wolf)
		{
			Wolf mob = (Wolf) entity;
			if (NastyWolfEntity.canInfect(mob))
				NastyWolfEntity.onInfect(mob);
		}
		return flag;
	}

	@Override
	protected void actuallyHurt(DamageSource source, float damageAmount)
	{
		if (source.is(DamageTypeTags.IS_PROJECTILE))
			damageAmount *= MyConfig.arrowDamageMultiplier;
		super.actuallyHurt(source, damageAmount);
	}

	@Override
	public void onInfect(Entity mob)
	{
		ServerLevel world = (ServerLevel) mob.level();
		Vec3 mobpos = mob.position();
		if (mob.onGround())
			mob.setDeltaMovement(0, 0, 0);

		NastySkeletonEntity newmob = ModEntities.NASTY_SKELETON.create(world);
		if (newmob == null)
			return;
		if (mob.hasCustomName()) {
			newmob.setCustomName(mob.getCustomName());
			newmob.setCustomNameVisible(mob.isCustomNameVisible());
		}
		newmob.setInvisible(mob.isInvisible());
		newmob.setInvulnerable(mob.isInvulnerable());
		newmob.moveTo(mobpos.x(), mobpos.y(), mobpos.z(), mob.getYRot(), mob.getXRot());
		newmob.entityData.set(SUB_TYPE, getSubType());

		newmob.finalizeSpawn(world, world.getCurrentDifficultyAt(BlockPos.containing(mobpos)), MobSpawnType.CONVERSION, (SpawnGroupData) null);

		if (mob instanceof Mob)
		{
			Mob from = (Mob) mob;
			ItemStack stack;
			for (EquipmentSlot slot : EquipmentSlot.values())
			{
				stack = from.getItemBySlot(slot);
				newmob.setItemSlot(slot, stack.copyAndClear());
				newmob.setDropChance(slot, from.getEquipmentDropChance(slot));
			}
			newmob.setCanPickUpLoot(from.canPickUpLoot());
			if (from.isPersistenceRequired())
				newmob.setPersistenceRequired();
			newmob.setNoAi(from.isNoAi());
			newmob.setLastHurtByMob(from.getLastHurtByMob());
			newmob.yHeadRot = from.yHeadRot;
			newmob.yBodyRot = from.yHeadRot;
		}
		Entity riding = mob.getVehicle();
		if (riding != null)
		{
			mob.stopRiding();
			newmob.startRiding(riding, true);
		}
		else
			newmob.setDeltaMovement(mob.getDeltaMovement());

		// SoundEvents.ZOMBIE_VILLAGER_CONVERTED
		world.levelEvent((Player) null, 1027, newmob.blockPosition(), 0);
		world.addFreshEntity(newmob);
		mob.discard();
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
