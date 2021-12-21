package com.lupicus.nasty.sound;

import com.lupicus.nasty.Main;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class ModSounds
{
	public static final SoundEvent ARMOR_DROP = create("item.armor.drop");

	private static SoundEvent create(String key)
	{
		ResourceLocation res = new ResourceLocation(Main.MODID, key);
		SoundEvent ret = new SoundEvent(res);
		ret.setRegistryName(res);
		return ret;
	}

	public static void register(IForgeRegistry<SoundEvent> registry)
	{
		registry.register(ARMOR_DROP);
	}
}
