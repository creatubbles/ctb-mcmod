package com.creatubbles.ctbmod.common.painting;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.creatubbles.api.core.Creation;
import com.creatubbles.api.core.User;
import com.creatubbles.ctbmod.CTBMod;
import com.google.common.base.Optional;

public class PaintingHighlightHandler {

    @SubscribeEvent
    public void renderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.isCanceled() || event.getType() != RenderGameOverlayEvent.ElementType.POTION_ICONS || Minecraft.getMinecraft().thePlayer.isSneaking()) {
            return;
        }

        RayTraceResult res = Minecraft.getMinecraft().objectMouseOver;
        if (res != null && res.typeOfHit == Type.BLOCK) {
            IBlockState state = Minecraft.getMinecraft().theWorld.getBlockState(res.getBlockPos());
            if (state.getBlock() == CTBMod.painting) {
                TilePainting painting = BlockPainting.getDataPainting(Minecraft.getMinecraft().theWorld, res.getBlockPos());
                if (painting == null) {
                    return;
                }

                FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
                
                ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());

                int x = resolution.getScaledWidth() / 2;
                int y = resolution.getScaledHeight() / 2;
                
                x += 10;
                y -= fr.FONT_HEIGHT / 2 - 1;
                
                fr.drawStringWithShadow("\"" + TextFormatting.ITALIC + painting.getCreation().name + TextFormatting.RESET + "\"", x, y, 0xFFFFFFFF);
                y += fr.FONT_HEIGHT + 2;

                String[] creators = painting.getCreation().creator_ids;
                if (creators.length == 1) {
                    Optional<User> user = CTBMod.cache.getUserForID(creators[0]);
                    fr.drawStringWithShadow("By: " + getUserString(user, painting.getCreation()), x, y, 0xFFFFFFFF);
                } else if (creators.length > 1) {
                    fr.drawStringWithShadow("By:", x, y, 0xFFFFFFFF);
                    for (String s : creators) {
                        y += fr.FONT_HEIGHT + 2;
                        Optional<User> user = CTBMod.cache.getUserForID(s);
                        fr.drawStringWithShadow("- " + getUserString(user, painting.getCreation()), x, y, 0xFFFFFFFF);
                    }
                }
            }
        }
    }
    
    private static String getUserString(Optional<User> opt, Creation creation) {
        if (opt.isPresent()) {
            User user = opt.get();
            String gender = user.gender.equals("male") ? "\u2642" : user.gender.equals("female") ? "\u2640" : "";
            return user.display_name + gender + " \u2295" + user.country_name + " " + creation.created_at_age_per_creator.get(user.id);
        } else {
            int dots = (int) ((Minecraft.getMinecraft().theWorld.getTotalWorldTime() / 4) % 4);
            String s = "Loading";
            for (int i = 0; i < dots; i++) {
                s += '.';
            }
            return s;
        }
    }
}
