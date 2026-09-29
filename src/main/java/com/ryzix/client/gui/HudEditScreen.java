package com.ryzix.client.gui;

import com.ryzix.client.modules.ChestCounterHUD;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class HudEditScreen extends Screen {
    private final Screen parent;
    private boolean dragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    public HudEditScreen(Screen parent) {
        super(new LiteralText("HUD Editor"));
        this.parent = parent;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        // Dim the background so the HUD stands out
        this.renderBackground(matrices);

        // Instructions
        drawCenteredString(matrices, this.textRenderer, "Drag the HUD to reposition. Press ESC to save.", this.width / 2, 20, 0xFFFFFFFF);

        int hudX = ChestCounterHUD.hudX;
        int hudY = ChestCounterHUD.hudY;

        // Hover outline
        if (mouseX >= hudX - 4 && mouseX <= hudX + 100 && mouseY >= hudY - 3 && mouseY <= hudY + 14) {
            fill(matrices, hudX - 5, hudY - 4, hudX + 101, hudY + 15, 0x55FF2541);
        }

        // Draw the exact same pill from InGameHudMixin so user sees what it looks like
        fill(matrices, hudX - 4, hudY - 3, hudX + 100, hudY + 14, 0xCC0A0A0A);
        fill(matrices, hudX - 4, hudY - 3, hudX - 1, hudY + 14, 0xFFFF2541);
        this.textRenderer.drawWithShadow(matrices, "\u2302 Storages: 99", hudX + 3, hudY, 0xFFFF2541);

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int hudX = ChestCounterHUD.hudX;
            int hudY = ChestCounterHUD.hudY;
            if (mouseX >= hudX - 4 && mouseX <= hudX + 100 && mouseY >= hudY - 3 && mouseY <= hudY + 14) {
                dragging = true;
                dragOffsetX = (int)mouseX - hudX;
                dragOffsetY = (int)mouseY - hudY;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            ChestCounterHUD.hudX = (int)mouseX - dragOffsetX;
            ChestCounterHUD.hudY = (int)mouseY - dragOffsetY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        this.client.openScreen(parent);
    }
}
