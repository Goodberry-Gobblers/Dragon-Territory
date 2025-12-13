package io.github.goodberry_gobblers.dragonterritory;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.goodberry_gobblers.dragonterritory.render.SilhouetteRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DragonTerritory.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            RenderSystem.runAsFancy(() -> SilhouetteRenderer.renderEntireBatch(event.getLevelRenderer(), event.getPoseStack(), event.getRenderTick(), event.getCamera(), event.getPartialTick()));
        }
    }

}
