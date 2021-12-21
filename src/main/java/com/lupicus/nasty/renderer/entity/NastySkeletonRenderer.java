package com.lupicus.nasty.renderer.entity;

import com.lupicus.nasty.Main;
import com.lupicus.nasty.entity.NastySkeletonEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NastySkeletonRenderer extends SkeletonRenderer
{
	private static final ResourceLocation[] TEXTURES = {
			new ResourceLocation(Main.MODID, "textures/entity/skeleton_0.png"),
			new ResourceLocation(Main.MODID, "textures/entity/skeleton_1.png"),
			new ResourceLocation(Main.MODID, "textures/entity/skeleton_2.png"),
			new ResourceLocation(Main.MODID, "textures/entity/skeleton_3.png"),
			new ResourceLocation(Main.MODID, "textures/entity/skeleton_4.png"),
			new ResourceLocation(Main.MODID, "textures/entity/skeleton_5.png"),
	};

	public NastySkeletonRenderer(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	public ResourceLocation getTextureLocation(AbstractSkeleton entity) {
		int subtype = ((NastySkeletonEntity) entity).getSubType();
		return TEXTURES[subtype];
	}
}
