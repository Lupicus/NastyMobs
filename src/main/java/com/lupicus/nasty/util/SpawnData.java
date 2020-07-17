package com.lupicus.nasty.util;

import java.util.List;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.registries.ForgeRegistries;

public class SpawnData
{
    public static void copyBiomeSpawn(EntityType<?> oldMob, EntityType<?> newMob, int weight, int min, int max)
    {
    	EntityClassification type = newMob.getClassification();
    	for (Biome b : ForgeRegistries.BIOMES)
    	{
    		List<SpawnListEntry> list = b.getSpawns(type);
    		if (list == null)
    			continue;
    		if (list.stream().anyMatch(e -> oldMob.equals(e.entityType)))
    		{
    			list.add(new SpawnListEntry(newMob, weight, min, max));
    		}
    	}
    }

    public static void copyFeatureSpawn(EntityType<?> oldMob, EntityType<?> newMob, int weight, int min, int max)
    {
    	for (Feature<?> f : ForgeRegistries.FEATURES)
    	{
    		List<SpawnListEntry> list = f.getSpawnList();
    		if (list == null)
    			continue;
    		if (list.stream().anyMatch(e -> oldMob.equals(e.entityType)))
    		{
    			list.add(new SpawnListEntry(newMob, weight, min, max));
    		}
    	}
    }

	public static void removeBiomeSpawn(EntityType<?> mob)
	{
    	EntityClassification type = mob.getClassification();
    	for (Biome b : ForgeRegistries.BIOMES)
    	{
    		List<SpawnListEntry> list = b.getSpawns(type);
    		if (list == null)
    			continue;
    		for (int i = 0; i < list.size(); ++i)
    		{
    			if (mob.equals(list.get(i).entityType))
    			{
    				list.remove(i);
    				break;
    			}
    		}
    	}
	}

	public static void removeFeatureSpawn(EntityType<?> mob)
	{
    	for (Feature<?> f : ForgeRegistries.FEATURES)
    	{
    		List<SpawnListEntry> list = f.getSpawnList();
    		if (list == null)
    			continue;
    		for (int i = 0; i < list.size(); ++i)
    		{
    			if (mob.equals(list.get(i).entityType))
    			{
    				list.remove(i);
    				break;
    			}
    		}
    	}
	}
}
