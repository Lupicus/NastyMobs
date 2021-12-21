package com.lupicus.nasty.entity.ai.controller;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class JumpMovementController extends MoveControl
{
	public JumpMovementController(Mob mob)
	{
		super(mob);
	}

	@Override
	public void tick()
	{
		if (this.operation == MoveControl.Operation.MOVE_TO)
		{
	         this.operation = MoveControl.Operation.WAIT;
	         double d0 = this.wantedX - this.mob.getX();
	         double d1 = this.wantedZ - this.mob.getZ();
	         double d2 = this.wantedY - this.mob.getY();
	         double d3 = d0 * d0 + d2 * d2 + d1 * d1;
	         if (d3 < 2.500000277905201E-7D) {
	            this.mob.setZza(0.0F);
	            return;
	         }

	         float f9 = (float) (Mth.atan2(d1, d0) * (180.0 / Math.PI)) - 90.0F;
	         this.mob.setYRot(this.rotlerp(this.mob.getYRot(), f9, 90.0F));
	         this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
	         BlockPos blockpos = mob.blockPosition();
	         BlockState blockstate = this.mob.level.getBlockState(blockpos);
	         VoxelShape voxelshape = blockstate.getCollisionShape(this.mob.level, blockpos);
	         if (d2 > (double) this.mob.maxUpStep && d0 * d0 + d1 * d1 < (double) Math.max(1.0F, this.mob.getBbWidth()) || !voxelshape.isEmpty() && this.mob.getY() < voxelshape.max(Direction.Axis.Y) + (double)blockpos.getY() &&
	        		 (mob.hasEffect(MobEffects.JUMP) || (!blockstate.is(BlockTags.DOORS) && !blockstate.is(BlockTags.FENCES)))) {
	            this.mob.getJumpControl().jump();
	            this.operation = MoveControl.Operation.JUMPING;
	         }
		}
		else
			super.tick();
	}
}
