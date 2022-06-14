package com.lupicus.nasty.util;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo.BiomeInfo.Builder;
import net.minecraftforge.registries.IForgeRegistry;

public class ModBiomeModifier implements BiomeModifier
{
    public static final ModBiomeModifier INSTANCE = new ModBiomeModifier();

    @Override
    public void modify(Holder<Biome> biome, Phase phase, Builder builder)
    {
    	if (phase == Phase.ADD)
    	{
    		SpawnData.onBiome(builder);
    	}
    }

    @Override
    public Codec<? extends BiomeModifier> codec()
    {
    	return Codec.unit(ModBiomeModifier.INSTANCE);
    }

    public static void register(@Nullable IForgeRegistry<Object> forgeRegistry)
    {
    	forgeRegistry.register("special", INSTANCE.codec());
    }
}
