package com.lupicus.nasty.entity;

import java.util.ArrayList;
import javax.annotation.Nullable;

import com.lupicus.nasty.config.MyConfig;
import com.lupicus.nasty.sound.ModSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

public class ExplosiveArrowEntity extends Arrow
{
	private BlockPos target;

	public ExplosiveArrowEntity(EntityType<? extends Arrow> type, Level world)
	{
		super(type, world);
	}

	public ExplosiveArrowEntity(Level worldIn, double x, double y, double z)
	{
		super(worldIn, x, y, z);
	}

	public ExplosiveArrowEntity(Level worldIn, LivingEntity shooter)
	{
		super(worldIn, shooter);
	}

	@Override
	protected void onHit(HitResult raytraceResultIn)
	{
		super.onHit(raytraceResultIn);
		if (raytraceResultIn.getType() == HitResult.Type.BLOCK)
		{
			BlockHitResult result = (BlockHitResult) raytraceResultIn;
			target = result.getBlockPos();
			//world.createExplosion(this, target.getX()+0.5, target.getY()+0.5, target.getZ()+0.5, 0.5F, Explosion.BlockInteraction.DESTROY);
			createExplosion(target.getX()+0.5, target.getY()+0.5, target.getZ()+0.5, 0.5F, Explosion.BlockInteraction.DESTROY);
			this.discard();
		}
	}

	private Explosion createExplosion(double x, double y, double z, float r, BlockInteraction mode)
	{
		return createExplosion((DamageSource) null, x, y, z, r, mode);
	}

	/**
	 * Modified version from ServerWorld.createExplosion
	 */
	private Explosion createExplosion(@Nullable DamageSource source, double x, double y, double z, float r, BlockInteraction mode)
	{
		if (!(level instanceof ServerLevel))
			return null;
		ServerLevel world = (ServerLevel) this.level;
		SimpleExplosion e = new SimpleExplosion(world, this, source, null, x, y, z, r, false, mode);

		if (ForgeEventFactory.onExplosionStart(world, e))
			return e;

		e.explode();
		e.finalizeExplosion(false);
		if (mode == Explosion.BlockInteraction.KEEP)
			e.clearToBlow();

		for (ServerPlayer serverplayerentity : world.players())
		{
			if (serverplayerentity.distanceToSqr(x, y, z) < 4096.0D)
			{
				serverplayerentity.connection.send(new ClientboundExplodePacket(x, y, z, r, e.getToBlow(),
						e.getHitPlayers().get(serverplayerentity)));
			}
		}

		return e;
	}

	@Override
	public boolean shouldBlockExplode(Explosion explosionIn, BlockGetter worldIn, BlockPos pos,
			BlockState blockStateIn, float f)
	{
		if (!target.equals(pos))
			return false;
		return MyConfig.explosiveArrowOnBlock;
	}

	@Override
	protected void onHitEntity(EntityHitResult raytraceResultIn)
	{
		Entity entity = raytraceResultIn.getEntity();
		LivingEntity le = null;
		boolean blocking = false;
		EquipmentSlot slot = EquipmentSlot.OFFHAND;
		double rot = 0;
		if (MyConfig.explosiveArrowOnArmor && entity instanceof LivingEntity)
		{
			le = (LivingEntity) entity;
			if (MyConfig.explosiveArrowOnShield)
			{
				Entity se = getOwner();
				if (se == null)
					se = this;
				blocking = isDamageSourceBlocked(le, se);
				if (blocking && le.getUsedItemHand() == InteractionHand.MAIN_HAND)
					slot = EquipmentSlot.MAINHAND;
			}
			rot = getYRot();
		}
		super.onHitEntity(raytraceResultIn);
		if (blocking || (le != null && rot == getYRot())) {
			// if blocking with shield then it is dropped
			// else random armor slot is picked and that is dropped
			if (!blocking)
				slot = EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, random.nextInt(4));
			ItemStack stack = le.getItemBySlot(slot);
			if (!stack.isEmpty()) {
				le.setItemSlot(slot, ItemStack.EMPTY);
				Vec3 pos = le.position();
				ItemEntity itementity = new ItemEntity(le.level, pos.x, pos.y + 1.0, pos.z, stack);
				itementity.setDefaultPickUpDelay();
				float f = random.nextFloat() * 0.5F;
				float f1 = random.nextFloat() * ((float)Math.PI * 2F);
				itementity.setDeltaMovement((double)(-Mth.sin(f1) * f), 0.2, (double)(Mth.cos(f1) * f));
				le.level.addFreshEntity(itementity);
				le.level.playSound(null, le.getX(), le.getY(), le.getZ(), ModSounds.ARMOR_DROP, le.getSoundSource(), 1.0F, 1.0F);
			}
			if (isAlive())
				discard();
		}
	}

	/**
	 * A condensed version from LivingEntity.isDamageSourceBlocked
	 * @param target
	 * @param source
	 * @return
	 */
	private boolean isDamageSourceBlocked(LivingEntity target, Entity source)
	{
		if (target.isBlocking() && !(getPierceLevel() > 0)) {
			Vec3 vec3d2 = source.position();
			Vec3 vec3d = target.getViewVector(1.0F);
			Vec3 vec3d1 = vec3d2.vectorTo(target.position()).normalize();
			vec3d1 = new Vec3(vec3d1.x, 0.0D, vec3d1.z);
			if (vec3d1.dot(vec3d) < 0.0D) {
				return true;
			}
		}

		return false;
	}

	@Override
	public float getBlockExplosionResistance(Explosion explosionIn, BlockGetter worldIn, BlockPos pos,
			BlockState blockStateIn, FluidState ifluidstateIn, float f2) {
		return f2 / MyConfig.explosiveArrowStrength;
	}

	public static class SimpleExplosion extends Explosion
	{
		private double x;
		private double y;
		private double z;
		private Level world;

		public SimpleExplosion(Level worldIn, Entity exploderIn, @Nullable DamageSource dmgsrc, @Nullable ExplosionDamageCalculator ctx, double xIn, double yIn, double zIn, float sizeIn,
				boolean causesFireIn, BlockInteraction modeIn) {
			super(worldIn, exploderIn, dmgsrc, ctx, xIn, yIn, zIn, sizeIn, causesFireIn, modeIn);
			world = worldIn;
			x = xIn;
			y = yIn;
			z = zIn;
		}

		@Override
		public void explode()
		{
			Entity exploder = getExploder();
			BlockPos blockpos = new BlockPos(x, y, z);
			world.gameEvent(exploder, GameEvent.EXPLODE, blockpos);
			BlockState blockstate = world.getBlockState(blockpos);
			FluidState fluidstate = world.getFluidState(blockpos);
			if (!blockstate.isAir() || !fluidstate.isEmpty())
			{
				float f2 = Math.max(blockstate.getExplosionResistance(world, blockpos, this),
									fluidstate.getExplosionResistance(world, blockpos, this));
				if (exploder != null)
					f2 = exploder.getBlockExplosionResistance(this, world, blockpos, blockstate, fluidstate, f2);

				if (f2 < 1.0F && (exploder == null
						|| exploder.shouldBlockExplode(this, world, blockpos, blockstate, f2)))
					getToBlow().add(blockpos);
			}
			ForgeEventFactory.onExplosionDetonate(world, this, new ArrayList<Entity>(), 1.0);
		}
	}
}
