package com.lupicus.nasty.entity.ai.goal;

import com.lupicus.nasty.entity.IHasVirus;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class SpreadVirusGoal extends Goal
{
	protected final Mob entity;
	protected Entity closestEntity;
	protected final float maxDistance;
	protected final float chance;
	protected final Class<? extends LivingEntity> watchedClass;
	protected final TargetingConditions predicate;

	/**
	 * Spreading Virus Goal
	 * @param <T> Mob with IHasVirus
	 * @param entity
	 * @param watchTargetClass
	 * @param maxDistance
	 * @param chanceIn
	 */
	public <T extends Mob & IHasVirus> SpreadVirusGoal(T entity, Class<? extends Mob> watchTargetClass, float maxDistance, float chanceIn)
	{
		this.entity = entity;
		this.watchedClass = watchTargetClass;
		this.maxDistance = maxDistance;
		this.chance = chanceIn;
        this.predicate = TargetingConditions.forNonCombat().range((double)maxDistance);
        if (watchTargetClass.isInstance(entity))
        {
        	this.predicate.selector(e -> !(e instanceof IHasVirus));
        }
	}

	@Override
	public boolean canUse() {
		if (this.entity.getRandom().nextFloat() >= this.chance) {
			return false;
		} else {
			this.closestEntity = this.entity.level().getNearestEntity(this.watchedClass, this.predicate, this.entity,
					this.entity.getX(), this.entity.getEyeY(), this.entity.getZ(),
					this.entity.getBoundingBox().inflate((double) this.maxDistance, 3.0D, (double) this.maxDistance));
			return this.closestEntity != null;
		}
	}

	@Override
	public boolean canContinueToUse() {
		return false;
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void start() {
		((IHasVirus) entity).onInfect(closestEntity);
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by
	 * another one
	 */
	@Override
	public void stop() {
		this.closestEntity = null;
	}
}
