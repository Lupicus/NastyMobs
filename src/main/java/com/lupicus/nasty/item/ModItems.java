package com.lupicus.nasty.item;

import com.lupicus.nasty.entity.ModEntities;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class ModItems
{
	public static final Item NASTY_SKELETON_SPAWN_EGG = new ForgeSpawnEggItem(() -> ModEntities.NASTY_SKELETON, 0xFFD7D3D3, 0xFF006000, new Properties());
	public static final Item NASTY_WOLF_SPAWN_EGG = new ForgeSpawnEggItem(() -> ModEntities.NASTY_WOLF, 0xFFC1C1C1, 0xFF008000, new Properties());
	public static final Item EXPLOSIVE_ARROW = new ExplosiveArrowItem(new Properties().rarity(Rarity.RARE));
	public static final Item MAGIC_ARROW = new MagicArrowItem(new Properties().rarity(Rarity.RARE));

	public static void register(IForgeRegistry<Item> forgeRegistry)
	{
		forgeRegistry.register("skeleton_spawn_egg", NASTY_SKELETON_SPAWN_EGG);
		forgeRegistry.register("wolf_spawn_egg", NASTY_WOLF_SPAWN_EGG);
		forgeRegistry.register("explosive_arrow", EXPLOSIVE_ARROW);
		forgeRegistry.register("magic_arrow", MAGIC_ARROW);
	}

	public static void setup()
	{
		DispenserBlock.registerProjectileBehavior(EXPLOSIVE_ARROW);
		DispenserBlock.registerProjectileBehavior(MAGIC_ARROW);
	}

	public static void setupTabs(BuildCreativeModeTabContentsEvent event)
	{
		if (event.getTabKey() == CreativeModeTabs.COMBAT)
		{
			event.accept(EXPLOSIVE_ARROW);
			event.accept(MAGIC_ARROW);
		}
		else if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS)
		{
			event.accept(NASTY_SKELETON_SPAWN_EGG);
			event.accept(NASTY_WOLF_SPAWN_EGG);
		}
	}
}
