package com.lupicus.nasty.item;

import com.lupicus.nasty.entity.MagicArrowEntity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MagicArrowItem extends ArrowItem
{
	public MagicArrowItem(Properties builder) {
		super(builder);
	}

	@Override
	public AbstractArrow createArrow(Level worldIn, ItemStack stack, LivingEntity shooter) {
		MagicArrowEntity arrowentity = new MagicArrowEntity(worldIn, shooter);
		arrowentity.setEffectsFromItem(stack);
		return arrowentity;
	}
}
