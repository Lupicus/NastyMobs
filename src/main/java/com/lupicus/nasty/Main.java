package com.lupicus.nasty;

import org.jetbrains.annotations.NotNull;

import com.lupicus.nasty.config.MyConfig;
import com.lupicus.nasty.entity.ModEntities;
import com.lupicus.nasty.item.ModItems;
import com.lupicus.nasty.sound.ModSounds;
import com.lupicus.nasty.util.ModBiomeModifier;
import com.lupicus.nasty.util.ModStructureModifier;
import com.lupicus.nasty.util.SpawnData;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Main.MODID)
public class Main
{
    public static final String MODID = "nasty";

    public Main()
    {
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MyConfig.COMMON_SPEC);
		MinecraftForge.EVENT_BUS.register(SpawnData.class);
    }

	@SubscribeEvent
	public void setup(final FMLCommonSetupEvent event)
	{
		event.enqueueWork(() -> ModItems.setup());
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void setupClient(final FMLClientSetupEvent event)
	{
	}

	@SubscribeEvent
	public void setupServer(final FMLDedicatedServerSetupEvent event)
	{
	}

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents
    {
    	@SubscribeEvent
	    public static void onRegister(final RegisterEvent event)
	    {
	    	@NotNull
			ResourceKey<? extends Registry<?>> key = event.getRegistryKey();
	    	if (key.equals(ForgeRegistries.Keys.ITEMS))
	    		ModItems.register(event.getForgeRegistry());
	    	else if (key.equals(ForgeRegistries.Keys.ENTITY_TYPES))
	    		ModEntities.register(event.getForgeRegistry());
		    else if (key.equals(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS))
		    	ModBiomeModifier.register(event.getForgeRegistry());
		    else if (key.equals(ForgeRegistries.Keys.STRUCTURE_MODIFIER_SERIALIZERS))
		    	ModStructureModifier.register(event.getForgeRegistry());
		    else if (key.equals(ForgeRegistries.Keys.SOUND_EVENTS))
	    		ModSounds.register(event.getForgeRegistry());
	    }

    	@SubscribeEvent
    	public static void onCreativeTab(BuildCreativeModeTabContentsEvent event)
    	{
    		ModItems.setupTabs(event);
    	}

    	@SubscribeEvent
        public static void onRenderers(final RegisterRenderers event)
        {
        	ModEntities.setupClient(event);
        }

        @SubscribeEvent
        public static void onAttribute(final EntityAttributeCreationEvent event)
        {
        	ModEntities.onAttribute(event);
        }
    }

    @Mod.EventBusSubscriber()
    public static class ForgeEvents
    {
        @SubscribeEvent
        public static void onServerStarting(ServerStartingEvent event)
        {
        	ModEntities.addSpawnData();
        }

        @SubscribeEvent
        public static void onServerStopping(ServerStoppingEvent event)
        {
        	ModEntities.removeSpawnData();
        }
    }
}
