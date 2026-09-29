package com.ryzix.client.mixin;

import com.ryzix.client.modules.StorageESP;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

	@Inject(method = "render", at = @At("RETURN"))
	private void onRenderWorld(MatrixStack matrices, float tickDelta, long limitTime,
							   boolean renderBlockOutline, Camera camera,
							   GameRenderer gameRenderer,
							   LightmapTextureManager lightmapTextureManager,
							   Matrix4f matrix4f, CallbackInfo ci) {
		StorageESP.render(matrices, tickDelta);
	}
}
