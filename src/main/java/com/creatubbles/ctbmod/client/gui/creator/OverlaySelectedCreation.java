package com.creatubbles.ctbmod.client.gui.creator;

import java.awt.Dimension;
import java.awt.Rectangle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import com.creatubbles.api.core.Creation;
import com.creatubbles.api.core.Image.ImageType;
import com.creatubbles.ctbmod.client.gui.GuiUtil;
import com.creatubbles.ctbmod.client.gui.GuiUtil.Bubbles;
import com.creatubbles.ctbmod.client.gui.TextUtil;
import com.creatubbles.ctbmod.common.http.CreationRelations;
import com.creatubbles.ctbmod.common.http.DownloadableImage;
import com.creatubbles.repack.endercore.client.gui.button.IconButton;

public class OverlaySelectedCreation extends OverlayBase<GuiCreator> implements ISelectionCallback {

    private final IconButton createButton;

    private Creation selected;

    protected OverlaySelectedCreation(int x, int y, OverlayCreationList creationList, IconButton createButton) {
        super(x, y, new Dimension());
        this.createButton = createButton;
    }

    @Override
    protected void doDraw(int mouseX, int mouseY, float partialTick) {

        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiCreator.OVERLAY_TEX);
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        GlStateManager.enableBlend();

        drawTexturedModalRect(xRel, yRel, 94, 0, 54, 54);

        int x = xRel + 3;
        int y = yRel + 3;

        if (selected != null) {
            DownloadableImage img = getGui().images.get(selected);
            ResourceLocation res;
            int imgWidth = 16, imgHeight = 16;
            int scaledW = 16, scaledH = 16;
            if (img.hasSize(ImageType.full_view)) {
                res = img.getResource(ImageType.full_view);
                imgWidth = img.getWidth(ImageType.full_view);
                imgHeight = img.getHeight(ImageType.full_view);
                scaledW = img.getScaledWidth(ImageType.full_view);
                scaledH = img.getScaledHeight(ImageType.full_view);
            } else {
                res = null;
                img.download(ImageType.full_view);
            }

            if (res != null) {
                Minecraft.getMinecraft().getTextureManager().bindTexture(res);
            } else {
                GuiUtil.drawLoadingTex(x, y, 48, 48);
                return;
            }

            Rectangle bounds = new Rectangle(x, y, 48, 48);
            Rectangle area = new Rectangle(0, 0, imgWidth, imgHeight);
            GuiUtil.drawRectInscribed(area, bounds, scaledW, scaledH);

            x += 24;
            y += 56;
            TextUtil.drawCenteredSplitString(fr, TextFormatting.ITALIC + selected.getName(), x, y, 54, 0xFFFFFF);
        } else {
            Bubbles.CLEAR.getMap().render(Bubbles.CLEAR, x, y, 48, 48, 0, true);
            x += 24;
            y += 56;
            TextUtil.drawCenteredSplitString(fr, "No Selection", x, y, 54, 0xFFFFFF);
        }
    }

    @Override
    public void callback(CreationRelations selected) {
        this.selected = selected;
        createButton.enabled = selected != null;
    }
}
