package com.ryzix.client.mixin;

import com.ryzix.client.modules.Freecam;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {

	@Shadow private Vec3d pos;
	@Shadow private float yaw;
	@Shadow private float pitch;

	@Inject(method = "update", at = @At("RETURN"))
	private void onCameraUpdate(BlockView area, Entity focusedEntity,
								boolean thirdPerson, boolean inverseView,
								float tickDelta, CallbackInfo ci) {
		if (Freecam.isEnabled()) {
			this.pos = new Vec3d(Freecam.getCamX(), Freecam.getCamY(), Freecam.getCamZ());
			this.yaw = Freecam.getCamYaw();
			this.pitch = Freecam.getCamPitch();
		}
	}
}
