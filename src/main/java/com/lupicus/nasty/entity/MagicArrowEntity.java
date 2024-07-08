package com.lupicus.nasty.entity;

import javax.annotation.Nullable;

import com.lupicus.nasty.config.MyConfig;
import com.lupicus.nasty.item.ModItems;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MagicArrowEntity extends AbstractArrow
{
	public MagicArrowEntity(EntityType<? extends MagicArrowEntity> type, Level world) {
		super(type, world);
	}

	public MagicArrowEntity(Level worldIn, double x, double y, double z, ItemStack stack, @Nullable ItemStack weapon) {
		super(ModEntities.MAGIC_ARROW, x, y, z, worldIn, stack, weapon);
	}

	public MagicArrowEntity(Level worldIn, LivingEntity shooter, ItemStack stack, @Nullable ItemStack weapon) {
		super(ModEntities.MAGIC_ARROW, shooter, worldIn, stack, weapon);
	}

	@Override
	protected void doPostHurtEffects(LivingEntity living)
	{
		super.doPostHurtEffects(living);
		if (!living.isInvertedHealAndHarm()) {
			float damage = living.lastHurt + (float) MyConfig.magicDamage;
			living.hurt(damageSources().magic(), damage);
		}
	}

	@Override
	protected ItemStack getDefaultPickupItem() {
		return new ItemStack(ModItems.MAGIC_ARROW);
	}
}
