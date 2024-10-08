package com.lupicus.nasty.entity;

import java.util.ArrayList;

import javax.annotation.Nullable;

import com.lupicus.nasty.config.MyConfig;
import com.lupicus.nasty.item.ModItems;
import com.lupicus.nasty.sound.ModSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

public class ExplosiveArrowEntity extends AbstractArrow
{
	private BlockPos target;

	public ExplosiveArrowEntity(EntityType<? extends ExplosiveArrowEntity> type, Level world)
	{
		super(type, world);
	}

	public ExplosiveArrowEntity(Level worldIn, double x, double y, double z, ItemStack stack, @Nullable ItemStack weapon)
	{
		super(ModEntities.EXPLOSIVE_ARROW, x, y, z, worldIn, stack, weapon);
	}

	public ExplosiveArrowEntity(Level worldIn, LivingEntity shooter, ItemStack stack, @Nullable ItemStack weapon)
	{
		super(ModEntities.EXPLOSIVE_ARROW, shooter, worldIn, stack, weapon);
	}

	@Override
	protected void onHitBlock(BlockHitResult result)
	{
		super.onHitBlock(result);
		target = result.getBlockPos().immutable();
		//world.createExplosion(this, target.getX()+0.5, target.getY()+0.5, target.getZ()+0.5, 0.5F, Explosion.BlockInteraction.DESTROY);
		createExplosion(target.getX()+0.5, target.getY()+0.5, target.getZ()+0.5, 0.5F, Explosion.BlockInteraction.DESTROY);
		this.discard();
	}

	/**
	 * Modified version from ServerLevel.explode
	 */
	private Explosion createExplosion(double x, double y, double z, float r, BlockInteraction mode)
	{
		Level level = level();
		if (!(level instanceof ServerLevel))
			return null;
		ServerLevel world = (ServerLevel) level;
		SimpleExplosion e = new SimpleExplosion(world, this, x, y, z, r, false, mode);

		if (ForgeEventFactory.onExplosionStart(world, e))
			return e;

		e.explode();
		e.finalizeExplosion(false);
		if (!e.interactsWithBlocks())
			e.clearToBlow();

		for (ServerPlayer serverplayerentity : world.players())
		{
			if (serverplayerentity.distanceToSqr(x, y, z) < 4096.0D)
			{
				serverplayerentity.connection.send(new ClientboundExplodePacket(x, y, z, r, e.getToBlow(),
						e.getHitPlayers().get(serverplayerentity), e.getBlockInteraction(),
						e.getSmallExplosionParticles(), e.getLargeExplosionParticles(), e.getExplosionSound()));
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
				slot = randomArmorSlot(le);
			ItemStack stack = le.getItemBySlot(slot);
			if (!stack.isEmpty()) {
				le.setItemSlot(slot, ItemStack.EMPTY);
				Level level = le.level();
				Vec3 pos = le.position();
				ItemEntity itementity = new ItemEntity(level, pos.x, pos.y + 1.0, pos.z, stack);
				itementity.setDefaultPickUpDelay();
				float f = random.nextFloat() * 0.5F;
				float f1 = random.nextFloat() * ((float)Math.PI * 2F);
				itementity.setDeltaMovement((double)(-Mth.sin(f1) * f), 0.2, (double)(Mth.cos(f1) * f));
				level.addFreshEntity(itementity);
				level.playSound(null, le.getX(), le.getY(), le.getZ(), ModSounds.ARMOR_DROP, le.getSoundSource(), 1.0F, 1.0F);
			}
			if (isAlive())
				discard();
		}
	}

	private EquipmentSlot randomArmorSlot(LivingEntity entity)
	{
		if (entity instanceof Mob mob && mob.isWearingBodyArmor())
			return EquipmentSlot.BODY;
		int index = random.nextInt(4);
		for (EquipmentSlot v : EquipmentSlot.values())
		{
			if (v.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && v.getIndex() == index)
				return v;
		}
		return EquipmentSlot.CHEST;
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

		public SimpleExplosion(Level worldIn, Entity exploderIn, double xIn, double yIn, double zIn, float sizeIn,
				boolean causesFireIn, BlockInteraction modeIn) {
			super(worldIn, exploderIn, xIn, yIn, zIn, sizeIn, causesFireIn, modeIn);
			world = worldIn;
			x = xIn;
			y = yIn;
			z = zIn;
		}

		@Override
		public void explode()
		{
			Entity exploder = getDirectSourceEntity();
			BlockPos blockpos = BlockPos.containing(x, y, z);
			world.gameEvent(exploder, GameEvent.EXPLODE, blockpos);
			BlockState blockstate = world.getBlockState(blockpos);
			FluidState fluidstate = world.getFluidState(blockpos);
			if (world.isInWorldBounds(blockpos) && (!blockstate.isAir() || !fluidstate.isEmpty()))
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

	@Override
	protected ItemStack getDefaultPickupItem() {
		return new ItemStack(ModItems.EXPLOSIVE_ARROW);
	}
}
