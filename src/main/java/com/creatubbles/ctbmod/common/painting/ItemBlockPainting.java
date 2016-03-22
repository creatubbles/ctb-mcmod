package com.creatubbles.ctbmod.common.painting;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.creatubbles.api.core.Creation;
import com.creatubbles.repack.endercore.common.util.BlockCoord;

public class ItemBlockPainting extends ItemBlock {

    public ItemBlockPainting(Block block) {
        super(block);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List tooltip, boolean advanced) {
        super.addInformation(stack, playerIn, tooltip, advanced);
        Creation c = BlockPainting.getCreation(stack);
        if (c != null) {
            tooltip.add(EnumChatFormatting.ITALIC.toString().concat(c.name));
            tooltip.add(c.user_id);
            tooltip.add(BlockPainting.getWidth(stack) + "x" + BlockPainting.getHeight(stack));
        }
    }

    @Override
    public boolean func_150936_a(World world, int x, int y, int z, int side, EntityPlayer player, ItemStack stack) {
        if (super.func_150936_a(world, x, y, z, side, player, stack)) {
            ForgeDirection facing = ForgeDirection.getOrientation(side);
            ForgeDirection ext = facing.getRotation(ForgeDirection.DOWN);
            BlockCoord pos = new BlockCoord(x, y, z);
            pos = pos.getLocation(facing);
            int width = BlockPainting.getWidth(stack);
            int height = BlockPainting.getHeight(stack);
            for (int x2 = 0; x2 < width; x2++) {
                for (int y2 = 0; y2 < height; y2++) {
                    BlockCoord pos2 = pos.add(x2 * ext.offsetX, y2, x2 * ext.offsetZ);
                    if (!pos2.isAirBlock(world) && !pos2.getBlock(world).isReplaceable(world, pos2.x, pos2.y, pos2.z)) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
}
