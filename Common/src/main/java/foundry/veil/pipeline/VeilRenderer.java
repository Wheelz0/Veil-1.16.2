package foundry.veil.pipeline;

import foundry.veil.render.CameraMatrices;
import foundry.veil.render.framebuffer.FramebufferManager;
import foundry.veil.render.post.PostProcessingManager;
import foundry.veil.render.shader.ShaderManager;
import foundry.veil.render.shader.definition.ShaderPreDefinitions;

/**
 * Manages the render pipeline for Veil.
 *
 * @author Ocelot
 */
public interface VeilRenderer {

    /**
     * @return The set of shader pre-definitions. Changes are automatically synced the next frame
     */
    default ShaderPreDefinitions getDefinitions() {
        return this.getShaderManager().getDefinitions();
    }

    /**
     * @return The manager for all veil shaders
     */
    ShaderManager getShaderManager();

    FramebufferManager getFramebufferManager();

    PostProcessingManager getPostProcessingManager();

    CameraMatrices getCameraMatrices();
}
