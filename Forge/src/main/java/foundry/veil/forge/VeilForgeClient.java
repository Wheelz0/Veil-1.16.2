package foundry.veil.forge;

import foundry.veil.VeilClient;
import foundry.veil.forge.event.ForgeVeilRendererEvent;
import foundry.veil.platform.services.VeilEventPlatform;
import foundry.veil.render.pipeline.VeilRenderSystem;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.ApiStatus;

import static foundry.veil.forge.VeilForgeClientEvents.OVERLAY;

@ApiStatus.Internal
public class VeilForgeClient {

    public static void init() {
        VeilClient.init();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(VeilForgeClient::registerKeys);
        modEventBus.addListener(VeilForgeClient::registerGuiOverlays);
        modEventBus.addListener(VeilForgeClient::registerListeners);
    }

    private static void registerListeners(RegisterClientReloadListenersEvent event) {
        VeilClient.initRenderer();
        MinecraftForge.EVENT_BUS.post(new ForgeVeilRendererEvent(VeilRenderSystem.renderer()));
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(VeilClient.EDITOR_KEY);
    }

    private static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "uitooltip", OVERLAY);
    }
}
