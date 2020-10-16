package com.lupicus.nasty.entity;

import com.lupicus.nasty.config.MyConfig;
import com.lupicus.nasty.item.ModItems;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;

public class MagicArrowEntity extends ArrowEntity
{
	private LivingEntity hitEntity = null;

	public MagicArrowEntity(EntityType<? extends ArrowEntity> type, World world) {
		super(type, world);
	}

	public MagicArrowEntity(World worldIn, LivingEntity shooter) {
		super(worldIn, shooter);
	}

	@Override
	protected void arrowHit(LivingEntity living)
	{
		super.arrowHit(living);
		hitEntity = living;
	}

	@Override
	protected void onEntityHit(EntityRayTraceResult raytraceResultIn)
	{
		super.onEntityHit(raytraceResultIn);
		if (hitEntity != null) {
			if (!hitEntity.isEntityUndead()) {
				float damage = hitEntity.lastDamage + (float) MyConfig.magicDamage;
				hitEntity.attackEntityFrom(DamageSource.MAGIC, damage);
			}
		}
	}

	@Override
	protected ItemStack getArrowStack() {
		return new ItemStack(ModItems.MAGIC_ARROW);
	}
}
