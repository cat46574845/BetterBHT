package com.goldencat.enhancebotania;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.model.BakedModelWrapper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;

class CustomBakedModel extends BakedModelWrapper {
    private final TextureAtlasSprite particle;
    private final ItemOverrideList overrideList;

    public CustomBakedModel(IBakedModel originalModel, TextureAtlasSprite particle) {
        super(originalModel);
        this.particle = particle;
        this.overrideList = new CustomOverrideList(originalModel.getOverrides().getOverrides());
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.particle;
    }
    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return this.overrideList;
    }

    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        return net.minecraftforge.client.ForgeHooksClient.handlePerspective(this, cameraTransformType);
    }
}
