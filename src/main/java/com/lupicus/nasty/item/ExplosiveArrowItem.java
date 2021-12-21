package com.lupicus.nasty.item;

import com.lupicus.nasty.entity.ExplosiveArrowEntity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
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
		ExplosiveArrowEntity arrowentity = new ExplosiveArrowEntity(worldIn, shooter);
		arrowentity.setEffectsFromItem(stack);
		return arrowentity;
	}
}
