package com.lupicus.nasty.util;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.common.world.ModifiableStructureInfo.StructureInfo.Builder;
import net.minecraftforge.common.world.StructureModifier;
import net.minecraftforge.registries.IForgeRegistry;

public class ModStructureModifier implements StructureModifier
{
    public static final ModStructureModifier INSTANCE = new ModStructureModifier();

    @Override
    public void modify(Holder<Structure> struct, Phase phase, Builder builder)
    {
    	if (phase == Phase.ADD)
    	{
    		SpawnData.onStructure(builder);
    	}
    }

    @Override
    public MapCodec<? extends StructureModifier> codec()
    {
    	return MapCodec.unit(ModStructureModifier.INSTANCE);
    }

    public static void register(@Nullable IForgeRegistry<Object> forgeRegistry)
    {
    	forgeRegistry.register("special", INSTANCE.codec());
    }
}
