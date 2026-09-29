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

    private final Screen parent;

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
    private float scale = 1.0f;

    public ModMenuScreen(Screen parent) {
        super(new LiteralText("RyzixClient"));
        this.parent = parent;
        modules.add(new Module("\u2302", "StorageESP",     "Highlight storage containers",    StorageESP::toggle,     StorageESP::isEnabled));
        modules.add(new Module("\u25A3", "PlayerESP",      "See players through walls",        PlayerESP::toggle,      PlayerESP::isEnabled));
        modules.add(new Module("\u2600", "FullBright",     "Maximum visibility in the dark",   FullBright::toggle,     FullBright::isEnabled));
        modules.add(new Module("\u2609", "Chest Counter",  "HUD showing nearby storage count", ChestCounterHUD::toggle,ChestCounterHUD::isEnabled));
    }

    // Default constructor for in-game keybind
    public ModMenuScreen() {
        this(null);
    }

    @Override
    protected void init() {
        float scaleY = (float) this.height / (PANEL_H + 20);
        float scaleX = (float) this.width / (PANEL_W + 20);
        scale = Math.min(1.0f, Math.min(scaleX, scaleY));

        int scaledW = (int) (this.width / scale);
        int scaledH = (int) (this.height / scale);
        
        panelX = (scaledW - PANEL_W) / 2;
        panelY = (scaledH - PANEL_H) / 2;
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
        fill(matrices, 0, 0, this.width, this.height, 0xC4000000);

        matrices.push();
        matrices.scale(scale, scale, 1.0f);

        int smX = (int) (mouseX / scale);
        int smY = (int) (mouseY / scale);

        fill(matrices, panelX, panelY, panelX + PANEL_W, panelY + PANEL_H, BG);
        fill(matrices, panelX, panelY, panelX + PANEL_W, panelY + 3, ACCENT);
        fill(matrices, panelX, panelY + 3, panelX + PANEL_W, panelY + 48, PANEL);
        
        fill(matrices, panelX + 14, panelY + 11, panelX + 36, panelY + 37, ACCENT);
        drawTextCenter(matrices, "R", panelX + 25, panelY + 20, WHITE);
        
        drawText(matrices, "RyzixClient",          panelX + 44, panelY + 15, WHITE);
        drawText(matrices, "v1.0  |  Modules",     panelX + 44, panelY + 27, GREY);

        // Customize HUD Button
        int custX = panelX + PANEL_W - 85;
        int custY = panelY + 14;
        int custW = 75;
        int custH = 20;
        boolean custHov = smX >= custX && smX <= custX + custW && smY >= custY && smY <= custY + custH;
        fill(matrices, custX, custY, custX + custW, custY + custH, custHov ? ACCENT : 0xFF222222);
        drawTextCenter(matrices, "Customize", custX + custW/2, custY + 6, WHITE);

        fill(matrices, panelX, panelY + 48, panelX + PANEL_W, panelY + 49, DIVIDER);

        hoveredIdx = -1;
        int rowY = panelY + 49;
        for (int i = 0; i < modules.size(); i++) {
            Module mod = modules.get(i);
            boolean hov = smX >= panelX && smX <= panelX + PANEL_W
                       && smY >= rowY   && smY <  rowY + ROW_H;
            if (hov) hoveredIdx = i;

            fill(matrices, panelX, rowY, panelX + PANEL_W, rowY + ROW_H, hov ? PANEL_HOV : BG);

            boolean on = mod.enabled.getAsBoolean();
            float target = on ? 1f : 0f;
            mod.anim += (target - mod.anim) * Math.min(1f, delta * 0.2f);

            if (mod.anim > 0.01f) {
                int sh = (int)(ROW_H * mod.anim);
                fill(matrices, panelX, rowY + (ROW_H - sh), panelX + 3, rowY + ROW_H, ACCENT);
            }

            fill(matrices, panelX + 14, rowY + 14, panelX + 36, rowY + 36, on ? ACCENT : 0xFF222222);
            drawTextCenter(matrices, mod.icon, panelX + 25, rowY + 21, WHITE);

            drawText(matrices, mod.name, panelX + 46, rowY + 14, WHITE);
            drawText(matrices, mod.desc, panelX + 46, rowY + 26, GREY);

            int pillX = panelX + PANEL_W - 52;
            int pillY = rowY + 21;
            int pillW = 34;
            int pillH = 14;
            fill(matrices, pillX, pillY, pillX + pillW, pillY + pillH, on ? ACCENT : 0xFF333333);
            int dotX = (int)(pillX + 2 + (pillW - 14) * mod.anim);
            fill(matrices, dotX, pillY + 2, dotX + 10, pillY + pillH - 2, WHITE);

            fill(matrices, panelX + 14, rowY + ROW_H - 1, panelX + PANEL_W - 14, rowY + ROW_H, DIVIDER);
            rowY += ROW_H;
        }

        fill(matrices, panelX, panelY + PANEL_H - 28, panelX + PANEL_W, panelY + PANEL_H, PANEL);
        drawTextCenter(matrices, "Press R or ESC to close", panelX + PANEL_W / 2, panelY + PANEL_H - 18, GREY);

        matrices.pop();
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int smX = (int)(mx / scale);
        int smY = (int)(my / scale);

        // Customize button click
        int custX = panelX + PANEL_W - 85;
        int custY = panelY + 14;
        int custW = 75;
        int custH = 20;
        if (button == 0 && smX >= custX && smX <= custX + custW && smY >= custY && smY <= custY + custH) {
            this.client.openScreen(new HudEditScreen(this));
            return true;
        }

        if (button == 0 && hoveredIdx >= 0) {
            modules.get(hoveredIdx).toggle.run();
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 82 || keyCode == 256) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public void onClose() {
        if (this.client != null) {
            this.client.openScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
