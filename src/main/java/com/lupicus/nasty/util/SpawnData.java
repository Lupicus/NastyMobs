package com.lupicus.nasty.util;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.lupicus.nasty.entity.ModEntities;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride.BoundingBoxType;
import net.minecraftforge.common.world.MobSpawnSettingsBuilder;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.common.world.ModifiableStructureInfo;
import net.minecraftforge.common.world.StructureSettingsBuilder;
import net.minecraftforge.common.world.StructureSettingsBuilder.StructureSpawnOverrideBuilder;

public class SpawnData
{
	public static void onBiome(ModifiableBiomeInfo.BiomeInfo.Builder biomeBuilder)
	{
		// copy spawn for biomes
		MobSpawnSettingsBuilder builder = biomeBuilder.getMobSpawnSettings();
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

	public static void onStructure(ModifiableStructureInfo.StructureInfo.Builder structureBuilder)
	{
		// copy spawn for structures
		StructureSettingsBuilder builder = structureBuilder.getStructureSettings();
		List<EntityType<?>> mobs = new ArrayList<>();
		List<SpawnerData> list = new ArrayList<>();
		ModEntities.getFeatureSpawnData(mobs, list);
		for (int i = 0; i < mobs.size(); ++i)
		{
			EntityType<?> mob = mobs.get(i);
			@Nullable
			StructureSpawnOverrideBuilder override = builder.getSpawnOverrides(mob.getCategory());
			if (override == null)
				continue;
			for (SpawnerData s : override.getSpawns())
			{
				if (s.type == mob)
				{
					SpawnerData spawner = list.get(i);
					MobCategory catType = spawner.type.getCategory();
					if (catType != mob.getCategory())
					{
						BoundingBoxType boxType = override.getBoundingBox();
						override = builder.getOrAddSpawnOverrides(catType);
						if (override.getSpawns().isEmpty())
							override.setBoundingBox(boxType);
					}
					override.addSpawn(spawner);
					break;
				}
			}
		}
	}
}
