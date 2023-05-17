package com.lupicus.nasty.pathfinding;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;

public class JumpNodeProcessor extends WalkNodeEvaluator
{
	private double jumpHeight;
	private int adjusted_j;

	@Override
	public void prepare(PathNavigationRegion region, Mob mobIn) {
		super.prepare(region, mobIn);
		jumpHeight = Math.max(1.125, (double) this.mob.maxUpStep());
		adjusted_j = Mth.floor(Math.max(1.0F, this.mob.getStepHeight()));
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
		BlockPathTypes pathnodetype = this.getCachedBlockType(this.mob, nodeIn.x, nodeIn.y + 1, nodeIn.z);
		BlockPathTypes pathnodetype1 = this.getCachedBlockType(this.mob, nodeIn.x, nodeIn.y, nodeIn.z);
		if (this.mob.getPathfindingMalus(pathnodetype) >= 0.0F && pathnodetype1 != BlockPathTypes.STICKY_HONEY) {
			j = adjusted_j;
		}

		double d0 = getFloorLevel(new BlockPos(nodeIn.x, nodeIn.y, nodeIn.z));
		Node node = this.findAcceptedNode(nodeIn.x, nodeIn.y, nodeIn.z + 1, j, d0,
				Direction.SOUTH, pathnodetype1);
		if (this.isNeighborValid(node, nodeIn)) {
			retNodes[i++] = node;
		}

		Node node1 = this.findAcceptedNode(nodeIn.x - 1, nodeIn.y, nodeIn.z, j, d0,
				Direction.WEST, pathnodetype1);
		if (this.isNeighborValid(node1, nodeIn)) {
			retNodes[i++] = node1;
		}

		Node node2 = this.findAcceptedNode(nodeIn.x + 1, nodeIn.y, nodeIn.z, j, d0,
				Direction.EAST, pathnodetype1);
		if (this.isNeighborValid(node2, nodeIn)) {
			retNodes[i++] = node2;
		}

		Node node3 = this.findAcceptedNode(nodeIn.x, nodeIn.y, nodeIn.z - 1, j, d0,
				Direction.NORTH, pathnodetype1);
		if (this.isNeighborValid(node3, nodeIn)) {
			retNodes[i++] = node3;
		}

		Node node4 = this.findAcceptedNode(nodeIn.x - 1, nodeIn.y, nodeIn.z - 1, j, d0,
				Direction.NORTH, pathnodetype1);
		if (this.isDiagonalValid(nodeIn, node1, node3, node4)) {
			retNodes[i++] = node4;
		}

		Node node5 = this.findAcceptedNode(nodeIn.x + 1, nodeIn.y, nodeIn.z - 1, j, d0,
				Direction.NORTH, pathnodetype1);
		if (this.isDiagonalValid(nodeIn, node2, node3, node5)) {
			retNodes[i++] = node5;
		}

		Node node6 = this.findAcceptedNode(nodeIn.x - 1, nodeIn.y, nodeIn.z + 1, j, d0,
				Direction.SOUTH, pathnodetype1);
		if (this.isDiagonalValid(nodeIn, node1, node, node6)) {
			retNodes[i++] = node6;
		}

		Node node7 = this.findAcceptedNode(nodeIn.x + 1, nodeIn.y, nodeIn.z + 1, j, d0,
				Direction.SOUTH, pathnodetype1);
		if (this.isDiagonalValid(nodeIn, node2, node, node7)) {
			retNodes[i++] = node7;
		}

		return i;
	}

	/**
	 * Returns a point that the mob can safely move to
	 */
	@Override
	@Nullable
	protected Node findAcceptedNode(int x, int y, int z, int stepHeight, double groundYIn, Direction facing,
			BlockPathTypes nodeTypeIn) {
		Node node = null;
		BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();
		double d0 = getFloorLevel(blockpos$mutable.set(x, y, z));
		if (d0 - groundYIn > jumpHeight) {
			return null;
		} else {
			BlockPathTypes pathnodetype = this.getCachedBlockType(this.mob, x, y, z);
			float f = this.mob.getPathfindingMalus(pathnodetype);
			double d1 = (double) this.mob.getBbWidth() / 2.0D;
			if (f >= 0.0F) {
				node = this.getNodeAndUpdateCostToMax(x, y, z, pathnodetype, f);
			}

			if (doesBlockHavePartialCollision(nodeTypeIn) && node != null && node.costMalus >= 0.0F
					&& !this.canReachWithoutCollision(node)) {
				node = null;
			}

			if (pathnodetype != BlockPathTypes.WALKABLE
					&& (!this.isAmphibious() || pathnodetype != BlockPathTypes.WATER)) {
				if ((node == null || node.costMalus < 0.0F) && stepHeight > 0
						&& (pathnodetype != BlockPathTypes.FENCE || this.canWalkOverFences()) && pathnodetype != BlockPathTypes.UNPASSABLE_RAIL
						&& pathnodetype != BlockPathTypes.TRAPDOOR && pathnodetype != BlockPathTypes.POWDER_SNOW) {
					node = this.findAcceptedNode(x, y + 1, z, stepHeight - 1, groundYIn, facing, nodeTypeIn);
					if (node != null
							&& (node.type == BlockPathTypes.OPEN || node.type == BlockPathTypes.WALKABLE)
							&& this.mob.getBbWidth() < 1.0F) {
						double d2 = (double) (x - facing.getStepX()) + 0.5D;
						double d3 = (double) (z - facing.getStepZ()) + 0.5D;
						AABB axisalignedbb = new AABB(d2 - d1,
								getFloorLevel(blockpos$mutable.set(d2, (double) (y + 1), d3)) + 0.001D, d3 - d1,
								d2 + d1, (double) this.mob.getBbHeight() + getFloorLevel(
										blockpos$mutable.set(node.x, node.y, node.z)) - 0.002D,
								d3 + d1);
						if (this.hasCollisions(axisalignedbb)) {
							node = null;
						}
					}
				}

				if (!this.isAmphibious() && pathnodetype == BlockPathTypes.WATER && !this.canFloat()) {
					if (this.getCachedBlockType(this.mob, x, y - 1, z) != BlockPathTypes.WATER) {
						return node;
					}

					while (y > this.mob.level.getMinBuildHeight()) {
						--y;
						pathnodetype = this.getCachedBlockType(this.mob, x, y, z);
						if (pathnodetype != BlockPathTypes.WATER) {
							return node;
						}

						node = this.getNodeAndUpdateCostToMax(x, y, z, pathnodetype, this.mob.getPathfindingMalus(pathnodetype));
					}
				}

				if (pathnodetype == BlockPathTypes.OPEN) {
					int j = 0;
					int i = y;

					while (pathnodetype == BlockPathTypes.OPEN) {
						--y;
						if (y < this.mob.level.getMinBuildHeight()) {
							return this.getBlockedNode(x, i, z);
						}

						if (j++ >= this.mob.getMaxFallDistance()) {
							return this.getBlockedNode(x, y, z);
						}

						pathnodetype = this.getCachedBlockType(this.mob, x, y, z);
						f = this.mob.getPathfindingMalus(pathnodetype);
						if (pathnodetype != BlockPathTypes.OPEN && f >= 0.0F) {
							node = this.getNodeAndUpdateCostToMax(x, y, z, pathnodetype, f);
							break;
						}

						if (f < 0.0F) {
							return this.getBlockedNode(x, y, z);
						}
					}
				}

				if (doesBlockHavePartialCollision(pathnodetype) && node == null) {
					node = this.getNode(x, y, z);
					node.closed = true;
					node.type = pathnodetype;
					node.costMalus = pathnodetype.getMalus();
				}
			}

			return node;
		}
	}
}
