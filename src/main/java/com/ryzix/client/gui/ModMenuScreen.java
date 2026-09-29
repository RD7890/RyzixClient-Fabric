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
import java.util.function.BooleanSupplier;

public class ModMenuScreen extends Screen {

    private static final int ACCENT    = 0xFFFF2541;
    private static final int BG        = 0xFF0A0A0A;
    private static final int PANEL     = 0xFF111111;
    private static final int PANEL_HOV = 0xFF1C1C1C;
    private static final int DIVIDER   = 0xFF1E1E1E;
    private static final int WHITE     = 0xFFFFFFFF;
    private static final int GREY      = 0xFF888888;

    private static final int PANEL_W = 280;
    private static final int PANEL_H = 340;
    private static final int ROW_H   = 58;

    private static class Module {
        final String icon;
        final String name;
        final String desc;
        final Runnable toggle;
        final BooleanSupplier enabled;
        float anim;

        Module(String icon, String name, String desc, Runnable toggle, BooleanSupplier enabled) {
            this.icon = icon; this.name = name; this.desc = desc;
            this.toggle = toggle; this.enabled = enabled;
            this.anim = enabled.getAsBoolean() ? 1f : 0f;
        }
    }

    private final List<Module> modules = new ArrayList<>();
    private int panelX, panelY;
    private int hoveredIdx = -1;

    public ModMenuScreen() {
        super(new LiteralText("RyzixClient"));
        modules.add(new Module("\u2302", "StorageESP",     "Highlight storage containers",    StorageESP::toggle,     StorageESP::isEnabled));
        modules.add(new Module("\u25A3", "PlayerESP",      "See players through walls",        PlayerESP::toggle,      PlayerESP::isEnabled));
        modules.add(new Module("\u2600", "FullBright",     "Maximum visibility in the dark",   FullBright::toggle,     FullBright::isEnabled));
        modules.add(new Module("\u2609", "Chest Counter",  "HUD showing nearby storage count", ChestCounterHUD::toggle,ChestCounterHUD::isEnabled));
    }

    @Override
    protected void init() {
        panelX = (this.width  - PANEL_W) / 2;
        panelY = (this.height - PANEL_H) / 2;
    }

    private void drawText(MatrixStack m, String t, int x, int y, int color) {
        this.textRenderer.draw(m, t, (float) x, (float) y, color);
    }

    private void drawTextCenter(MatrixStack m, String t, int cx, int y, int color) {
        int w = this.textRenderer.getWidth(t);
        this.textRenderer.draw(m, t, (float)(cx - w / 2), (float) y, color);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        // Full-screen dim
        fill(matrices, 0, 0, this.width, this.height, 0xC4000000);

        // Panel
        fill(matrices, panelX, panelY, panelX + PANEL_W, panelY + PANEL_H, BG);

        // Top accent line
        fill(matrices, panelX, panelY, panelX + PANEL_W, panelY + 3, ACCENT);

        // Header
        fill(matrices, panelX, panelY + 3, panelX + PANEL_W, panelY + 48, PANEL);
        // Logo box
        fill(matrices, panelX + 14, panelY + 11, panelX + 36, panelY + 37, ACCENT);
        drawTextCenter(matrices, "R", panelX + 25, panelY + 20, WHITE);
        // Title
        drawText(matrices, "RyzixClient",          panelX + 44, panelY + 15, WHITE);
        drawText(matrices, "v1.0  |  Modules",     panelX + 44, panelY + 27, GREY);
        // Header divider
        fill(matrices, panelX, panelY + 48, panelX + PANEL_W, panelY + 49, DIVIDER);

        // Module rows
        hoveredIdx = -1;
        int rowY = panelY + 49;
        for (int i = 0; i < modules.size(); i++) {
            Module mod = modules.get(i);
            boolean hov = mouseX >= panelX && mouseX <= panelX + PANEL_W
                       && mouseY >= rowY   && mouseY <  rowY + ROW_H;
            if (hov) hoveredIdx = i;

            fill(matrices, panelX, rowY, panelX + PANEL_W, rowY + ROW_H, hov ? PANEL_HOV : BG);

            boolean on = mod.enabled.getAsBoolean();
            // Smooth animation
            float target = on ? 1f : 0f;
            mod.anim += (target - mod.anim) * Math.min(1f, delta * 0.2f);

            // Left accent strip
            if (mod.anim > 0.01f) {
                int sh = (int)(ROW_H * mod.anim);
                fill(matrices, panelX, rowY + (ROW_H - sh), panelX + 3, rowY + ROW_H, ACCENT);
            }

            // Icon box
            fill(matrices, panelX + 14, rowY + 14, panelX + 36, rowY + 36, on ? ACCENT : 0xFF222222);
            drawTextCenter(matrices, mod.icon, panelX + 25, rowY + 21, WHITE);

            // Name & description
            drawText(matrices, mod.name, panelX + 46, rowY + 14, WHITE);
            drawText(matrices, mod.desc, panelX + 46, rowY + 26, GREY);

            // Toggle pill
            int pillX = panelX + PANEL_W - 52;
            int pillY = rowY + 21;
            int pillW = 34;
            int pillH = 14;
            fill(matrices, pillX, pillY, pillX + pillW, pillY + pillH, on ? ACCENT : 0xFF333333);
            int dotX = (int)(pillX + 2 + (pillW - 14) * mod.anim);
            fill(matrices, dotX, pillY + 2, dotX + 10, pillY + pillH - 2, WHITE);

            // Row divider
            fill(matrices, panelX + 14, rowY + ROW_H - 1, panelX + PANEL_W - 14, rowY + ROW_H, DIVIDER);

            rowY += ROW_H;
        }

        // Footer
        fill(matrices, panelX, panelY + PANEL_H - 28, panelX + PANEL_W, panelY + PANEL_H, PANEL);
        drawTextCenter(matrices, "Press R or ESC to close", panelX + PANEL_W / 2, panelY + PANEL_H - 18, GREY);

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 && hoveredIdx >= 0) {
            modules.get(hoveredIdx).toggle.run();
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82 || keyCode == 256) { // R or ESC
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
