package com.ryzix.client.gui;

import com.ryzix.client.modules.ChestCounterHUD;
import com.ryzix.client.modules.FullBright;
import com.ryzix.client.modules.PlayerESP;
import com.ryzix.client.modules.StorageESP;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;
import java.util.List;

public class ModMenuScreen extends Screen {

    // Brand color #ff2541
    private static final int ACCENT = 0xFFFF2541;
    private static final int ACCENT_DIM = 0xAAFF2541;
    private static final int BG = 0xFF0A0A0A;
    private static final int PANEL = 0xFF111111;
    private static final int PANEL_HOVER = 0xFF1A1A1A;
    private static final int TEXT_WHITE = 0xFFFFFFFF;
    private static final int TEXT_GREY = 0xFF888888;
    private static final int DIVIDER = 0xFF1E1E1E;

    // Panel dimensions
    private static final int PANEL_W = 280;
    private static final int PANEL_H = 340;

    // Module data
    private static class Module {
        String icon;   // Unicode icon char (Font Awesome codepoints via unicode)
        String name;
        String desc;
        Runnable toggle;
        java.util.function.BooleanSupplier enabled;
        float animProgress = 0f; // 0.0 = off, 1.0 = on

        Module(String icon, String name, String desc, Runnable toggle, java.util.function.BooleanSupplier enabled) {
            this.icon = icon;
            this.name = name;
            this.desc = desc;
            this.toggle = toggle;
            this.enabled = enabled;
            this.animProgress = enabled.getAsBoolean() ? 1f : 0f;
        }
    }

    private final List<Module> modules = new ArrayList<>();
    private int panelX, panelY;
    private int hoveredIndex = -1;

    // For draggable HUD position feature
    private boolean draggingHUD = false;
    private int dragOffX, dragOffY;

    public ModMenuScreen() {
        super(new LiteralText("RyzixClient"));

        modules.add(new Module("\u2302", "StorageESP", "Highlight storage containers",
                StorageESP::toggle, StorageESP::isEnabled));
        modules.add(new Module("\u25A9", "PlayerESP", "See players through walls",
                PlayerESP::toggle, PlayerESP::isEnabled));
        modules.add(new Module("\u2600", "FullBright", "Maximum visibility in the dark",
                FullBright::toggle, FullBright::isEnabled));
        modules.add(new Module("\u2609", "Chest Counter", "HUD showing nearby storage count",
                ChestCounterHUD::toggle, ChestCounterHUD::isEnabled));
    }

    @Override
    protected void init() {
        panelX = (this.width - PANEL_W) / 2;
        panelY = (this.height - PANEL_H) / 2;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        // Dim world behind menu
        fillGradient(matrices, 0, 0, this.width, this.height, 0xC0000000, 0xC0000000);

        // Panel background
        fill(matrices, panelX, panelY, panelX + PANEL_W, panelY + PANEL_H, BG);

        // Top accent bar
        fill(matrices, panelX, panelY, panelX + PANEL_W, panelY + 3, ACCENT);

        // Header section
        fill(matrices, panelX, panelY + 3, panelX + PANEL_W, panelY + 48, PANEL);

        // Logo text "R" in accent color box
        fill(matrices, panelX + 16, panelY + 11, panelX + 36, panelY + 37, ACCENT);
        drawCenteredString(matrices, this.textRenderer, "R", panelX + 26, panelY + 20, TEXT_WHITE);

        // Mod name
        drawString(matrices, this.textRenderer, "RyzixClient", panelX + 44, panelY + 16, TEXT_WHITE);
        drawString(matrices, this.textRenderer, "v1.0  |  Modules", panelX + 44, panelY + 28, TEXT_GREY);

        // Divider
        fill(matrices, panelX, panelY + 48, panelX + PANEL_W, panelY + 49, DIVIDER);

        // Module rows
        hoveredIndex = -1;
        int rowY = panelY + 49;
        int rowH = 58;

        for (int i = 0; i < modules.size(); i++) {
            Module m = modules.get(i);
            int rowX = panelX;
            int rowBottom = rowY + rowH;

            boolean hovered = mouseX >= rowX && mouseX <= rowX + PANEL_W
                    && mouseY >= rowY && mouseY <= rowBottom;
            if (hovered) hoveredIndex = i;

            // Row bg
            fill(matrices, rowX, rowY, rowX + PANEL_W, rowBottom, hovered ? PANEL_HOVER : BG);

            // Left accent strip if enabled
            boolean on = m.enabled.getAsBoolean();

            // Animate toggle
            if (on && m.animProgress < 1f) m.animProgress = Math.min(1f, m.animProgress + delta * 0.15f);
            if (!on && m.animProgress > 0f) m.animProgress = Math.max(0f, m.animProgress - delta * 0.15f);

            if (m.animProgress > 0f) {
                int stripH = (int)(rowH * m.animProgress);
                fill(matrices, rowX, rowY + (rowH - stripH), rowX + 3, rowY + rowH, ACCENT);
            }

            // Icon box
            int iconBoxColor = on ? ACCENT : 0xFF222222;
            fill(matrices, rowX + 14, rowY + 14, rowX + 36, rowY + 36, iconBoxColor);
            drawCenteredString(matrices, this.textRenderer, m.icon, rowX + 25, rowY + 21, TEXT_WHITE);

            // Name
            drawString(matrices, this.textRenderer, m.name, rowX + 46, rowY + 14, TEXT_WHITE);

            // Description
            drawString(matrices, this.textRenderer, m.desc, rowX + 46, rowY + 27, TEXT_GREY);

            // Toggle pill
            int pillX = rowX + PANEL_W - 52;
            int pillY2 = rowY + 22;
            int pillW = 34;
            int pillH = 14;
            int pillColor = on ? ACCENT : 0xFF333333;
            fill(matrices, pillX, pillY2, pillX + pillW, pillY2 + pillH, pillColor);

            // Pill dot position (animated)
            int dotX = (int)(pillX + 2 + (pillW - 14) * m.animProgress);
            fill(matrices, dotX, pillY2 + 2, dotX + 10, pillY2 + pillH - 2, TEXT_WHITE);

            // Status text
            String status = on ? "ON" : "OFF";
            drawCenteredString(matrices, this.textRenderer, status, pillX + pillW / 2, pillY2 + pillH + 4, on ? ACCENT : TEXT_GREY);

            // Bottom divider
            fill(matrices, rowX + 14, rowBottom - 1, rowX + PANEL_W - 14, rowBottom, DIVIDER);

            rowY += rowH;
        }

        // Footer
        fill(matrices, panelX, panelY + PANEL_H - 28, panelX + PANEL_W, panelY + PANEL_H, PANEL);
        drawCenteredString(matrices, this.textRenderer, "Press \u00A7cR\u00A7r or \u00A77ESC\u00A7r to close",
                panelX + PANEL_W / 2, panelY + PANEL_H - 18, TEXT_GREY);

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hoveredIndex >= 0) {
            modules.get(hoveredIndex).toggle.run();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Close on R or ESC
        if (keyCode == 82 || keyCode == 256) { // 82 = R, 256 = ESC
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false; // Don't pause the game
    }
}
