package com.lupicus.nasty.item;

import com.lupicus.nasty.entity.ExplosiveArrowEntity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ExplosiveArrowItem extends ArrowItem
{
	public ExplosiveArrowItem(Properties builder) {
		super(builder);
	}

	@Override
	public AbstractArrowEntity createArrow(World worldIn, ItemStack stack, LivingEntity shooter) {
		ExplosiveArrowEntity arrowentity = new ExplosiveArrowEntity(worldIn, shooter);
		arrowentity.setPotionEffect(stack);
		return arrowentity;
	}
}
