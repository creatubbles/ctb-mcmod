package com.creatubbles.ctbmod.common.painting;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import com.creatubbles.ctbmod.common.http.CreationRelations;

public class ItemBlockPainting extends ItemBlock {

    public ItemBlockPainting(Block block) {
        super(block);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List tooltip, boolean advanced) {
        super.addInformation(stack, playerIn, tooltip, advanced);
        CreationRelations c = BlockPainting.getCreation(stack);
        if (c != null) {
            tooltip.add(TextFormatting.ITALIC.toString().concat(c.getName()));
            if (c.getRelationships() != null) {
                tooltip.add(c.getRelationships().getUser().getId());
            }
            tooltip.add(BlockPainting.getWidth(stack) + "x" + BlockPainting.getHeight(stack));
        }
    }

    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
        if (super.canPlaceBlockOnSide(worldIn, pos, side, player, stack)) {
            EnumFacing ext = side.rotateYCCW();
            pos = pos.offset(side);
            int width = BlockPainting.getWidth(stack);
            int height = BlockPainting.getHeight(stack);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    BlockPos pos2 = pos.add(x * ext.getFrontOffsetX(), y, x * ext.getFrontOffsetZ());
                    if (!worldIn.isAirBlock(pos2) && !worldIn.getBlockState(pos2).getBlock().isReplaceable(worldIn, pos2)) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
}
