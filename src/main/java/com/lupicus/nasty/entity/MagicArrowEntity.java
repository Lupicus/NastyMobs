package com.lupicus.nasty.entity;

import com.lupicus.nasty.config.MyConfig;
import com.lupicus.nasty.item.ModItems;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class MagicArrowEntity extends AbstractArrow
{
	private static final ItemStack DEFAULT_ARROW_STACK = new ItemStack(ModItems.MAGIC_ARROW);
	private LivingEntity hitEntity = null;

	public MagicArrowEntity(EntityType<? extends MagicArrowEntity> type, Level world) {
		super(type, world, DEFAULT_ARROW_STACK);
	}

	public MagicArrowEntity(Level worldIn, double x, double y, double z, ItemStack stack) {
		super(ModEntities.MAGIC_ARROW, x, y, z, worldIn, stack);
	}

	public MagicArrowEntity(Level worldIn, LivingEntity shooter, ItemStack stack) {
		super(ModEntities.MAGIC_ARROW, shooter, worldIn, stack);
	}

	@Override
	protected void doPostHurtEffects(LivingEntity living)
	{
		super.doPostHurtEffects(living);
		hitEntity = living;
	}

	@Override
	protected void onHitEntity(EntityHitResult raytraceResultIn)
	{
		super.onHitEntity(raytraceResultIn);
		if (hitEntity != null) {
			if (!hitEntity.isInvertedHealAndHarm()) {
				float damage = hitEntity.lastHurt + (float) MyConfig.magicDamage;
				hitEntity.hurt(damageSources().magic(), damage);
			}
		}
	}
}
