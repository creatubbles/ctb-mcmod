package com.creatubbles.ctbmod.client.render;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import com.creatubbles.api.core.Image;
import com.creatubbles.api.core.Image.ImageType;
import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.common.http.CreationRelations;
import com.creatubbles.ctbmod.common.http.DownloadableImage;
import com.creatubbles.ctbmod.common.painting.BlockPainting;
import com.creatubbles.ctbmod.common.painting.TilePainting;

import jersey.repackaged.com.google.common.collect.Maps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.IPerspectiveAwareModel;

public class RenderPaintingItem extends RenderPainting {
    
    public static class DummyTile extends TilePainting {}
    
    public class BakedModel implements IPerspectiveAwareModel {
        
        private class Overrides extends ItemOverrideList {

            public Overrides() {
                super(Collections.EMPTY_LIST);
            }
            
            @Override
            public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
                RenderPaintingItem.this.stack = stack;
                return BakedModel.this;
            }
        }
        
        @Override
        public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) { return Collections.EMPTY_LIST; }

        @Override
        public boolean isAmbientOcclusion() { return true; }

        @Override
        public boolean isGui3d() { return true; }

        @Override
        public boolean isBuiltInRenderer() { return true; }

        @Override
        public TextureAtlasSprite getParticleTexture() { return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite(); }

        @Override
        public ItemCameraTransforms getItemCameraTransforms() { return ItemCameraTransforms.DEFAULT; }

        @Override
        public ItemOverrideList getOverrides() { return new Overrides(); }
        
        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
            RenderPaintingItem.this.transform = cameraTransformType;
            return Pair.of(this, null); 
        }

    }
    
    public final BakedModel baked = new BakedModel();
    
    private final Map<ItemStack, CreationRelations> stackCache = new WeakHashMap<ItemStack, CreationRelations>();
    private final Map<Image, DownloadableImage> imageCache = Maps.newHashMap();
    
    private ItemStack stack;
    private TransformType transform;
    
    private IBakedModel model;
    
    @Override
    public void renderTileEntityAt(TilePainting te, double x, double y, double z, float partialTicks, int destroyStage) {
        Minecraft.getMinecraft().mcProfiler.startSection("painting_item");
        
        if (model == null) {
            model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getModel(new ModelResourceLocation(CTBMod.DOMAIN + ":painting", "inventory"));
        }
        
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5, 0.5, 0.5);
        if (transform == TransformType.GROUND) {
            GlStateManager.scale(0.5, 0.5, 0.5);
        } else if (transform == TransformType.GUI) {
            GlStateManager.disableLighting();
        }
        IBakedModel bakedModel = ForgeHooksClient.handleCameraTransforms(model, transform, transform == TransformType.FIRST_PERSON_LEFT_HAND);
        Minecraft.getMinecraft().getRenderItem().renderItem(stack, bakedModel);
        
        GlStateManager.translate(-0.5, -0.5, -0.025);
        
        CreationRelations relations = stackCache.get(stack);
        if (relations == null) {
            stackCache.put(stack, relations = BlockPainting.getCreation(stack));
        }
        
        DownloadableImage image = imageCache.get(relations.getImage());
        if (image == null) {
            image = new DownloadableImage(relations.getImage(), relations);
            image.download(ImageType.list_view);
            imageCache.put(relations.getImage(), image);
        }
        
        renderPaintingImage(image, ImageType.list_view, EnumFacing.SOUTH);
        
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        
        Minecraft.getMinecraft().mcProfiler.endSection();
    }
}
