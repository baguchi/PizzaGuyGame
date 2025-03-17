package baguchi.pizza_guy_move.client;

import baguchi.pizza_guy_move.PizzaGuyGame;
import baguchi.pizza_guy_move.api.IShadow;
import baguchi.pizza_guy_move.api.ShadowHandler;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

@EventBusSubscriber(modid = PizzaGuyGame.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ClientRegistry {
    public static final ContextKey<ShadowHandler> SHADOW = new ContextKey<>(ResourceLocation.fromNamespaceAndPath(PizzaGuyGame.MODID, "shadow"));

/*    @SubscribeEvent
    public static void registerOverlay(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("timer", new TimerOverlay());
    }*/

    @SubscribeEvent
    public static void registerRenderState(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(PlayerRenderer.class, (living, renderstate) -> {
            if (living instanceof IShadow shadowHandler) {
                renderstate.setRenderData(SHADOW, shadowHandler.getShadowHandler());
            }
        });
    }
}
