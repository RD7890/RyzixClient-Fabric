package com.ryzix.client.mixin;

import com.ryzix.client.gui.ModMenuScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({TitleScreen.class, GameMenuScreen.class})
public abstract class MenuScreensMixin extends Screen {

    protected MenuScreensMixin() {
        super(null);
    }

    @Inject(at = @At("RETURN"), method = "init()V")
    private void onInitAddRyzixButton(CallbackInfo ci) {
        // Add button in the top left corner to avoid overlapping with default buttons
        this.addButton(new ButtonWidget(5, 5, 100, 20, new LiteralText("Ryzix Menu"), b -> {
            MinecraftClient.getInstance().openScreen(new ModMenuScreen(this));
        }));
    }
}
