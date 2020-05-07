package com.lupicus.nasty.renderer.entity;

import com.lupicus.nasty.Main;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NastyWolfRenderer extends WolfRenderer
{
	public static final ResourceLocation TEXTURE = new ResourceLocation(Main.MODID, "textures/entity/wolf.png");
	public static final ResourceLocation ANGRY_TEXTURE = new ResourceLocation(Main.MODID, "textures/entity/wolf_angry.png");

	public NastyWolfRenderer(EntityRendererManager renderManagerIn)
	{
		super(renderManagerIn);
	}

	@Override
	public ResourceLocation getEntityTexture(WolfEntity entity)
	{
		return entity.isAngry() ? ANGRY_TEXTURE : TEXTURE;
	}
}
