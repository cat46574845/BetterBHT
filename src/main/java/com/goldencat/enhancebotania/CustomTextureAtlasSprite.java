package com.goldencat.enhancebotania;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;

public class CustomTextureAtlasSprite extends TextureAtlasSprite {

    protected CustomTextureAtlasSprite(String spriteName) {
        super(spriteName);
    }

    public void setBufferedImage(BufferedImage image) {
        // 設置材質尺寸
        this.width = image.getWidth();
        this.height = image.getHeight();

        // 清除現有的幀數據
        this.clearFramesTextureData();

        // 創建新的材質數據數組
        int frameCount = 1;  // 靜態材質只需要1幀
        int mipmapLevels = Minecraft.getMinecraft().gameSettings.mipmapLevels + 1;

        // 初始化所有mipmap級別的數組
        int[][] imageData = new int[mipmapLevels][];

        // 處理主材質 (mipmap level 0)
        imageData[0] = new int[width * height];

        // 讀取圖片數據
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // getRGB返回的格式是ARGB，需要轉換為ABGR
                int argb = image.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                // 重新組合為ABGR格式
                imageData[0][x + y * width] = (a << 24) | (b << 16) | (g << 8) | r;
            }
        }

        // 生成mipmap
        if (mipmapLevels > 1) {
            for (int level = 1; level < mipmapLevels; level++) {
                int prevWidth = width >> (level - 1);
                int prevHeight = height >> (level - 1);
                int newWidth = width >> level;
                int newHeight = height >> level;

                // 跳過太小的mipmap級別
                if (newWidth < 1 || newHeight < 1) {
                    break;
                }

                int[] prevData = imageData[level - 1];
                int[] newData = new int[newWidth * newHeight];

                // 生成mipmap
                for (int y = 0; y < newHeight; y++) {
                    for (int x = 0; x < newWidth; x++) {
                        int x2 = x * 2;
                        int y2 = y * 2;

                        // 獲取2x2區域的4個像素
                        int p1 = prevData[x2 + y2 * prevWidth];
                        int p2 = prevData[Math.min(x2 + 1, prevWidth - 1) + y2 * prevWidth];
                        int p3 = prevData[x2 + Math.min(y2 + 1, prevHeight - 1) * prevWidth];
                        int p4 = prevData[Math.min(x2 + 1, prevWidth - 1) + Math.min(y2 + 1, prevHeight - 1) * prevWidth];

                        // 分離通道
                        int a = (((p1 >> 24) & 0xFF) + ((p2 >> 24) & 0xFF) + ((p3 >> 24) & 0xFF) + ((p4 >> 24) & 0xFF)) / 4;
                        int b = (((p1 >> 16) & 0xFF) + ((p2 >> 16) & 0xFF) + ((p3 >> 16) & 0xFF) + ((p4 >> 16) & 0xFF)) / 4;
                        int g = (((p1 >> 8) & 0xFF) + ((p2 >> 8) & 0xFF) + ((p3 >> 8) & 0xFF) + ((p4 >> 8) & 0xFF)) / 4;
                        int r = ((p1 & 0xFF) + (p2 & 0xFF) + (p3 & 0xFF) + (p4 & 0xFF)) / 4;

                        // 組合新像素
                        newData[x + y * newWidth] = (a << 24) | (b << 16) | (g << 8) | r;
                    }
                }

                imageData[level] = newData;
            }
        }

        // 設置材質數據
        this.framesTextureData.add(imageData);
    }

    public void clearFramesTextureData() {
        if (this.framesTextureData != null) {
            this.framesTextureData.clear();
        } else {
            this.framesTextureData = Lists.newArrayList();
        }
    }
}