package com.lupicus.nasty.item;

import com.lupicus.nasty.entity.ExplosiveArrowEntity;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ExplosiveArrowItem extends ArrowItem
{
	public ExplosiveArrowItem(Properties builder) {
		super(builder);
	}

	@Override
	public AbstractArrow createArrow(Level worldIn, ItemStack stack, LivingEntity shooter) {
		ExplosiveArrowEntity arrowentity = new ExplosiveArrowEntity(worldIn, shooter, stack.copyWithCount(1));
		return arrowentity;
	}

	@Override
	public Projectile asProjectile(Level worldIn, Position position, ItemStack stack, Direction dir) {
		AbstractArrow arrowentity = new ExplosiveArrowEntity(worldIn, position.x(), position.y(), position.z(), stack.copyWithCount(1));
		arrowentity.pickup = AbstractArrow.Pickup.ALLOWED;
		return arrowentity;
	}
}
