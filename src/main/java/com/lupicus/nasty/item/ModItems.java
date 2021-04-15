package com.lupicus.nasty.item;

import com.lupicus.nasty.entity.ExplosiveArrowEntity;
import com.lupicus.nasty.entity.MagicArrowEntity;
import com.lupicus.nasty.entity.ModEntities;

import net.minecraft.block.DispenserBlock;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.ProjectileDispenseBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Properties;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.item.SpawnEggItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistry;

public class ModItems
{
	public static final Item NASTY_SKELETON_SPAWN_EGG = new SpawnEggItem(ModEntities.NASTY_SKELETON, 0xD7D3D3, 0x006000, new Properties().group(ItemGroup.MISC)).setRegistryName("skeleton_spawn_egg");
	public static final Item NASTY_WOLF_SPAWN_EGG = new SpawnEggItem(ModEntities.NASTY_WOLF, 0xC1C1C1, 0x008000, new Properties().group(ItemGroup.MISC)).setRegistryName("wolf_spawn_egg");
	public static final Item EXPLOSIVE_ARROW = new ExplosiveArrowItem(new Properties().group(ItemGroup.COMBAT).rarity(Rarity.RARE)).setRegistryName("explosive_arrow");
	public static final Item MAGIC_ARROW = new MagicArrowItem(new Properties().group(ItemGroup.COMBAT).rarity(Rarity.RARE)).setRegistryName("magic_arrow");

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
		DefaultDispenseItemBehavior eggdispenseitembehavior = new DefaultDispenseItemBehavior() {
			@Override
			public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
				Direction direction = source.getBlockState().get(DispenserBlock.FACING);
				EntityType<?> entitytype = ((SpawnEggItem) stack.getItem()).getType(stack.getTag());
				entitytype.spawn(source.getWorld(), stack, (PlayerEntity) null, source.getBlockPos().offset(direction),
						SpawnReason.DISPENSER, direction != Direction.UP, false);
				stack.shrink(1);
				return stack;
			}
		};
		DispenserBlock.registerDispenseBehavior(NASTY_SKELETON_SPAWN_EGG, eggdispenseitembehavior);
		DispenserBlock.registerDispenseBehavior(NASTY_WOLF_SPAWN_EGG, eggdispenseitembehavior);
		DispenserBlock.registerDispenseBehavior(EXPLOSIVE_ARROW, new ProjectileDispenseBehavior() {
			@Override
			protected ProjectileEntity getProjectileEntity(World worldIn, IPosition position, ItemStack stack) {
				ArrowEntity arrowentity = new ExplosiveArrowEntity(worldIn, position.getX(), position.getY(), position.getZ());
				arrowentity.pickupStatus = AbstractArrowEntity.PickupStatus.ALLOWED;
				return arrowentity;
			}
		});
		DispenserBlock.registerDispenseBehavior(MAGIC_ARROW, new ProjectileDispenseBehavior() {
			@Override
			protected ProjectileEntity getProjectileEntity(World worldIn, IPosition position, ItemStack stack) {
				ArrowEntity arrowentity = new MagicArrowEntity(worldIn, position.getX(), position.getY(), position.getZ());
				arrowentity.pickupStatus = AbstractArrowEntity.PickupStatus.ALLOWED;
				return arrowentity;
			}
		});
	}
}
