package com.ryzix.client.mixin;

import com.ryzix.client.modules.ChestCounterHUD;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

	@Inject(method = "render", at = @At("RETURN"))
	private void onRenderHud(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
		if (!ChestCounterHUD.isEnabled()) return;

		net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
		if (mc.player == null || mc.world == null) return;

		int count = ChestCounterHUD.countStorageContainers();

		int x = ChestCounterHUD.hudX;
		int y = ChestCounterHUD.hudY;

		// Background pill
		net.minecraft.client.gui.DrawableHelper.fill(matrices, x - 4, y - 3, x + 100, y + 14, 0xCC0A0A0A);
		// Left accent bar
		net.minecraft.client.gui.DrawableHelper.fill(matrices, x - 4, y - 3, x - 1, y + 14, 0xFFFF2541);
		// Icon + text
		mc.textRenderer.drawWithShadow(matrices, "\u2302 Storages: " + count, x + 3, y, 0xFFFF2541);
	}
}
