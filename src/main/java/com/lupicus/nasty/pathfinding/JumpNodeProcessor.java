package com.lupicus.nasty.pathfinding;

import javax.annotation.Nullable;

import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Region;

public class JumpNodeProcessor extends WalkNodeProcessor
{
	private double jumpHeight;
	private int adjusted_j;

	@Override
	public void func_225578_a_(Region p_225578_1_, MobEntity p_225578_2_) {
		super.func_225578_a_(p_225578_1_, p_225578_2_);
		jumpHeight = 1.125;
		adjusted_j = MathHelper.floor(Math.max(1.0F, this.entity.stepHeight));
		EffectInstance effect = p_225578_2_.getActivePotionEffect(Effects.JUMP_BOOST);
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
	 * Modified version from WalkNodeProcessor
	 */
	@Override
	public int func_222859_a(PathPoint[] p_222859_1_, PathPoint p_222859_2_) {
		int i = 0;
		int j = 0;
		PathNodeType pathnodetype = this.func_237230_a_(this.entity, p_222859_2_.x, p_222859_2_.y + 1, p_222859_2_.z);
		PathNodeType pathnodetype1 = this.func_237230_a_(this.entity, p_222859_2_.x, p_222859_2_.y, p_222859_2_.z);
		if (this.entity.getPathPriority(pathnodetype) >= 0.0F && pathnodetype1 != PathNodeType.STICKY_HONEY) {
			j = adjusted_j;
		}

		double d0 = getGroundY(this.blockaccess, new BlockPos(p_222859_2_.x, p_222859_2_.y, p_222859_2_.z));
		PathPoint pathpoint = this.getSafePoint(p_222859_2_.x, p_222859_2_.y, p_222859_2_.z + 1, j, d0,
				Direction.SOUTH, pathnodetype1);
		if (this.func_237235_a_(pathpoint, p_222859_2_)) {
			p_222859_1_[i++] = pathpoint;
		}

		PathPoint pathpoint1 = this.getSafePoint(p_222859_2_.x - 1, p_222859_2_.y, p_222859_2_.z, j, d0,
				Direction.WEST, pathnodetype1);
		if (this.func_237235_a_(pathpoint1, p_222859_2_)) {
			p_222859_1_[i++] = pathpoint1;
		}

		PathPoint pathpoint2 = this.getSafePoint(p_222859_2_.x + 1, p_222859_2_.y, p_222859_2_.z, j, d0,
				Direction.EAST, pathnodetype1);
		if (this.func_237235_a_(pathpoint2, p_222859_2_)) {
			p_222859_1_[i++] = pathpoint2;
		}

		PathPoint pathpoint3 = this.getSafePoint(p_222859_2_.x, p_222859_2_.y, p_222859_2_.z - 1, j, d0,
				Direction.NORTH, pathnodetype1);
		if (this.func_237235_a_(pathpoint3, p_222859_2_)) {
			p_222859_1_[i++] = pathpoint3;
		}

		PathPoint pathpoint4 = this.getSafePoint(p_222859_2_.x - 1, p_222859_2_.y, p_222859_2_.z - 1, j, d0,
				Direction.NORTH, pathnodetype1);
		if (this.func_222860_a(p_222859_2_, pathpoint1, pathpoint3, pathpoint4)) {
			p_222859_1_[i++] = pathpoint4;
		}

		PathPoint pathpoint5 = this.getSafePoint(p_222859_2_.x + 1, p_222859_2_.y, p_222859_2_.z - 1, j, d0,
				Direction.NORTH, pathnodetype1);
		if (this.func_222860_a(p_222859_2_, pathpoint2, pathpoint3, pathpoint5)) {
			p_222859_1_[i++] = pathpoint5;
		}

		PathPoint pathpoint6 = this.getSafePoint(p_222859_2_.x - 1, p_222859_2_.y, p_222859_2_.z + 1, j, d0,
				Direction.SOUTH, pathnodetype1);
		if (this.func_222860_a(p_222859_2_, pathpoint1, pathpoint, pathpoint6)) {
			p_222859_1_[i++] = pathpoint6;
		}

		PathPoint pathpoint7 = this.getSafePoint(p_222859_2_.x + 1, p_222859_2_.y, p_222859_2_.z + 1, j, d0,
				Direction.SOUTH, pathnodetype1);
		if (this.func_222860_a(p_222859_2_, pathpoint2, pathpoint, pathpoint7)) {
			p_222859_1_[i++] = pathpoint7;
		}

		return i;
	}

	/**
	 * Returns a point that the entity can safely move to
	 */
	@Nullable
	private PathPoint getSafePoint(int x, int y, int z, int stepHeight, double groundYIn, Direction facing,
			PathNodeType p_186332_8_) {
		PathPoint pathpoint = null;
		BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
		double d0 = getGroundY(this.blockaccess, blockpos$mutable.setPos(x, y, z));
		if (d0 - groundYIn > jumpHeight) {
			return null;
		} else {
			PathNodeType pathnodetype = this.func_237230_a_(this.entity, x, y, z);
			float f = this.entity.getPathPriority(pathnodetype);
			double d1 = (double) this.entity.getWidth() / 2.0D;
			if (f >= 0.0F) {
				pathpoint = this.openPoint(x, y, z);
				pathpoint.nodeType = pathnodetype;
				pathpoint.costMalus = Math.max(pathpoint.costMalus, f);
			}

			if (p_186332_8_ == PathNodeType.FENCE && pathpoint != null && pathpoint.costMalus >= 0.0F
					&& !this.func_237234_a_(pathpoint)) {
				pathpoint = null;
			}

			if (pathnodetype == PathNodeType.WALKABLE) {
				return pathpoint;
			} else {
				if ((pathpoint == null || pathpoint.costMalus < 0.0F) && stepHeight > 0
						&& pathnodetype != PathNodeType.FENCE && pathnodetype != PathNodeType.UNPASSABLE_RAIL
						&& pathnodetype != PathNodeType.TRAPDOOR) {
					pathpoint = this.getSafePoint(x, y + 1, z, stepHeight - 1, groundYIn, facing, p_186332_8_);
					if (pathpoint != null
							&& (pathpoint.nodeType == PathNodeType.OPEN || pathpoint.nodeType == PathNodeType.WALKABLE)
							&& this.entity.getWidth() < 1.0F) {
						double d2 = (double) (x - facing.getXOffset()) + 0.5D;
						double d3 = (double) (z - facing.getZOffset()) + 0.5D;
						AxisAlignedBB axisalignedbb = new AxisAlignedBB(d2 - d1,
								getGroundY(this.blockaccess, blockpos$mutable.setPos(d2, (double) (y + 1), d3)) + 0.001D, d3 - d1,
								d2 + d1, (double) this.entity.getHeight() + getGroundY(this.blockaccess,
										blockpos$mutable.setPos(pathpoint.x, pathpoint.y, pathpoint.z)) - 0.002D,
								d3 + d1);
						if (this.func_237236_a_(axisalignedbb)) {
							pathpoint = null;
						}
					}
				}

				if (pathnodetype == PathNodeType.WATER && !this.getCanSwim()) {
					if (this.func_237230_a_(this.entity, x, y - 1, z) != PathNodeType.WATER) {
						return pathpoint;
					}

					while (y > 0) {
						--y;
						pathnodetype = this.func_237230_a_(this.entity, x, y, z);
						if (pathnodetype != PathNodeType.WATER) {
							return pathpoint;
						}

						pathpoint = this.openPoint(x, y, z);
						pathpoint.nodeType = pathnodetype;
						pathpoint.costMalus = Math.max(pathpoint.costMalus, this.entity.getPathPriority(pathnodetype));
					}
				}

				if (pathnodetype == PathNodeType.OPEN) {
					int j = 0;
					int i = y;

					while (pathnodetype == PathNodeType.OPEN) {
						--y;
						if (y < 0) {
							PathPoint pathpoint3 = this.openPoint(x, i, z);
							pathpoint3.nodeType = PathNodeType.BLOCKED;
							pathpoint3.costMalus = -1.0F;
							return pathpoint3;
						}

						if (j++ >= this.entity.getMaxFallHeight()) {
							PathPoint pathpoint2 = this.openPoint(x, y, z);
							pathpoint2.nodeType = PathNodeType.BLOCKED;
							pathpoint2.costMalus = -1.0F;
							return pathpoint2;
						}

						pathnodetype = this.func_237230_a_(this.entity, x, y, z);
						f = this.entity.getPathPriority(pathnodetype);
						if (pathnodetype != PathNodeType.OPEN && f >= 0.0F) {
							pathpoint = this.openPoint(x, y, z);
							pathpoint.nodeType = pathnodetype;
							pathpoint.costMalus = Math.max(pathpoint.costMalus, f);
							break;
						}

						if (f < 0.0F) {
							PathPoint pathpoint1 = this.openPoint(x, y, z);
							pathpoint1.nodeType = PathNodeType.BLOCKED;
							pathpoint1.costMalus = -1.0F;
							return pathpoint1;
						}
					}
				}

				if (pathnodetype == PathNodeType.FENCE) {
					pathpoint = this.openPoint(x, y, z);
					pathpoint.visited = true;
					pathpoint.nodeType = pathnodetype;
					pathpoint.costMalus = pathnodetype.getPriority();
				}

				return pathpoint;
			}
		}
	}
}
