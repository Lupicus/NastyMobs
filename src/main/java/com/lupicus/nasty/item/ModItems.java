package com.lupicus.nasty.item;

import com.lupicus.nasty.entity.ModEntities;

import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Properties;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistry;

public class ModItems
{
	public static final Item NASTY_SKELETON_SPAWN_EGG = new SpawnEggItem(ModEntities.NASTY_SKELETON, 0x006000, 0xFFFFFF, new Properties().group(ItemGroup.MISC)).setRegistryName("skeleton_spawn_egg");
	public static final Item EXPLOSIVE_ARROW = new ExplosiveArrowItem(new Properties().group(ItemGroup.COMBAT)).setRegistryName("explosive_arrow");

	public static void register(IForgeRegistry<Item> forgeRegistry)
	{
		forgeRegistry.register(NASTY_SKELETON_SPAWN_EGG);
		forgeRegistry.register(EXPLOSIVE_ARROW);
	}

	@OnlyIn(Dist.CLIENT)
	public static void register(ItemColors itemColors)
	{
	}
}
