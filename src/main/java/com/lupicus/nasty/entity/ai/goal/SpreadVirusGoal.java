package com.lupicus.nasty.entity.ai.goal;

import com.lupicus.nasty.entity.IHasVirus;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;

public class SpreadVirusGoal extends Goal
{
	protected final MobEntity entity;
	protected Entity closestEntity;
	protected final float maxDistance;
	protected final float chance;
	protected final Class<? extends LivingEntity> watchedClass;
	protected final EntityPredicate predicate;

	/**
	 * Spreading Virus Goal
	 * @param <T> MobEntity with IHasVirus
	 * @param entity
	 * @param watchTargetClass
	 * @param maxDistance
	 * @param chanceIn
	 */
	public <T extends MobEntity & IHasVirus> SpreadVirusGoal(T entity, Class<? extends MobEntity> watchTargetClass, float maxDistance, float chanceIn)
	{
		this.entity = entity;
		this.watchedClass = watchTargetClass;
		this.maxDistance = maxDistance;
		this.chance = chanceIn;
        this.predicate = (new EntityPredicate()).setDistance((double)maxDistance).allowFriendlyFire().allowInvulnerable().setSkipAttackChecks();
        if (watchTargetClass.isInstance(entity))
        {
        	this.predicate.setCustomPredicate(e -> !(e instanceof IHasVirus));
        }
	}

	@Override
	public boolean shouldExecute() {
		if (this.entity.getRNG().nextFloat() >= this.chance) {
			return false;
		} else {
			this.closestEntity = this.entity.world.func_225318_b(this.watchedClass, this.predicate, this.entity,
					this.entity.getPosX(), this.entity.getPosYEye(), this.entity.getPosZ(),
					this.entity.getBoundingBox().grow((double) this.maxDistance, 3.0D, (double) this.maxDistance));
			return this.closestEntity != null;
		}
	}

	@Override
	public boolean shouldContinueExecuting() {
		return false;
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void startExecuting() {
		((IHasVirus) entity).onInfect(closestEntity);
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by
	 * another one
	 */
	@Override
	public void resetTask() {
		this.closestEntity = null;
	}
}
