package foundry.veil.fabric;

import foundry.veil.Veil;
import foundry.veil.VeilClient;
import foundry.veil.fabric.event.FreeNativeResourcesEvent;
import foundry.veil.render.pipeline.VeilRenderSystem;
import foundry.veil.render.ui.VeilUITooltipRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class VeilFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        VeilClient.init();
        HudRenderCallback.EVENT.register((matrices, tickDelta) -> {
            VeilUITooltipRenderer.OVERLAY.render(Minecraft.getInstance().gui, matrices, tickDelta, Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight());
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> VeilClient.tickClient(client.getFrameTime()));
        FreeNativeResourcesEvent.EVENT.register(VeilRenderSystem::close);

        KeyBindingHelper.registerKeyBinding(VeilClient.EDITOR_KEY);

        // Register test resource pack
        FabricLoader loader = FabricLoader.getInstance();
        if (Veil.DEBUG && loader.isDevelopmentEnvironment()) {
            ResourceManagerHelper.registerBuiltinResourcePack(Veil.veilPath("test_shaders"), loader.getModContainer(Veil.MODID).orElseThrow(), ResourcePackActivationType.DEFAULT_ENABLED);
        }
    }
}
