package com.creatubbles.ctbmod.common.painting;

import com.creatubbles.api.core.Creation;
import com.creatubbles.api.core.User;
import com.creatubbles.api.response.relationships.RelationshipUser;
import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.repack.endercore.common.util.BlockCoord;
import com.google.common.base.Optional;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class PaintingHighlightHandler {

    @SubscribeEvent
    public void renderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.isCanceled() || event.type != RenderGameOverlayEvent.ElementType.CROSSHAIRS || Minecraft.getMinecraft().thePlayer.isSneaking()) {
            return;
        }

        MovingObjectPosition res = Minecraft.getMinecraft().objectMouseOver;
        if (res != null && res.typeOfHit == MovingObjectType.BLOCK) {
            BlockCoord pos = new BlockCoord(res);
            Minecraft mc = Minecraft.getMinecraft();
            Block block = pos.getBlock(mc.theWorld);
            if (block == CTBMod.painting) {
                TilePainting painting = BlockPainting.getDataPainting(Minecraft.getMinecraft().theWorld, pos);
                if (painting == null) {
                    return;
                }

                FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
                
                ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

                int x = resolution.getScaledWidth() / 2;
                int y = resolution.getScaledHeight() / 2;
                
                x += 10;
                y -= fr.FONT_HEIGHT / 2 - 1;
                
                fr.drawStringWithShadow("\"" + EnumChatFormatting.ITALIC + painting.getCreation().getName() + EnumChatFormatting.RESET + "\"", x, y, 0xFFFFFFFF);
                y += fr.FONT_HEIGHT + 2;

                if (painting.getCreation().getRelationships() != null) {
                    RelationshipUser[] creators = painting.getCreation().getRelationships().getCreators();
                    for (RelationshipUser r : creators) {
                        Optional<User> user = CTBMod.cache.getUserForID(r.getId());
                        y = drawUserString(getUserString(user, painting.getCreation()), x, y);
                        y += fr.FONT_HEIGHT;
                    }
                }
            }
        }
    }
    
    private static int drawUserString(String[] data, int x, int y) {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRenderer;
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        fr.drawStringWithShadow(data[0], x, y, -1);
        int ret = y;
        if (data.length > 1) {
            int len0 = fr.getStringWidth(data[0]);
            int len1 = fr.getStringWidth(data[1]);
            fr.drawStringWithShadow(data[1], x + len0, y, -1);
            if (x + len0 + len1 + fr.getStringWidth(data[2]) > sr.getScaledWidth()) {
                y += fr.FONT_HEIGHT;
                ret = y + 5;
                x += 12;
            } else {
                x += len0 + len1;
            }
            fr.drawStringWithShadow(data[2], x, y, -1);
        }
        return ret;
    }
    
    private static String[] getUserString(Optional<User> opt, Creation creation) {
        if (opt.isPresent()) {
            User user = opt.get();
            String gender = user.getGender().equals("male") ? "\u2642" : user.getGender().equals("female") ? "\u2640" : "";
            return new String[] { "By: " + user.getDisplayName() + gender, " " + creation.getCreatedAge(user), " \u2295" + user.getCountryName() };
        } else {
            int dots = (int) ((Minecraft.getMinecraft().theWorld.getTotalWorldTime() / 4) % 4);
            String s = "Loading";
            for (int i = 0; i < dots; i++) {
                s += '.';
            }
            return new String[] { s };
        }
    }
}
