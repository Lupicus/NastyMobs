package com.lupicus.nasty.pathfinding;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class JumpNodeProcessor extends WalkNodeEvaluator
{
	private double jumpHeight;
	private int adjusted_j;

	@Override
	public void prepare(PathNavigationRegion region, Mob mobIn) {
		super.prepare(region, mobIn);
		jumpHeight = Math.max(1.125, (double) this.mob.maxUpStep());
		adjusted_j = Mth.floor(Math.max(1.0F, this.mob.maxUpStep()));
		MobEffectInstance effect = mobIn.getEffect(MobEffects.JUMP);
		if (effect != null)
		{
			int amp = effect.getAmplifier();
			if (amp > 0)
			{
				int j = (amp > 2) ? 2 : 1;
				jumpHeight += j;
				adjusted_j += j;
			}
		}
	}

	/**
	 * Modified version from WalkNodeEvaluator
	 */
	@Override
	public int getNeighbors(Node[] retNodes, Node nodeIn) {
		int i = 0;
		int j = 0;
		PathType pathnodetype = this.getCachedPathType(nodeIn.x, nodeIn.y + 1, nodeIn.z);
		PathType pathnodetype1 = this.getCachedPathType(nodeIn.x, nodeIn.y, nodeIn.z);
		if (this.mob.getPathfindingMalus(pathnodetype) >= 0.0F && pathnodetype1 != PathType.STICKY_HONEY) {
			j = adjusted_j;
		}

		double d0 = getFloorLevel(new BlockPos(nodeIn.x, nodeIn.y, nodeIn.z));

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			Node node = this.findAcceptedNode(nodeIn.x + direction.getStepX(), nodeIn.y,
					nodeIn.z + direction.getStepZ(), j, d0, direction, pathnodetype1);
			this.reusableNeighbors[direction.get2DDataValue()] = node;
			if (this.isNeighborValid(node, nodeIn)) {
				retNodes[i++] = node;
			}
		}

		for (Direction direction1 : Direction.Plane.HORIZONTAL) {
			Direction direction2 = direction1.getClockWise();
			if (this.isDiagonalValid(nodeIn, this.reusableNeighbors[direction1.get2DDataValue()],
					this.reusableNeighbors[direction2.get2DDataValue()])) {
				Node node1 = this.findAcceptedNode(nodeIn.x + direction1.getStepX() + direction2.getStepX(), nodeIn.y,
						nodeIn.z + direction1.getStepZ() + direction2.getStepZ(), j, d0, direction1, pathnodetype1);
				if (this.isDiagonalValid(node1)) {
					retNodes[i++] = node1;
				}
			}
		}

		return i;
	}

	/**
	 * Returns a point that the mob can safely move to
	 */
	@Override
	@Nullable
	protected Node findAcceptedNode(int x, int y, int z, int stepHeight, double groundYIn, Direction facing, PathType nodeTypeIn) {
		Node node = null;
		BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();
		double d0 = getFloorLevel(blockpos$mutable.set(x, y, z));
		if (d0 - groundYIn > jumpHeight) {
			return null;
		} else {
			PathType pathtype = this.getCachedPathType(x, y, z);
			float f = this.mob.getPathfindingMalus(pathtype);
			if (f >= 0.0F) {
				node = this.getNodeAndUpdateCostToMax(x, y, z, pathtype, f);
			}

			if (doesBlockHavePartialCollision(nodeTypeIn) && node != null && node.costMalus >= 0.0F && !this.canReachWithoutCollision(node)) {
				node = null;
			}

			if (pathtype != PathType.WALKABLE && (!this.isAmphibious() || pathtype != PathType.WATER)) {
				if ((node == null || node.costMalus < 0.0F) && stepHeight > 0
						&& (pathtype != PathType.FENCE || this.canWalkOverFences())
						&& pathtype != PathType.UNPASSABLE_RAIL && pathtype != PathType.TRAPDOOR
						&& pathtype != PathType.POWDER_SNOW) {
					node = this.tryJumpOn(x, y, z, stepHeight, groundYIn, facing, nodeTypeIn, blockpos$mutable);
				} else if (!this.isAmphibious() && pathtype == PathType.WATER && !this.canFloat()) {
					node = this.tryFindFirstNonWaterBelow(x, y, z, node);
				} else if (pathtype == PathType.OPEN) {
					node = this.tryFindFirstGroundNodeBelow(x, y, z);
				} else if (doesBlockHavePartialCollision(pathtype) && node == null) {
					node = this.getClosedNode(x, y, z, pathtype);
				}
			}
			return node;
		}
	}
}
