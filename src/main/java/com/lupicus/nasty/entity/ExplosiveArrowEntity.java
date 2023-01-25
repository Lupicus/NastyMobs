package com.lupicus.nasty.entity;

import java.util.ArrayList;
import javax.annotation.Nullable;

import com.lupicus.nasty.config.MyConfig;
import com.lupicus.nasty.sound.ModSounds;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.EquipmentSlotType.Group;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SExplosionPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.Explosion.Mode;
import net.minecraft.world.ExplosionContext;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;

public class ExplosiveArrowEntity extends ArrowEntity
{
	private BlockPos target;

	public ExplosiveArrowEntity(EntityType<? extends ArrowEntity> type, World world)
	{
		super(type, world);
	}

	public ExplosiveArrowEntity(World worldIn, double x, double y, double z)
	{
		super(worldIn, x, y, z);
	}

	public ExplosiveArrowEntity(World worldIn, LivingEntity shooter)
	{
		super(worldIn, shooter);
	}

	@Override
	protected void onImpact(RayTraceResult raytraceResultIn)
	{
		super.onImpact(raytraceResultIn);
		if (raytraceResultIn.getType() == RayTraceResult.Type.BLOCK)
		{
			BlockRayTraceResult result = (BlockRayTraceResult) raytraceResultIn;
			target = result.getPos();
			//world.createExplosion(this, target.getX()+0.5, target.getY()+0.5, target.getZ()+0.5, 0.5F, Explosion.Mode.BREAK);
			createExplosion(target.getX()+0.5, target.getY()+0.5, target.getZ()+0.5, 0.5F, Explosion.Mode.BREAK);
			this.remove();
		}
	}

	private Explosion createExplosion(double x, double y, double z, float r, Mode mode)
	{
		return createExplosion((DamageSource) null, x, y, z, r, mode);
	}

	/**
	 * Modified version from ServerWorld.createExplosion
	 */
	private Explosion createExplosion(@Nullable DamageSource source, double x, double y, double z, float r, Mode mode)
	{
		if (!(world instanceof ServerWorld))
			return null;
		ServerWorld world = (ServerWorld) this.world;
		SimpleExplosion e = new SimpleExplosion(world, this, source, null, x, y, z, r, false, mode);

		if (ForgeEventFactory.onExplosionStart(world, e))
			return e;

		e.doExplosionA();
		e.doExplosionB(false);
		if (mode == Explosion.Mode.NONE)
			e.clearAffectedBlockPositions();

		for (ServerPlayerEntity serverplayerentity : world.getPlayers())
		{
			if (serverplayerentity.getDistanceSq(x, y, z) < 4096.0D)
			{
				serverplayerentity.connection.sendPacket(new SExplosionPacket(x, y, z, r, e.getAffectedBlockPositions(),
						e.getPlayerKnockbackMap().get(serverplayerentity)));
			}
		}

		return e;
	}

	@Override
	public boolean canExplosionDestroyBlock(Explosion explosionIn, IBlockReader worldIn, BlockPos pos,
			BlockState blockStateIn, float f)
	{
		if (!target.equals(pos))
			return false;
		return MyConfig.explosiveArrowOnBlock;
	}

	@Override
	protected void onEntityHit(EntityRayTraceResult raytraceResultIn)
	{
		Entity entity = raytraceResultIn.getEntity();
		LivingEntity le = null;
		boolean blocking = false;
		EquipmentSlotType slot = EquipmentSlotType.OFFHAND;
		double rot = 0;
		if (MyConfig.explosiveArrowOnArmor && entity instanceof LivingEntity)
		{
			le = (LivingEntity) entity;
			if (MyConfig.explosiveArrowOnShield)
			{
				Entity se = func_234616_v_(); // getShooter
				if (se == null)
					se = this;
				blocking = canBlockDamageSource(le, se);
				if (blocking && le.getActiveHand() == Hand.MAIN_HAND)
					slot = EquipmentSlotType.MAINHAND;
			}
			rot = rotationYaw;
		}
		super.onEntityHit(raytraceResultIn);
		if (blocking || (le != null && rot == rotationYaw)) {
			// if blocking with shield then it is dropped
			// else random armor slot is picked and that is dropped
			if (!blocking)
				slot = EquipmentSlotType.fromSlotTypeAndIndex(Group.ARMOR, rand.nextInt(4));
			ItemStack stack = le.getItemStackFromSlot(slot);
			if (!stack.isEmpty()) {
				le.setItemStackToSlot(slot, ItemStack.EMPTY);
				Vector3d pos = le.getPositionVec();
				ItemEntity itementity = new ItemEntity(le.world, pos.x, pos.y + 1.0, pos.z, stack);
				itementity.setDefaultPickupDelay();
				float f = rand.nextFloat() * 0.5F;
				float f1 = rand.nextFloat() * ((float)Math.PI * 2F);
				itementity.setMotion((double)(-MathHelper.sin(f1) * f), 0.2, (double)(MathHelper.cos(f1) * f));
				le.world.addEntity(itementity);
				le.world.playSound(null, le.getPosX(), le.getPosY(), le.getPosZ(), ModSounds.ARMOR_DROP, le.getSoundCategory(), 1.0F, 1.0F);
			}
			if (isAlive())
				remove();
		}
	}

	/**
	 * A condensed version from LivingEntity.canBlockDamageSource
	 * @param target
	 * @param source
	 * @return
	 */
	private boolean canBlockDamageSource(LivingEntity target, Entity source)
	{
		if (target.isActiveItemStackBlocking() && !(getPierceLevel() > 0)) {
			Vector3d vec3d2 = source.getPositionVec();
			Vector3d vec3d = target.getLook(1.0F);
			Vector3d vec3d1 = vec3d2.subtractReverse(target.getPositionVec()).normalize();
			vec3d1 = new Vector3d(vec3d1.x, 0.0D, vec3d1.z);
			if (vec3d1.dotProduct(vec3d) < 0.0D) {
				return true;
			}
		}

		return false;
	}

	@Override
	public float getExplosionResistance(Explosion explosionIn, IBlockReader worldIn, BlockPos pos,
			BlockState blockStateIn, FluidState ifluidstateIn, float f2) {
		return f2 / MyConfig.explosiveArrowStrength;
	}

	public static class SimpleExplosion extends Explosion
	{
		private double x;
		private double y;
		private double z;
		private World world;

		public SimpleExplosion(World worldIn, Entity exploderIn, @Nullable DamageSource dmgsrc, @Nullable ExplosionContext ctx, double xIn, double yIn, double zIn, float sizeIn,
				boolean causesFireIn, Mode modeIn) {
			super(worldIn, exploderIn, dmgsrc, ctx, xIn, yIn, zIn, sizeIn, causesFireIn, modeIn);
			world = worldIn;
			x = xIn;
			y = yIn;
			z = zIn;
		}

		@SuppressWarnings("deprecation")
		@Override
		public void doExplosionA()
		{
			Entity exploder = getExploder();
			BlockPos blockpos = new BlockPos(x, y, z);
			BlockState blockstate = world.getBlockState(blockpos);
			FluidState fluidstate = world.getFluidState(blockpos);
			if (!blockstate.isAir(world, blockpos) || !fluidstate.isEmpty())
			{
				float f2 = Math.max(blockstate.getExplosionResistance(world, blockpos, this),
									fluidstate.getExplosionResistance(world, blockpos, this));
				if (exploder != null)
					f2 = exploder.getExplosionResistance(this, world, blockpos, blockstate, fluidstate, f2);

				if (f2 < 1.0F && (exploder == null
						|| exploder.canExplosionDestroyBlock(this, world, blockpos, blockstate, f2)))
					getAffectedBlockPositions().add(blockpos);
			}
			ForgeEventFactory.onExplosionDetonate(world, this, new ArrayList<Entity>(), 1.0);
		}
	}
}
