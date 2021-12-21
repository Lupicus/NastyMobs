package com.lupicus.nasty.entity;

import java.util.List;

import com.lupicus.nasty.Main;
import com.lupicus.nasty.config.MyConfig;
import com.lupicus.nasty.renderer.entity.NastySkeletonRenderer;
import com.lupicus.nasty.renderer.entity.NastyWolfRenderer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.common.DungeonHooks;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class ModEntities
{
	public static final EntityType<NastySkeletonEntity> NASTY_SKELETON = register("skeleton", EntityType.Builder.of(NastySkeletonEntity::new, MobCategory.MONSTER).sized(0.6F, 1.99F).clientTrackingRange(8));
	public static final EntityType<NastyWolfEntity> NASTY_WOLF = register("wolf", EntityType.Builder.of(NastyWolfEntity::new, MobCategory.MONSTER).sized(0.6F, 0.85F).clientTrackingRange(8));

	private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder)
	{
		String key = Main.MODID + ":" + name;
		EntityType<T> type = builder.build(key);
		type.setRegistryName(key);
		return type;
	}

	public static void register(IForgeRegistry<EntityType<?>> forgeRegistry)
	{
		forgeRegistry.registerAll(NASTY_SKELETON, NASTY_WOLF);

		SpawnPlacements.register(NASTY_SKELETON, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NastySkeletonEntity::checkSpawnRules);
		SpawnPlacements.register(NASTY_WOLF, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NastyWolfEntity::checkSpawnRules);
	}

    public static void onAttribute(final EntityAttributeCreationEvent event)
    {
    	event.put(NASTY_SKELETON, NastySkeletonEntity.createAttributes().build());
    	event.put(NASTY_WOLF, NastyWolfEntity.createAttributes().build());
    }

    @OnlyIn(Dist.CLIENT)
	public static void setupClient(final RegisterRenderers event)
	{
		event.registerEntityRenderer(NASTY_SKELETON, NastySkeletonRenderer::new);
		event.registerEntityRenderer(NASTY_WOLF, NastyWolfRenderer::new);
	}

	public static void addSpawnData()
	{
		int val = MyConfig.spawnDungeon;
		if (val > 0)
			DungeonHooks.addDungeonMob(NASTY_SKELETON, val);
	}

	public static void removeSpawnData()
	{
		DungeonHooks.removeDungeonMob(NASTY_SKELETON);
	}

	public static void getBiomeSpawnData(List<EntityType<?>> mobs, List<SpawnerData> spawns)
	{
		int val = MyConfig.spawnBiome;
		if (val > 0)
		{
			mobs.add(EntityType.SKELETON);
			spawns.add(new SpawnerData(NASTY_SKELETON, val, 1, 3));
		}
	}

	public static void getFeatureSpawnData(List<EntityType<?>> mobs, List<SpawnerData> spawns)
	{
		int val = MyConfig.spawnFeature;
		if (val > 0)
		{
			mobs.add(EntityType.SKELETON);
			spawns.add(new SpawnerData(NASTY_SKELETON, val, 3, 4));
		}
	}
}
