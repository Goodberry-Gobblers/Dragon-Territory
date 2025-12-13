package io.github.goodberry_gobblers.dragonterritory;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.entity.WitherBossRenderer;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(DragonTerritory.MOD_ID)
public class DragonTerritory
{
    public static final String MOD_ID = "dragonterritory";
    private static final Logger LOGGER = LogUtils.getLogger();

    public DragonTerritory(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }
}
