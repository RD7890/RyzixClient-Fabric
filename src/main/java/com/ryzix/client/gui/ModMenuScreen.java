package com.ryzix.client.gui;

import com.ryzix.client.modules.ChestCounterHUD;
import com.ryzix.client.modules.FullBright;
import com.ryzix.client.modules.PlayerESP;
import com.ryzix.client.modules.StorageESP;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

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

    private int panelW = 280;
    private int panelH = 340;
    private int rowH   = 58;

    private final Screen parent;
    private final boolean playIntro;
    private long openTime;
    private static final Identifier LOGO = new Identifier("ryzixclient", "textures/gui/logo.png");

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

    public ModMenuScreen(Screen parent, boolean playIntro) {
        super(new LiteralText("RyzixClient"));
        this.parent = parent;
        this.playIntro = playIntro;
        modules.add(new Module("\u2302", "StorageESP",     "Highlight storage containers",    StorageESP::toggle,     StorageESP::isEnabled));
        modules.add(new Module("\u25A3", "PlayerESP",      "See players through walls",        PlayerESP::toggle,      PlayerESP::isEnabled));
        modules.add(new Module("\u2600", "FullBright",     "Maximum visibility in the dark",   FullBright::toggle,     FullBright::isEnabled));
        modules.add(new Module("\u2609", "Chest Counter",  "HUD showing nearby storage count", ChestCounterHUD::toggle,ChestCounterHUD::isEnabled));
    }

    public ModMenuScreen() {
        this(null, false);
    }

    @Override
    protected void init() {
        openTime = Util.getMeasuringTimeMs();
        
        // Native Dynamic Layout (No Scaling)
        int maxH = this.height - 20; 
        if (340 > maxH) {
            panelH = maxH;
            int spaceForRows = panelH - 48 - 28; // Header is 48, footer is 28
            rowH = spaceForRows / modules.size();
        } else {
            panelH = 340;
            rowH = 58;
        }

        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;
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

        fill(matrices, panelX, panelY, panelX + panelW, panelY + panelH, BG);
        fill(matrices, panelX, panelY, panelX + panelW, panelY + 3, ACCENT);
        fill(matrices, panelX, panelY + 3, panelX + panelW, panelY + 48, PANEL);
        
        fill(matrices, panelX + 14, panelY + 11, panelX + 36, panelY + 37, ACCENT);
        drawTextCenter(matrices, "R", panelX + 25, panelY + 20, WHITE);
        
        drawText(matrices, "RyzixClient",          panelX + 44, panelY + 15, WHITE);
        drawText(matrices, "v1.0  |  Modules",     panelX + 44, panelY + 27, GREY);

        int custX = panelX + panelW - 85;
        int custY = panelY + 14;
        int custW = 75;
        int custH = 20;
        boolean custHov = mouseX >= custX && mouseX <= custX + custW && mouseY >= custY && mouseY <= custY + custH;
        fill(matrices, custX, custY, custX + custW, custY + custH, custHov ? ACCENT : 0xFF222222);
        drawTextCenter(matrices, "Customize", custX + custW/2, custY + 6, WHITE);

        fill(matrices, panelX, panelY + 48, panelX + panelW, panelY + 49, DIVIDER);

        hoveredIdx = -1;
        int rowY = panelY + 49;
        for (int i = 0; i < modules.size(); i++) {
            Module mod = modules.get(i);
            boolean hov = mouseX >= panelX && mouseX <= panelX + panelW && mouseY >= rowY && mouseY < rowY + rowH;
            if (hov) hoveredIdx = i;

            fill(matrices, panelX, rowY, panelX + panelW, rowY + rowH, hov ? PANEL_HOV : BG);

            boolean on = mod.enabled.getAsBoolean();
            float target = on ? 1f : 0f;
            mod.anim += (target - mod.anim) * Math.min(1f, delta * 0.2f);

            if (mod.anim > 0.01f) {
                int sh = (int)(rowH * mod.anim);
                fill(matrices, panelX, rowY + (rowH - sh), panelX + 3, rowY + rowH, ACCENT);
            }

            int centerY = rowY + (rowH / 2);

            fill(matrices, panelX + 14, centerY - 11, panelX + 36, centerY + 11, on ? ACCENT : 0xFF222222);
            drawTextCenter(matrices, mod.icon, panelX + 25, centerY - 4, WHITE);

            drawText(matrices, mod.name, panelX + 46, centerY - 11, WHITE);
            drawText(matrices, mod.desc, panelX + 46, centerY + 2, GREY);

            int pillX = panelX + panelW - 52;
            int pillY = centerY - 7;
            int pillW = 34;
            int pillH = 14;
            fill(matrices, pillX, pillY, pillX + pillW, pillY + pillH, on ? ACCENT : 0xFF333333);
            int dotX = (int)(pillX + 2 + (pillW - 14) * mod.anim);
            fill(matrices, dotX, pillY + 2, dotX + 10, pillY + pillH - 2, WHITE);

            fill(matrices, panelX + 14, rowY + rowH - 1, panelX + panelW - 14, rowY + rowH, DIVIDER);
            rowY += rowH;
        }

        fill(matrices, panelX, panelY + panelH - 28, panelX + panelW, panelY + panelH, PANEL);
        drawTextCenter(matrices, "Press R or ESC to close", panelX + panelW / 2, panelY + panelH - 18, GREY);

        if (playIntro) {
            long elapsed = Util.getMeasuringTimeMs() - openTime;
            if (elapsed < 1200) {
                float alpha = 1.0f;
                if (elapsed > 800) {
                    alpha = 1.0f - ((elapsed - 800) / 400.0f);
                }

                int bgAlpha = (int)(alpha * 255);
                fill(matrices, 0, 0, this.width, this.height, (bgAlpha << 24) | 0x050505);

                com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                com.mojang.blaze3d.systems.RenderSystem.color4f(1.0f, 1.0f, 1.0f, alpha);
                
                if (this.client != null) {
                    this.client.getTextureManager().bindTexture(LOGO);
                    int logoSize = 64; // Scaled down based on feedback
                    int logoX = (this.width - logoSize) / 2;
                    int logoY = (this.height - logoSize) / 2 - 20;
                    drawTexture(matrices, logoX, logoY, 0.0F, 0.0F, logoSize, logoSize, logoSize, logoSize);

                    int barW = 140;
                    int barH = 3;
                    int barX = (this.width - barW) / 2;
                    int barY = logoY + logoSize + 25;

                    float progress = Math.min(1.0f, elapsed / 800.0f);

                    fill(matrices, barX, barY, barX + barW, barY + barH, (bgAlpha << 24) | 0x222222);
                    fill(matrices, barX, barY, barX + (int)(barW * progress), barY + barH, (bgAlpha << 24) | 0xFF2541);
                }

                com.mojang.blaze3d.systems.RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                com.mojang.blaze3d.systems.RenderSystem.disableBlend();
            }
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (playIntro && Util.getMeasuringTimeMs() - openTime < 1200) return false;

        int custX = panelX + panelW - 85;
        int custY = panelY + 14;
        int custW = 75;
        int custH = 20;
        if (button == 0 && mx >= custX && mx <= custX + custW && my >= custY && my <= custY + custH) {
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
        if (playIntro && Util.getMeasuringTimeMs() - openTime < 1200) return false;

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
