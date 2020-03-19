package com.lupicus.nasty.renderer.entity;

import com.lupicus.nasty.Main;
import com.lupicus.nasty.entity.NastySkeletonEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.util.ResourceLocation;
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

	public NastySkeletonRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	public ResourceLocation getEntityTexture(AbstractSkeletonEntity entity) {
		int subtype = ((NastySkeletonEntity) entity).getSubType();
		return TEXTURES[subtype];
	}
}
