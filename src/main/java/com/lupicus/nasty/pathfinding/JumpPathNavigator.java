package com.lupicus.nasty.pathfinding;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;

public class JumpPathNavigator extends GroundPathNavigation
{
	public JumpPathNavigator(Mob entitylivingIn, Level worldIn)
	{
		super(entitylivingIn, worldIn);
	}

	@Override
	protected PathFinder createPathFinder(int num)
	{
		this.nodeEvaluator = new JumpNodeProcessor();
		this.nodeEvaluator.setCanPassDoors(true);
		return new PathFinder(this.nodeEvaluator, num);
	}
}
