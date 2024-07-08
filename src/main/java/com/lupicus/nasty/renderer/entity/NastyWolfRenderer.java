package com.lupicus.nasty.renderer.entity;

import com.lupicus.nasty.Main;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NastyWolfRenderer extends WolfRenderer
{
	public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/entity/wolf.png");
	public static final ResourceLocation ANGRY_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/entity/wolf_angry.png");

	public NastyWolfRenderer(EntityRendererProvider.Context renderManagerIn)
	{
		super(renderManagerIn);
	}

	@Override
	public ResourceLocation getTextureLocation(Wolf entity)
	{
		return entity.isAngry() ? ANGRY_TEXTURE : TEXTURE;
	}
}
