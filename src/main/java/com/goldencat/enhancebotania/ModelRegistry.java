package com.goldencat.enhancebotania;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import com.goldencat.enhance_botania.Tags;
import static com.goldencat.enhancebotania.EnhanceBotania.LOGGER;
import static vazkii.botania.common.lib.LibMisc.MOD_ID;


@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public class ModelRegistry {

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        LOGGER.info("onModelBake");
        ModelResourceLocation location = new ModelResourceLocation(
            new ResourceLocation(MOD_ID, "blackHoleTalisman1"), "inventory"
        );

        IBakedModel originalModel = event.getModelRegistry().getObject(location);

        if (originalModel != null) {
            CustomBakedModel customModel = new CustomBakedModel(
                originalModel,
                originalModel.getParticleTexture()
            );
            event.getModelRegistry().putObject(location, customModel);
        }
    }
}