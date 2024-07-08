package com.lupicus.nasty.item;

import javax.annotation.Nullable;

import com.lupicus.nasty.entity.MagicArrowEntity;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MagicArrowItem extends ArrowItem
{
	public MagicArrowItem(Properties builder) {
		super(builder);
	}

	@Override
	public AbstractArrow createArrow(Level worldIn, ItemStack stack, LivingEntity shooter, @Nullable ItemStack weapon) {
		MagicArrowEntity arrowentity = new MagicArrowEntity(worldIn, shooter, stack.copyWithCount(1), weapon);
		return arrowentity;
	}

	@Override
	public Projectile asProjectile(Level worldIn, Position position, ItemStack stack, Direction dir) {
		AbstractArrow arrowentity = new MagicArrowEntity(worldIn, position.x(), position.y(), position.z(), stack.copyWithCount(1), null);
		arrowentity.pickup = AbstractArrow.Pickup.ALLOWED;
		return arrowentity;
	}
}
