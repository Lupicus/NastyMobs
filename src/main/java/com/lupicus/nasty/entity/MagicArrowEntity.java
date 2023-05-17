package com.lupicus.nasty.entity;

import com.lupicus.nasty.config.MyConfig;
import com.lupicus.nasty.item.ModItems;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class MagicArrowEntity extends Arrow
{
	private LivingEntity hitEntity = null;

	public MagicArrowEntity(EntityType<? extends Arrow> type, Level world) {
		super(type, world);
	}

	public MagicArrowEntity(Level worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
	}

	public MagicArrowEntity(Level worldIn, LivingEntity shooter) {
		super(worldIn, shooter);
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

	@Override
	protected ItemStack getPickupItem() {
		return new ItemStack(ModItems.MAGIC_ARROW);
	}
}
