package com.goldencat.enhancebotania;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static com.goldencat.enhancebotania.EnhanceBotania.LOGGER;

public class CustomOverrideList extends ItemOverrideList {
    private static final Map<String, Map<Integer, IBakedModel>> MODEL_CACHE = Maps.newHashMap();
    private static final ResourceLocation OVERLAY_TEXTURE = new ResourceLocation("mymod:textures/overlay.png");

    public CustomOverrideList(List<ItemOverride> overridesIn) {
        super(overridesIn);
    }

    @Override
    public IBakedModel handleItemState(
            IBakedModel originalModel,
            ItemStack stack,
            @Nullable World world,
            @Nullable EntityLivingBase entity
    ) {
        if (!stack.hasTagCompound()) {
            return originalModel;
        }
        NBTTagCompound nbt = stack.getTagCompound();
        if (!(nbt.hasKey("blockName") && nbt.hasKey("blockMeta"))) {
            return originalModel;
        }

        String itemName = nbt.getString("blockName");
        int meta = nbt.getInteger("blockMeta");

        // 檢查緩存
        IBakedModel cachedModel = getCachedModel(itemName, meta);
        if (cachedModel != null) {
            return cachedModel;
        }
        return createAndCacheModel(itemName, meta, originalModel);
    }

    private IBakedModel createAndCacheModel(String itemName, int meta, IBakedModel originalModel) {
        try {
            // 創建ItemStack來獲取材質
            Item item = Item.getByNameOrId(itemName);
            if (item == null) {
                return originalModel;
            }

            ItemStack targetStack = new ItemStack(item, 1, meta);

            IBakedModel taegetItemModel = Minecraft.getMinecraft()
                    .getRenderItem()
                    .getItemModelMesher()
                    .getItemModel(targetStack);
            // 獲取物品的TextureAtlasSprite
            TextureAtlasSprite baseSprite = taegetItemModel.getParticleTexture();

            // 獲取overlay材質
            BufferedImage overlayImage;
            try {
                overlayImage = TextureUtil.readBufferedImage(
                        Minecraft.getMinecraft().getResourceManager()
                                .getResource(OVERLAY_TEXTURE)
                                .getInputStream()
                );
            } catch (IOException e) {
                LOGGER.warn("Texture not found: {}. Using transparent texture as fallback.", OVERLAY_TEXTURE, e);
                overlayImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                int pinkColor = new Color(255, 105, 180, 128).getRGB();  // 半透明粉红色
                for (int x = 0; x < overlayImage.getWidth(); x++) {
                    for (int y = 0; y < overlayImage.getHeight(); y++) {
                        overlayImage.setRGB(x, y, pinkColor);
                    }
                }
            }

            // 合併材質
            BufferedImage combinedTexture = combineTextures(baseSprite, overlayImage);

            // 創建新的TextureAtlasSprite
            String spriteName = "generated/" + itemName.replace(':', '_') + "_" + meta;
            CustomTextureAtlasSprite customSprite = new CustomTextureAtlasSprite(spriteName);
            customSprite.setBufferedImage(combinedTexture);

            // 註冊到TextureMap
            Minecraft.getMinecraft().getTextureMapBlocks().setTextureEntry(customSprite);

            // 創建新的BakedModel
            IBakedModel newModel = new CustomBakedModel(taegetItemModel, customSprite);
            // 緩存模型
            MODEL_CACHE.computeIfAbsent(itemName, k -> Maps.newHashMap()).put(meta, newModel);

            return newModel;

        } catch (Exception e) {
            e.printStackTrace();
            return originalModel;
        }
    }

    private BufferedImage combineTextures(TextureAtlasSprite baseSprite, BufferedImage overlayImage) {
        int width = baseSprite.getIconWidth();
        int height = baseSprite.getIconHeight();

        BufferedImage baseImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int[][] frameTextureData = baseSprite.getFrameTextureData(0);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                baseImage.setRGB(x, y, frameTextureData[0][x + y * width]);
            }
        }

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int basePixel = baseImage.getRGB(x, y);
                int overlayPixel = overlayImage.getRGB(
                        x % overlayImage.getWidth(),
                        y % overlayImage.getHeight()
                );

                // Alpha混合
                int resultPixel = blendPixels(basePixel, overlayPixel);
                result.setRGB(x, y, resultPixel);
            }
        }

        return result;
    }

    private int blendPixels(int basePixel, int overlayPixel) {
        int baseAlpha = (basePixel >> 24) & 0xff;
        int baseRed = (basePixel >> 16) & 0xff;
        int baseGreen = (basePixel >> 8) & 0xff;
        int baseBlue = basePixel & 0xff;

        int overlayAlpha = (overlayPixel >> 24) & 0xff;
        int overlayRed = (overlayPixel >> 16) & 0xff;
        int overlayGreen = (overlayPixel >> 8) & 0xff;
        int overlayBlue = overlayPixel & 0xff;

        float overlayFactor = overlayAlpha / 255f;
        float baseFactor = (1f - overlayFactor) * (baseAlpha / 255f);
        float totalAlpha = overlayFactor + baseFactor;

        if (totalAlpha < 0.001f) return 0;

        int finalRed = (int)((overlayRed * overlayFactor + baseRed * baseFactor) / totalAlpha);
        int finalGreen = (int)((overlayGreen * overlayFactor + baseGreen * baseFactor) / totalAlpha);
        int finalBlue = (int)((overlayBlue * overlayFactor + baseBlue * baseFactor) / totalAlpha);
        int finalAlpha = 255;

        return (finalAlpha << 24) | (finalRed << 16) | (finalGreen << 8) | finalBlue;
    }

    private IBakedModel getCachedModel(String itemName, int meta) {
        Map<Integer, IBakedModel> metaMap = MODEL_CACHE.get(itemName);
        if (metaMap != null) {
            return metaMap.get(meta);
        }
        return null;
    }

}