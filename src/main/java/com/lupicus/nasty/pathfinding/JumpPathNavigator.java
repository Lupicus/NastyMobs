package com.lupicus.nasty.pathfinding;

import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.world.World;

public class JumpPathNavigator extends GroundPathNavigator
{
	public JumpPathNavigator(MobEntity entitylivingIn, World worldIn)
	{
		super(entitylivingIn, worldIn);
	}

	@Override
	protected PathFinder getPathFinder(int num)
	{
		this.nodeProcessor = new JumpNodeProcessor();
		this.nodeProcessor.setCanEnterDoors(true);
		return new PathFinder(this.nodeProcessor, num);
	}
}
