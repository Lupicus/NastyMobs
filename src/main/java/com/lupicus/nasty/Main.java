package com.lupicus.nasty;

import com.lupicus.nasty.config.MyConfig;
import com.lupicus.nasty.entity.ModEntities;
import com.lupicus.nasty.item.ModItems;
import com.lupicus.nasty.sound.ModSounds;

import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Main.MODID)
public class Main
{
    public static final String MODID = "nasty";

    public Main()
    {
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MyConfig.COMMON_SPEC);
    }

	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public void setup(final FMLCommonSetupEvent event)
	{
		net.minecraftforge.fml.DeferredWorkQueue.runLater(() -> ModItems.setup());
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void setupClient(final FMLClientSetupEvent event)
	{
		ModEntities.setupClient();
	}

	@SubscribeEvent
	public void setupServer(final FMLDedicatedServerSetupEvent event)
	{
	}

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents
    {
    	@SubscribeEvent
    	public static void onItemsRegistry(final RegistryEvent.Register<Item> event)
    	{
    		ModItems.register(event.getRegistry());
    	}

        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public static void onColorsRegistry(final ColorHandlerEvent.Item event)
        {
        	ModItems.register(event.getItemColors());
        }

        @SubscribeEvent
        public static void onEntityRegistry(final RegistryEvent.Register<EntityType<?>> event)
        {
        	ModEntities.register(event.getRegistry());
        }

        @SubscribeEvent
        public static void onSoundRegistry(final RegistryEvent.Register<SoundEvent> event)
        {
        	ModSounds.register(event.getRegistry());
        }
    }

    @Mod.EventBusSubscriber()
    public static class ForgeEvents
    {
        @SubscribeEvent
        public static void onServerStarting(FMLServerStartingEvent event)
        {
        	ModEntities.addSpawnData();
        }

        @SubscribeEvent
        public static void onServerStopping(FMLServerStoppingEvent event)
        {
        	ModEntities.removeSpawnData();
        }
    }
}
