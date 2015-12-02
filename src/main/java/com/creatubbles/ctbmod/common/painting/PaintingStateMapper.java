package com.creatubbles.ctbmod.common.painting;

import static com.creatubbles.ctbmod.common.painting.BlockPainting.CONNECTION;
import static com.creatubbles.ctbmod.common.painting.BlockPainting.DUMMY;
import static com.creatubbles.ctbmod.common.painting.BlockPainting.FACING;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import com.creatubbles.ctbmod.CTBMod;
import com.google.common.collect.Maps;

public class PaintingStateMapper implements IStateMapper {

    @Override
    public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block block) {
        Map<IBlockState, ModelResourceLocation> map = Maps.newHashMap();
        IBlockState state;
        for (ConnectionType type : ConnectionType.values()) {
            state = block.getDefaultState().withProperty(CONNECTION, type);
            for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                state = state.withProperty(DUMMY, false).withProperty(FACING, facing);
                add(map, state, facing, type);
                state = state.withProperty(DUMMY, true);
                add(map, state, facing, type);
            }
        }
        return map;
    }

    private void add(Map<IBlockState, ModelResourceLocation> map, IBlockState state, EnumFacing facing, ConnectionType name) {
        map.put(state, new ModelResourceLocation(new ResourceLocation(CTBMod.DOMAIN, "painting"), facing.getName() + "/" + name.getName()));
    }
}
