package com.lupicus.nasty.util;

import java.util.ArrayList;
import java.util.List;

import com.lupicus.nasty.entity.ModEntities;

import net.minecraft.entity.EntityType;
import net.minecraft.world.biome.MobSpawnInfo.Spawners;
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
		List<Spawners> list = new ArrayList<>();
		ModEntities.getBiomeSpawnData(mobs, list);
		for (int i = 0; i < mobs.size(); ++i)
		{
			EntityType<?> mob = mobs.get(i);
			for (Spawners s : builder.getSpawner(mob.getClassification()))
			{
				if (s.field_242588_c == mob)
				{
					Spawners spawner = list.get(i);
					builder.func_242575_a(spawner.field_242588_c.getClassification(), spawner);
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
		List<Spawners> list = new ArrayList<>();
		ModEntities.getFeatureSpawnData(mobs, list);
		for (int i = 0; i < mobs.size(); ++i)
		{
			EntityType<?> mob = mobs.get(i);
			for (Spawners s : event.getEntitySpawns(mob.getClassification()))
			{
				if (s.field_242588_c == mob)
				{
					Spawners spawner = list.get(i);
					event.addEntitySpawn(spawner.field_242588_c.getClassification(), spawner);
					break;
				}
			}
		}
	}
}
