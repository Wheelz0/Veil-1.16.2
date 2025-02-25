package foundry.veil.impl.client.editor;

import foundry.veil.Veil;
import foundry.veil.VeilClient;
import foundry.veil.api.client.editor.SingleWindowEditor;
import foundry.veil.api.client.imgui.VeilImGuiUtil;
import foundry.veil.api.client.registry.VeilResourceEditorRegistry;
import foundry.veil.api.resource.*;
import foundry.veil.api.resource.editor.ResourceFileEditor;
import foundry.veil.impl.resource.VeilPackResources;
import foundry.veil.impl.resource.VeilResourceManagerImpl;
import foundry.veil.impl.resource.VeilResourceRenderer;
import foundry.veil.impl.resource.tree.VeilResourceFolder;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiTreeNodeFlags;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@ApiStatus.Internal
public class ResourceManagerEditor extends SingleWindowEditor implements VeilEditorEnvironment {

    private VeilResource<?> contextResource;
    private List<? extends VeilResourceAction<?>> actions;

    private CompletableFuture<?> reloadFuture;

    @Override
    public void renderComponents() {
        this.contextResource = null;
        this.actions = Collections.emptyList();

        ImGui.beginDisabled(this.reloadFuture != null && !this.reloadFuture.isDone());
        if (ImGui.button("Reload Resources")) {
            this.reloadFuture = Minecraft.getInstance().reloadResourcePacks();
        }
        ImGui.endDisabled();

        VeilResourceManagerImpl resourceManager = VeilClient.resourceManager();
        if (ImGui.beginListBox("##file_tree", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY())) {
            List<VeilPackResources> packs = resourceManager.getAllPacks();
            for (int i = packs.size() - 1; i >= 0; i--) {
                VeilPackResources pack = packs.get(i);
                String modid = pack.getName();
                int color = VeilImGuiUtil.colorOf(modid);

                boolean open = ImGui.treeNodeEx("##" + modid, ImGuiTreeNodeFlags.SpanAvailWidth);

                ImGui.pushStyleColor(ImGuiCol.Text, color);
                ImGui.sameLine();
                int icon = pack.getTexture();
                if (icon != 0) {
                    float size = ImGui.getTextLineHeight();
                    ImGui.image(icon, size, size);
                } else {
                    VeilImGuiUtil.icon(0xEA7D, color);
                }
                ImGui.sameLine();
                ImGui.text(modid);
                ImGui.popStyleColor();

                if (open) {
                    this.renderFolderContents(pack.getRoot());
                    ImGui.treePop();
                }

                ImGui.separator();
            }
            ImGui.endListBox();
        }
    }

    private void renderFolder(VeilResourceFolder folder) {
        boolean open = ImGui.treeNodeEx("##" + folder.getName(), ImGuiTreeNodeFlags.SpanAvailWidth);
        ImGui.sameLine();
        VeilImGuiUtil.icon(open ? 0xED6F : 0xF43B);
        ImGui.sameLine();
        ImGui.text(folder.getName());

        if (open) {
            this.renderFolderContents(folder);
            ImGui.treePop();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void renderFolderContents(VeilResourceFolder folder) {
        for (VeilResourceFolder subFolder : folder.getSubFolders()) {
            this.renderFolder(subFolder);
        }

        ImGui.indent();
        for (VeilResource<?> resource : folder.getResources()) {
            VeilResourceInfo info = resource.resourceInfo();
            if (info.hidden()) {
                continue;
            }

            ImGui.selectable("##" + resource.resourceInfo().location());

            ImGui.setItemAllowOverlap();
            ImGui.sameLine();
            VeilResourceRenderer.renderFilename(resource);

            if (ImGui.beginPopupContextItem("" + resource.resourceInfo().location())) {
                if (resource != this.contextResource) {
                    this.contextResource = resource;
                    this.actions = resource.getActions();
                }

                if (ImGui.selectable("##copy_path")) {
                    ImGui.setClipboardText(info.location().toString());
                }

                ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
                ImGui.setItemAllowOverlap();
                ImGui.sameLine();
                VeilImGuiUtil.icon(0xEB91);
                ImGui.sameLine();
                ImGui.popStyleVar();
                ImGui.text("Copy Path");

                ImGui.beginDisabled(info.isStatic());
                if (ImGui.selectable("##open_folder")) {
                    Path file = info.modResourcePath() != null ? info.modResourcePath() : info.filePath();
                    if (file.getParent() != null) {
                        Util.getPlatform().openFile(file.getParent().toFile());
                    }
                }

                ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
                ImGui.setItemAllowOverlap();
                ImGui.sameLine();
                VeilImGuiUtil.icon(0xECAF);
                ImGui.sameLine();
                ImGui.popStyleVar();
                ImGui.text("Open in Explorer");
                ImGui.endDisabled();

                for (int i = 0; i < this.actions.size(); i++) {
                    VeilResourceAction action = this.actions.get(i);
                    if (ImGui.selectable("##action" + i)) {
                        action.perform(this, resource);
                    }

                    ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
                    ImGui.setItemAllowOverlap();
                    ImGui.sameLine();
                    action.getIcon().ifPresent(icon -> {
                        VeilImGuiUtil.icon(icon);
                        ImGui.sameLine();
                    });
                    ImGui.popStyleVar();
                    ImGui.text(action.getName());
                }

                ImGui.endPopup();
            }
        }
        ImGui.unindent();
    }

    @Override
    public String getDisplayName() {
        return "Resource Browser";
    }

    @Override
    public @Nullable String getGroup() {
        return "Resources";
    }

    @Override
    public void open(VeilResource<?> resource, ResourceLocation editorName) {
        ResourceFileEditor<?> editor = VeilResourceEditorRegistry.REGISTRY.get(editorName);
        if (editor == null) {
            Veil.LOGGER.error("Failed to find editor for resource: {}", resource.resourceInfo().location());
            return;
        }

        try {
            this.open(editor, resource);
        } catch (Throwable t) {
            Veil.LOGGER.error("Failed to open editor for resource: {}", resource.resourceInfo().location(), t);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends VeilResource<?>> void open(ResourceFileEditor<T> editor, VeilResource<?> resource) {
        editor.open(this, (T) resource);
    }

    @Override
    public VeilResourceManager getResourceManager() {
        return VeilClient.resourceManager();
    }
}
