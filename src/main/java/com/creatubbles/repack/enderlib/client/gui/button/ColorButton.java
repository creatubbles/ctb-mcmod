package com.creatubbles.repack.enderlib.client.gui.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.item.ItemDye;
import net.minecraft.util.MathHelper;

import org.lwjgl.opengl.GL11;

import com.creatubbles.repack.endercore.common.util.DyeColor;
import com.creatubbles.repack.enderlib.api.client.gui.IGuiScreen;

public class ColorButton extends IconButton {

  private int colorIndex = 0;

  private String tooltipPrefix = "";

  public ColorButton(IGuiScreen gui, int id, int x, int y) {
    super(gui, id, x, y, null);
  }

  @Override
  public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3) {
    boolean result = super.mousePressed(par1Minecraft, par2, par3);
    if (result) {
      nextColor();
    }
    return result;
  }

  @Override
  public boolean mousePressedButton(Minecraft mc, int x, int y, int button) {
    boolean result = button == 1 && super.checkMousePress(mc, x, y);
    if (result) {
      prevColor();
    }
    return result;
  }

  public String getTooltipPrefix() {
    return tooltipPrefix;
  }

  public void setToolTipHeading(String tooltipPrefix) {
    if (tooltipPrefix == null) {
      this.tooltipPrefix = "";
    } else {
      this.tooltipPrefix = tooltipPrefix;
    }
  }

  private void nextColor() {
    colorIndex++;
    if (colorIndex >= ItemDye.dyeColors.length) {
      colorIndex = 0;
    }
    setColorIndex(colorIndex);
  }

  private void prevColor() {
    colorIndex--;
    if (colorIndex < 0) {
      colorIndex = ItemDye.dyeColors.length - 1;
    }
    setColorIndex(colorIndex);
  }

  public int getColorIndex() {
    return colorIndex;
  }

  public void setColorIndex(int colorIndex) {
    this.colorIndex = MathHelper.clamp_int(colorIndex, 0, ItemDye.dyeColors.length - 1);
    String colStr = DyeColor.values()[colorIndex].getLocalisedName();
    if (tooltipPrefix != null && tooltipPrefix.length() > 0) {
      setToolTip(tooltipPrefix, colStr);
    } else {
      setToolTip(colStr);
    }
  }

  @Override
  public void drawButton(Minecraft mc, int mouseX, int mouseY) {
    super.drawButton(mc, mouseX, mouseY);
    if (visible) {
      WorldRenderer renderer = Tessellator.getInstance().getWorldRenderer();
      renderer.startDrawingQuads();

      int x = xPosition + 2;
      int y = yPosition + 2;

      GL11.glDisable(GL11.GL_TEXTURE_2D);

      int col = ItemDye.dyeColors[colorIndex];
      renderer.func_178991_c(col);
      renderer.addVertex(x, y + height - 4, zLevel);
      renderer.addVertex(x + width - 4, y + height - 4, zLevel);
      renderer.addVertex(x + width - 4, y + 0, zLevel);
      renderer.addVertex(x, y + 0, zLevel);

      renderer.draw();

      GL11.glEnable(GL11.GL_TEXTURE_2D);

    }
  }
}
