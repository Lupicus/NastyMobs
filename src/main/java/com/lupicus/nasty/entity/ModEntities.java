package com.lupicus.nasty.entity;

import com.lupicus.nasty.Main;
import com.lupicus.nasty.config.MyConfig;
import com.lupicus.nasty.renderer.entity.NastySkeletonRenderer;
import com.lupicus.nasty.util.SpawnData;
import com.lupicus.nasty.renderer.entity.NastyWolfRenderer;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.DungeonHooks;
import net.minecraftforge.registries.IForgeRegistry;

public class ModEntities
{
	public static final EntityType<NastySkeletonEntity> NASTY_SKELETON = register("skeleton", EntityType.Builder.create(NastySkeletonEntity::new, EntityClassification.MONSTER).size(0.6F, 1.99F));
	public static final EntityType<NastyWolfEntity> NASTY_WOLF = register("wolf", EntityType.Builder.create(NastyWolfEntity::new, EntityClassification.MONSTER).size(0.6F, 0.85F));

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

		EntitySpawnPlacementRegistry.register(NASTY_SKELETON, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, NastySkeletonEntity::canSpawn);
	}

	@OnlyIn(Dist.CLIENT)
	public static void register(EntityRendererManager renderManager)
	{
		renderManager.register(NASTY_SKELETON, new NastySkeletonRenderer(renderManager));
		renderManager.register(NASTY_WOLF, new NastyWolfRenderer(renderManager));
	}

	public static void addSpawnData()
	{
		int val = MyConfig.spawnBiome;
		if (val > 0)
			SpawnData.copyBiomeSpawn(EntityType.SKELETON, NASTY_SKELETON, val, 1, 3);
		val = MyConfig.spawnFeature;
		if (val > 0)
			SpawnData.copyFeatureSpawn(EntityType.SKELETON, NASTY_SKELETON, val, 3, 4);
		val = MyConfig.spawnDungeon;
		if (val > 0)
			DungeonHooks.addDungeonMob(NASTY_SKELETON, val);
	}

	public static void removeSpawnData()
	{
		SpawnData.removeBiomeSpawn(NASTY_SKELETON);
		SpawnData.removeFeatureSpawn(NASTY_SKELETON);
		DungeonHooks.removeDungeonMob(NASTY_SKELETON);
	}
}
