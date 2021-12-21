package com.lupicus.nasty.util;

import java.util.ArrayList;
import java.util.List;

import com.lupicus.nasty.entity.ModEntities;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraftforge.common.world.MobSpawnInfoBuilder;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.StructureSpawnListGatherEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SpawnData
{
	@SubscribeEvent
	public static void onBiome(BiomeLoadingEvent event)
	{
		// copy spawn for biomes
		MobSpawnInfoBuilder builder = event.getSpawns();
		List<EntityType<?>> mobs = new ArrayList<>();
		List<SpawnerData> list = new ArrayList<>();
		ModEntities.getBiomeSpawnData(mobs, list);
		for (int i = 0; i < mobs.size(); ++i)
		{
			EntityType<?> mob = mobs.get(i);
			for (SpawnerData s : builder.getSpawner(mob.getCategory()))
			{
				if (s.type == mob)
				{
					SpawnerData spawner = list.get(i);
					builder.addSpawn(spawner.type.getCategory(), spawner);
					break;
				}
			}
		}
	}

	@SubscribeEvent
	public static void onStructure(StructureSpawnListGatherEvent event)
	{
		// copy spawn for structures
		List<EntityType<?>> mobs = new ArrayList<>();
		List<SpawnerData> list = new ArrayList<>();
		ModEntities.getFeatureSpawnData(mobs, list);
		for (int i = 0; i < mobs.size(); ++i)
		{
			EntityType<?> mob = mobs.get(i);
			for (SpawnerData s : event.getEntitySpawns(mob.getCategory()))
			{
				if (s.type == mob)
				{
					SpawnerData spawner = list.get(i);
					event.addEntitySpawn(spawner.type.getCategory(), spawner);
					break;
				}
			}
		}
	}
}
