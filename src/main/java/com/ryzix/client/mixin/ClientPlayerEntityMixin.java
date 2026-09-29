package com.ryzix.client.mixin;

import com.ryzix.client.modules.Freecam;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

	@Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
	private void onSendMovementPackets(CallbackInfo ci) {
		if (Freecam.isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void onTickHead(CallbackInfo ci) {
		if (Freecam.isEnabled()) {
			((ClientPlayerEntity)(Object)this).noClip = true;
		}
	}

	@Inject(method = "tick", at = @At("RETURN"))
	private void onTickReturn(CallbackInfo ci) {
		if (Freecam.isEnabled()) {
			ClientPlayerEntity self = (ClientPlayerEntity)(Object)this;
			self.noClip = false;
			self.setVelocity(0, 0, 0);
		}
	}
}
