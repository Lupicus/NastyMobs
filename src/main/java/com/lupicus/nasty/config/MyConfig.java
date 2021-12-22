package com.lupicus.nasty.config;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lupicus.nasty.Main;
import com.lupicus.nasty.entity.NastySkeletonEntity;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod.EventBusSubscriber(modid = Main.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class MyConfig
{
	private static final Logger LOGGER = LogManager.getLogger();
	public static final Common COMMON;
	public static final ForgeConfigSpec COMMON_SPEC;
	static
	{
		final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
		COMMON_SPEC = specPair.getRight();
		COMMON = specPair.getLeft();
	}

	public static double minHealth;
	public static double maxHealth;
	public static double minDistance;
	public static double maxDistance;
	public static double minDistanceSq;
	public static double maxDistanceSq;
	public static double arrowDamageMultiplier;
	public static double addInaccuracy;
	public static boolean explosiveArrowOnArmor;
	public static boolean explosiveArrowOnBlock;
	public static boolean explosiveArrowOnShield;
	public static boolean useBlindness;
	public static float explosiveArrowStrength;
	public static float virusDistance;
	public static float virusChance;
	public static float virusChance2;
	public static int bonusPower;
	public static int bonusHardPower;
	public static int magicDamage;
	public static int poisonTime;
	public static int yellowTime;
	public static int spawnBiome;
	public static int spawnFeature;
	public static int spawnDungeon;
	public static int spawnLightLevel;
	public static boolean spawnTempBased;
	public static int[] spawnVariantWeights;
	public static String[] biomeAdjustments;
	public static VMode virusMode2;

	@SubscribeEvent
	public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent)
	{
		if (configEvent.getConfig().getSpec() == MyConfig.COMMON_SPEC)
		{
			bakeConfig();
		}
	}

	public static void bakeConfig()
	{
		check(COMMON.minHealth, COMMON.maxHealth);
		minHealth = COMMON.minHealth.get();
		maxHealth = COMMON.maxHealth.get();
		check(COMMON.minDistance, COMMON.maxDistance);
		minDistance = COMMON.minDistance.get();
		minDistanceSq = minDistance * minDistance;
		maxDistance = COMMON.maxDistance.get();
		maxDistanceSq = maxDistance * maxDistance;
		arrowDamageMultiplier = COMMON.arrowDamageMultiplier.get();
		addInaccuracy = COMMON.addInaccuracy.get();
		bonusPower = COMMON.bonusPower.get();
		bonusHardPower = COMMON.bonusHardPower.get();
		magicDamage = COMMON.magicDamage.get();
		poisonTime = COMMON.poisonTime.get() * 20;
		yellowTime = COMMON.yellowTime.get() * 20;
		spawnBiome = COMMON.spawnBiome.get();
		spawnFeature = COMMON.spawnFeature.get();
		spawnDungeon = COMMON.spawnDungeon.get();
		spawnLightLevel = COMMON.spawnLightLevel.get();
		explosiveArrowOnArmor = COMMON.explosiveArrowOnArmor.get();
		explosiveArrowOnBlock = COMMON.explosiveArrowOnBlock.get();
		explosiveArrowOnShield = COMMON.explosiveArrowOnShield.get();
		useBlindness = COMMON.useBlindness.get();
		explosiveArrowStrength = (float) COMMON.explosiveArrowStrength.get().doubleValue();
		virusDistance = (float) COMMON.virusDistance.get().doubleValue();
		virusChance = (float) COMMON.virusChance.get().doubleValue();
		virusChance2 = (float) COMMON.virusChance2.get().doubleValue();
		spawnTempBased = COMMON.spawnTempBased.get();
		spawnVariantWeights = extract(NastySkeletonEntity.NVARIANTS, COMMON.spawnVariantWeights.get());
		biomeAdjustments = extract(COMMON.biomeAdjustments.get());
		virusMode2 = COMMON.virusMode2.get();
		NastySkeletonEntity.configUpdate();
	}

	private static void check(DoubleValue min, DoubleValue max)
	{
		double v1, v2;
		v1 = min.get();
		v2 = max.get();
		if (v1 > v2)
		{
			min.set(v2);
			max.set(v1);
		}
	}

	private static int[] extract(int num, String value)
	{
		int count = 0;
		int[] ret = new int[num];
		String[] list = value.split(",");
		int len = list.length;
		if (len != num)
		{
			LOGGER.warn("Bad sequence; excepted " + num + " values but has " + len);
			LOGGER.warn("for value= " + value);
		}
		if (len > num)
			len = num;
		for (int i = 0; i < len; ++i)
		{
			try
			{
				ret[i] = Integer.parseInt(list[i]);
			}
			catch (NumberFormatException e)
			{
				++count;
			}
		}
		if (count > 0)
		{
			LOGGER.warn("Encountered " + count + " parsing errors");
			LOGGER.warn("for value= " + value);
		}
		for (int i = 0; i < num; ++i)
		{
			if (ret[i] < 0)
				ret[i] = 0;
		}
		return ret;
	}

	private static String[] extract(String value)
	{
		return value.isEmpty() ? new String[0] : value.split(";");
	}

	public static class Common
	{
		public final DoubleValue minHealth;
		public final DoubleValue maxHealth;
		public final DoubleValue minDistance;
		public final DoubleValue maxDistance;
		public final DoubleValue arrowDamageMultiplier;
		public final DoubleValue addInaccuracy;
		public final BooleanValue explosiveArrowOnArmor;
		public final BooleanValue explosiveArrowOnBlock;
		public final BooleanValue explosiveArrowOnShield;
		public final BooleanValue useBlindness;
		public final DoubleValue explosiveArrowStrength;
		public final DoubleValue virusDistance;
		public final DoubleValue virusChance;
		public final DoubleValue virusChance2;
		public final IntValue bonusPower;
		public final IntValue bonusHardPower;
		public final IntValue magicDamage;
		public final IntValue poisonTime;
		public final IntValue yellowTime;
		public final IntValue spawnBiome;
		public final IntValue spawnFeature;
		public final IntValue spawnDungeon;
		public final IntValue spawnLightLevel;
		public final BooleanValue spawnTempBased;
		public final ConfigValue<String> spawnVariantWeights;
		public final ConfigValue<String> biomeAdjustments;
		public final EnumValue<VMode> virusMode2;

		public Common(ForgeConfigSpec.Builder builder)
		{
			String baseTrans = Main.MODID + ".config.";
			String sectionTrans;

			builder.push("Nasty Skeleton");
			sectionTrans = baseTrans + "skeleton.";
			minHealth = builder
					.comment("Minimum health")
					.translation(sectionTrans + "min_health")
					.defineInRange("MinHealth", () -> 20.0, 4.0, 5000.0);
			maxHealth = builder
					.comment("Maximum health")
					.translation(sectionTrans + "max_health")
					.defineInRange("MaxHealth", () -> 46.0, 20.0, 5000.0);
			minDistance = builder
					.comment("Minimum Distance")
					.translation(sectionTrans + "min_distance")
					.defineInRange("MinDistance", () -> 500.0, 0.0, 1280000.0);
			maxDistance = builder
					.comment("Maximum Distance")
					.translation(sectionTrans + "max_distance")
					.defineInRange("MaxDistance", () -> 22500.0, 0.0, 1280000.0);
			bonusPower = builder
					.comment("Bonus Power")
					.translation(sectionTrans + "bonus_power")
					.defineInRange("BonusPower", 3, 0, 10);
			bonusHardPower = builder
					.comment("Bonus Power for Hard")
					.translation(sectionTrans + "bonus_hard_power")
					.defineInRange("BonusHardPower", 7, 0, 20);
			magicDamage = builder
					.comment("Magic Damage for arrow used by purple skeleton")
					.translation(sectionTrans + "magic_damage")
					.defineInRange("MagicDamage", 3, 0, 50);
			poisonTime = builder
					.comment("Poison Time")
					.translation(sectionTrans + "poison_time")
					.defineInRange("PoisonTime", 9, 1, 30);
			yellowTime = builder
					.comment("Yellow skeleton effect Time")
					.translation(sectionTrans + "yellow_time")
					.defineInRange("YellowTime", 7, 1, 30);
			spawnBiome = builder
					.comment("Spawn in Biome weight")
					.translation(sectionTrans + "spawn_biome")
					.defineInRange("SpawnBiome", 100, 0, 200);
			spawnFeature = builder
					.comment("Spawn in Feature weight")
					.translation(sectionTrans + "spawn_feature")
					.defineInRange("SpawnFeature", 5, 0, 20);
			spawnDungeon = builder
					.comment("Spawn in Dungeon weight")
					.translation(sectionTrans + "spawn_dungeon")
					.defineInRange("SpawnDungeon", 100, 0, 200);
			spawnLightLevel = builder
					.comment("Spawn light level (use 7 to be same as normal skeletons)")
					.translation(sectionTrans + "spawn_light_level")
					.defineInRange("SpawnLightLevel", 9, 0, 15);
			arrowDamageMultiplier = builder
					.comment("Arrow Damage Multiplier (damage to skeleton)")
					.translation(sectionTrans + "arrow_damage_multiplier")
					.defineInRange("ArrowDamageMultiplier", () -> 0.5, 0.0, 1.0);
			addInaccuracy = builder
					.comment("Add inaccuracy to shot")
					.translation(sectionTrans + "add_inaccuracy")
					.defineInRange("AddInaccuracy", () -> 0.7, 0.0, 1.0);
			explosiveArrowOnArmor = builder
					.comment("Explosive Arrow cause Armor to drop")
					.translation(sectionTrans + "explosive_arrow_armor")
					.define("ExplosiveArrowOnArmor", true);
			explosiveArrowOnBlock = builder
					.comment("Explosive Arrow cause Block to drop")
					.translation(sectionTrans + "explosive_arrow_block")
					.define("ExplosiveArrowOnBlock", true);
			explosiveArrowOnShield = builder
					.comment("Explosive Arrow cause Shield to drop")
					.translation(sectionTrans + "explosive_arrow_shield")
					.define("ExplosiveArrowOnShield", false);
			useBlindness = builder
					.comment("Use blindness instead of nausea for the yellow skeleton")
					.translation(sectionTrans + "use_blindness")
					.define("UseBlindness", false);
			explosiveArrowStrength = builder
					.comment("Explosive Arrow strength")
					.translation(sectionTrans + "explosive_arrow_strength")
					.defineInRange("ExplosiveArrowStrength", () -> 8.0, 1.0, 1500.0);
			virusDistance = builder
					.comment("Virus Distance for spreading")
					.translation(sectionTrans + "virus_distance")
					.defineInRange("VirusDistance", () -> 5.0, 0.0, 20.0);
			virusChance = builder
					.comment("Virus Chance for spreading")
					.translation(sectionTrans + "virus_chance")
					.defineInRange("VirusChance", () -> 0.02, 0.0, 1.0);
			spawnTempBased = builder
					.comment("Spawn type based on Tempature")
					.translation(sectionTrans + "spawn_type_temp_based")
					.define("SpawnTempBased", false);
			spawnVariantWeights = builder
					.comment("Spawn weights (Green,Red,Purple,Cyan,Orange,Yellow)")
					.translation(sectionTrans + "spawn_weights")
					.define("SpawnVariantWeights", "5,5,5,5,5,1");
			biomeAdjustments = builder
					.comment("Biome adjustments (biome,variant,hp,speed)")
					.translation(sectionTrans + "biome_adjuments")
					.define("BiomeAdjustments", "minecraft:plains,*,-0.2,0.2;minecraft:swamp,0,0.2,0.0;minecraft:swamp,*,0.1,-0.1;minecraft:badlands,*,0.3,0.3;minecraft:dark_forest_hills,*,0.3,0.3");
			builder.pop();

			builder.push("Nasty Wolf");
			sectionTrans = baseTrans + "wolf.";
			virusMode2 = builder
					.comment("Virus Mode")
					.translation(sectionTrans + "virus_mode")
					.defineEnum("VirusMode", VMode.WILD);
			virusChance2 = builder
					.comment("Virus Chance for spreading")
					.translation(sectionTrans + "virus_chance")
					.defineInRange("VirusChance", () -> 0.25, 0.0, 1.0);
			builder.pop();
		}
	}

	public enum VMode
	{
		OFF,
		WILD,
		UNNAMED
	}
}
