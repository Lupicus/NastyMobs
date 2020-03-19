package com.lupicus.nasty.entity.ai.controller;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.potion.Effects;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;

public class JumpMovementController extends MovementController
{
	public JumpMovementController(MobEntity mob)
	{
		super(mob);
	}

	@Override
	public void tick()
	{
		if (this.action == MovementController.Action.MOVE_TO)
		{
	         this.action = MovementController.Action.WAIT;
	         double d0 = this.posX - this.mob.getPosX();
	         double d1 = this.posZ - this.mob.getPosZ();
	         double d2 = this.posY - this.mob.getPosY();
	         double d3 = d0 * d0 + d2 * d2 + d1 * d1;
	         if (d3 < (double)2.5000003E-7F) {
	            this.mob.setMoveForward(0.0F);
	            return;
	         }

	         float f9 = (float)(MathHelper.atan2(d1, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
	         this.mob.rotationYaw = this.limitAngle(this.mob.rotationYaw, f9, 90.0F);
	         this.mob.setAIMoveSpeed((float)(this.speed * this.mob.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue()));
	         BlockPos blockpos = new BlockPos(this.mob);
	         BlockState blockstate = this.mob.world.getBlockState(blockpos);
	         Block block = blockstate.getBlock();
	         VoxelShape voxelshape = blockstate.getCollisionShape(this.mob.world, blockpos);
	         if (d2 > (double)this.mob.stepHeight && d0 * d0 + d1 * d1 < (double)Math.max(1.0F, this.mob.getWidth()) || !voxelshape.isEmpty() && this.mob.getPosY() < voxelshape.getEnd(Direction.Axis.Y) + (double)blockpos.getY() &&
	        		 (mob.isPotionActive(Effects.JUMP_BOOST) || (!block.isIn(BlockTags.DOORS) && !block.isIn(BlockTags.FENCES)))) {
	            this.mob.getJumpController().setJumping();
	            this.action = MovementController.Action.JUMPING;
	         }
		}
		else
			super.tick();
	}
}
