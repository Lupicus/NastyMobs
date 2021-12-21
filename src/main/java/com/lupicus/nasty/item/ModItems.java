package com.lupicus.nasty.item;

import com.lupicus.nasty.entity.ExplosiveArrowEntity;
import com.lupicus.nasty.entity.MagicArrowEntity;
import com.lupicus.nasty.entity.ModEntities;

import net.minecraft.client.color.item.ItemColors;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.IForgeRegistry;

public class ModItems
{
	public static final Item NASTY_SKELETON_SPAWN_EGG = new ForgeSpawnEggItem(() -> ModEntities.NASTY_SKELETON, 0xD7D3D3, 0x006000, new Properties().tab(CreativeModeTab.TAB_MISC)).setRegistryName("skeleton_spawn_egg");
	public static final Item NASTY_WOLF_SPAWN_EGG = new ForgeSpawnEggItem(() -> ModEntities.NASTY_WOLF, 0xC1C1C1, 0x008000, new Properties().tab(CreativeModeTab.TAB_MISC)).setRegistryName("wolf_spawn_egg");
	public static final Item EXPLOSIVE_ARROW = new ExplosiveArrowItem(new Properties().tab(CreativeModeTab.TAB_COMBAT).rarity(Rarity.RARE)).setRegistryName("explosive_arrow");
	public static final Item MAGIC_ARROW = new MagicArrowItem(new Properties().tab(CreativeModeTab.TAB_COMBAT).rarity(Rarity.RARE)).setRegistryName("magic_arrow");

	public static void register(IForgeRegistry<Item> forgeRegistry)
	{
		forgeRegistry.registerAll(NASTY_SKELETON_SPAWN_EGG, NASTY_WOLF_SPAWN_EGG);
		forgeRegistry.registerAll(EXPLOSIVE_ARROW, MAGIC_ARROW);
	}

	@OnlyIn(Dist.CLIENT)
	public static void register(ItemColors itemColors)
	{
	}

	public static void setup()
	{
		DispenserBlock.registerBehavior(EXPLOSIVE_ARROW, new AbstractProjectileDispenseBehavior() {
			@Override
			protected Projectile getProjectile(Level worldIn, Position position, ItemStack stack) {
				Arrow arrowentity = new ExplosiveArrowEntity(worldIn, position.x(), position.y(), position.z());
				arrowentity.pickup = AbstractArrow.Pickup.ALLOWED;
				return arrowentity;
			}
		});
		DispenserBlock.registerBehavior(MAGIC_ARROW, new AbstractProjectileDispenseBehavior() {
			@Override
			protected Projectile getProjectile(Level worldIn, Position position, ItemStack stack) {
				Arrow arrowentity = new MagicArrowEntity(worldIn, position.x(), position.y(), position.z());
				arrowentity.pickup = AbstractArrow.Pickup.ALLOWED;
				return arrowentity;
			}
		});
	}
}
