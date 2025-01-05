package com.goldencat.enhancebotania.mixin;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import static com.goldencat.enhancebotania.EnhanceBotania.LOGGER;

@Mixin(value = vazkii.botania.common.item.ItemBlackHoleTalisman.class)
public class testMixin  {
    @Inject(
            method = "setBlock(Lnet/minecraft/item/ItemStack;Lnet/minecraft/block/Block;I)Z",
            at = @At("HEAD"),
            remap = false
    )
    private void onSetBlock(ItemStack stack, Block block, int meta, CallbackInfoReturnable<Boolean> cir) {
        LOGGER.info("Setting block: " + block.getRegistryName());
    }
}
