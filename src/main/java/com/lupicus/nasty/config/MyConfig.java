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
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod.EventBusSubscriber(modid = Main.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class MyConfig
{
	private static final Logger LOGGER = LogManager.getLogger();
	public static final Server SERVER;
	public static final ForgeConfigSpec SERVER_SPEC;
	static
	{
		final Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Server::new);
		SERVER_SPEC = specPair.getRight();
		SERVER = specPair.getLeft();
	}

	public static double minHealth;
	public static double maxHealth;
	public static double minDistance;
	public static double maxDistance;
	public static double minDistanceSq;
	public static double maxDistanceSq;
	public static double arrowDamageMultiplier;
	public static boolean explosiveArrowOnArmor;
	public static boolean explosiveArrowOnBlock;
	public static boolean explosiveArrowOnShield;
	public static float explosiveArrowStrength;
	public static float virusDistance;
	public static float virusChance;
	public static int spawnBiome;
	public static int spawnFeature;
	public static int spawnDungeon;
	public static boolean spawnTempBased;
	public static int[] spawnVariantWeights;
	public static String[] biomeAdjustments;

	@SubscribeEvent
	public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent)
	{
		if (configEvent.getConfig().getSpec() == MyConfig.SERVER_SPEC)
		{
			bakeConfig();
		}
	}

	public static void bakeConfig()
	{
		check(SERVER.minHealth, SERVER.maxHealth);
		minHealth = SERVER.minHealth.get();
		maxHealth = SERVER.maxHealth.get();
		check(SERVER.minDistance, SERVER.maxDistance);
		minDistance = SERVER.minDistance.get();
		minDistanceSq = minDistance * minDistance;
		maxDistance = SERVER.maxDistance.get();
		maxDistanceSq = maxDistance * maxDistance;
		arrowDamageMultiplier = SERVER.arrowDamageMultiplier.get();
		spawnBiome = SERVER.spawnBiome.get();
		spawnFeature = SERVER.spawnFeature.get();
		spawnDungeon = SERVER.spawnDungeon.get();
		explosiveArrowOnArmor = SERVER.explosiveArrowOnArmor.get();
		explosiveArrowOnBlock = SERVER.explosiveArrowOnBlock.get();
		explosiveArrowOnShield = SERVER.explosiveArrowOnShield.get();
		explosiveArrowStrength = (float) SERVER.explosiveArrowStrength.get().doubleValue();
		virusDistance = (float) SERVER.virusDistance.get().doubleValue();
		virusChance = (float) SERVER.virusChance.get().doubleValue();
		spawnTempBased = SERVER.spawnTempBased.get();
		spawnVariantWeights = extract(NastySkeletonEntity.NVARIANTS, SERVER.spawnVariantWeights.get());
		biomeAdjustments = extract(SERVER.biomeAdjustments.get());
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
		String[] ret = value.split(";");
		return ret;
	}

	public static class Server
	{
		public final DoubleValue minHealth;
		public final DoubleValue maxHealth;
		public final DoubleValue minDistance;
		public final DoubleValue maxDistance;
		public final DoubleValue arrowDamageMultiplier;
		public final BooleanValue explosiveArrowOnArmor;
		public final BooleanValue explosiveArrowOnBlock;
		public final BooleanValue explosiveArrowOnShield;
		public final DoubleValue explosiveArrowStrength;
		public final DoubleValue virusDistance;
		public final DoubleValue virusChance;
		public final IntValue spawnBiome;
		public final IntValue spawnFeature;
		public final IntValue spawnDungeon;
		public final BooleanValue spawnTempBased;
		public final ConfigValue<String> spawnVariantWeights;
		public final ConfigValue<String> biomeAdjustments;

		public Server(ForgeConfigSpec.Builder builder) {
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
					.defineInRange("MaxHealth", () -> 250.0, 100.0, 5000.0);
			minDistance = builder
					.comment("Minimum Distance")
					.translation(sectionTrans + "min_distance")
					.defineInRange("MinDistance", () -> 500.0, 0.0, 1280000.0);
			maxDistance = builder
					.comment("Maximum Distance")
					.translation(sectionTrans + "max_distance")
					.defineInRange("MaxDistance", () -> 4000.0, 0.0, 1280000.0);
			spawnBiome = builder
					.comment("Spawn in Biome weight")
					.translation(sectionTrans + "spawn_biome")
					.defineInRange("SpawnBiome", 25, 0, 100);
			spawnFeature = builder
					.comment("Spawn in Feature weight")
					.translation(sectionTrans + "spawn_feature")
					.defineInRange("SpawnFeature", 2, 0, 20);
			spawnDungeon = builder
					.comment("Spawn in Dungeon weight")
					.translation(sectionTrans + "spawn_dungeon")
					.defineInRange("SpawnDungeon", 15, 0, 100);
			arrowDamageMultiplier = builder
					.comment("Arrow Damage Multiplier (damage to skeleton)")
					.translation(sectionTrans + "arrow_damage_multiplier")
					.defineInRange("arrowDamageMultiplier", () -> 0.5, 0.0, 1.0);
			explosiveArrowOnArmor = builder
					.comment("Explosive Arrow cause Armor to drop")
					.translation(sectionTrans + "explosive_arrow_armor")
					.define("explosiveArrowOnArmor", true);
			explosiveArrowOnBlock = builder
					.comment("Explosive Arrow cause Block to drop")
					.translation(sectionTrans + "explosive_arrow_block")
					.define("explosiveArrowOnBlock", true);
			explosiveArrowOnShield = builder
					.comment("Explosive Arrow cause Shield to drop")
					.translation(sectionTrans + "explosive_arrow_shield")
					.define("explosiveArrowOnShield", false);
			explosiveArrowStrength = builder
					.comment("Explosive Arrow strength")
					.translation(sectionTrans + "explosive_arrow_strength")
					.defineInRange("ExplosiveArrowStrength", () -> 8.0, 1.0, 1500.0);
			virusDistance = builder
					.comment("Virus Distance for spreading")
					.translation(sectionTrans + "virus_distance")
					.defineInRange("virusDistance", () -> 5.0, 0.0, 20.0);
			virusChance = builder
					.comment("Virus Chance for spreading")
					.translation(sectionTrans + "virus_chance")
					.defineInRange("virusChance", () -> 0.02, 0.0, 1.0);
			spawnTempBased = builder
					.comment("Spawn type based on Tempature")
					.translation(sectionTrans + "spawn_type_temp_based")
					.define("spawnTempBased", false);
			spawnVariantWeights = builder
					.comment("Spawn weights (Green,Red,Purple,Cyan,Orange,Yellow)")
					.translation(sectionTrans + "spawn_weights")
					.define("spawnVariantWeights", "5,5,5,5,5,1");
			biomeAdjustments = builder
					.comment("Biome adjustments (biome,variant,hp,speed)")
					.translation(sectionTrans + "biome_adjuments")
					.define("biomeAdjustments", "minecraft:plains,*,-0.2,0.2;minecraft:swamp,0,0.2,0.0;minecraft:swamp,*,0.1,-0.1;minecraft:badlands,*,0.3,0.3;minecraft:dark_forest_hills,*,0.3,0.3");
			builder.pop();
		}
	}
}
