package com.ryzix.client.gui;

import com.ryzix.client.modules.ChestCounterHUD;
import com.ryzix.client.modules.FullBright;
import com.ryzix.client.modules.PlayerESP;
import com.ryzix.client.modules.StorageESP;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Matrix4f;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class ModMenuScreen extends Screen {

    private static final int ACCENT    = 0xFFFD1523; // New Primary Color #FD1523
    private static final int BG        = 0x75050505; // Glassmorphism Dark background
    private static final int PANEL     = 0x40FFFFFF; // Glassmorphism subtle overlay
    private static final int PANEL_HOV = 0x60FFFFFF; 
    private static final int DIVIDER   = 0x30FFFFFF;
    private static final int WHITE     = 0xFFFFFFFF;
    private static final int GREY      = 0xFFBBBBBB;

    private int panelW = 280;
    private int panelH = 340;
    private int rowH   = 58;

    private final Screen parent;
    
    // Animation States
    private long openTime;
    private long closeTime = 0;
    private boolean isClosing = false;

    // Assets
    private static final Identifier LOGO = new Identifier("ryzixclient", "textures/gui/logo.png");

    private static class Module {
        final Identifier icon;
        final String name;
        final String desc;
        final Runnable toggle;
        final BooleanSupplier enabled;
        float anim;

        Module(Identifier icon, String name, String desc, Runnable toggle, BooleanSupplier enabled) {
            this.icon = icon; this.name = name; this.desc = desc;
            this.toggle = toggle; this.enabled = enabled;
            this.anim = enabled.getAsBoolean() ? 1f : 0f;
        }
    }

    private final List<Module> modules = new ArrayList<>();
    private int panelX, panelY;
    private int hoveredIdx = -1;

    public ModMenuScreen(Screen parent, boolean playIntro) {
        // We always play intro now based on user request, but keeping constructor signature to not break init
        super(new LiteralText("RyzixClient"));
        this.parent = parent;
        modules.add(new Module(new Identifier("ryzixclient", "textures/gui/icons/storage.png"), "StorageESP",     "Highlight storage containers",    StorageESP::toggle,     StorageESP::isEnabled));
        modules.add(new Module(new Identifier("ryzixclient", "textures/gui/icons/player.png"), "PlayerESP",      "See players through walls",        PlayerESP::toggle,      PlayerESP::isEnabled));
        modules.add(new Module(new Identifier("ryzixclient", "textures/gui/icons/sun.png"), "FullBright",     "Maximum visibility in the dark",   FullBright::toggle,     FullBright::isEnabled));
        modules.add(new Module(new Identifier("ryzixclient", "textures/gui/icons/chest.png"), "Chest Counter",  "HUD showing nearby storage count", ChestCounterHUD::toggle,ChestCounterHUD::isEnabled));
    }

    public ModMenuScreen() {
        this(null, true);
    }

    @Override
    protected void init() {
        openTime = Util.getMeasuringTimeMs();
        
        int maxH = this.height - 20; 
        if (340 > maxH) {
            panelH = maxH;
            int spaceForRows = panelH - 48 - 28; 
            rowH = spaceForRows / modules.size();
        } else {
            panelH = 340;
            rowH = 58;
        }

        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;
    }

    private void drawText(MatrixStack m, String t, int x, int y, int color) {
        this.textRenderer.drawWithShadow(m, t, (float) x, (float) y, color);
    }

    private void drawTextCenter(MatrixStack m, String t, int cx, int y, int color) {
        int w = this.textRenderer.getWidth(t);
        this.textRenderer.drawWithShadow(m, t, (float)(cx - w / 2), (float) y, color);
    }

    // Helper for Rounded Rectangles (Glassmorphism effect)
    public static void fillRounded(MatrixStack matrices, int x, int y, int x2, int y2, int r, int color) {
        if (r > (x2 - x) / 2) r = (x2 - x) / 2;
        if (r > (y2 - y) / 2) r = (y2 - y) / 2;

        float a = (float)(color >> 24 & 255) / 255.0F;
        float red = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Matrix4f matrix = matrices.peek().getModel();

        buffer.begin(7, VertexFormats.POSITION_COLOR);
        // Center
        buffer.vertex(matrix, x + r, y, 0).color(red, g, b, a).next();
        buffer.vertex(matrix, x + r, y2, 0).color(red, g, b, a).next();
        buffer.vertex(matrix, x2 - r, y2, 0).color(red, g, b, a).next();
        buffer.vertex(matrix, x2 - r, y, 0).color(red, g, b, a).next();
        // Left
        buffer.vertex(matrix, x, y + r, 0).color(red, g, b, a).next();
        buffer.vertex(matrix, x, y2 - r, 0).color(red, g, b, a).next();
        buffer.vertex(matrix, x + r, y2 - r, 0).color(red, g, b, a).next();
        buffer.vertex(matrix, x + r, y + r, 0).color(red, g, b, a).next();
        // Right
        buffer.vertex(matrix, x2 - r, y + r, 0).color(red, g, b, a).next();
        buffer.vertex(matrix, x2 - r, y2 - r, 0).color(red, g, b, a).next();
        buffer.vertex(matrix, x2, y2 - r, 0).color(red, g, b, a).next();
        buffer.vertex(matrix, x2, y + r, 0).color(red, g, b, a).next();
        tessellator.draw();

        buffer.begin(6, VertexFormats.POSITION_COLOR);
        drawCorner(buffer, matrix, x + r, y + r, r, 180, 270, red, g, b, a);
        tessellator.draw();

        buffer.begin(6, VertexFormats.POSITION_COLOR);
        drawCorner(buffer, matrix, x2 - r, y + r, r, 270, 360, red, g, b, a);
        tessellator.draw();

        buffer.begin(6, VertexFormats.POSITION_COLOR);
        drawCorner(buffer, matrix, x2 - r, y2 - r, r, 0, 90, red, g, b, a);
        tessellator.draw();

        buffer.begin(6, VertexFormats.POSITION_COLOR);
        drawCorner(buffer, matrix, x + r, y2 - r, r, 90, 180, red, g, b, a);
        tessellator.draw();

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private static void drawCorner(BufferBuilder buffer, Matrix4f matrix, int cx, int cy, int r, int startAngle, int endAngle, float red, float g, float b, float a) {
        buffer.vertex(matrix, cx, cy, 0).color(red, g, b, a).next();
        for (int i = startAngle; i <= endAngle; i += 10) {
            float rad = (float) Math.toRadians(i);
            float px = (float) (cx + Math.cos(rad) * r);
            float py = (float) (cy + Math.sin(rad) * r);
            buffer.vertex(matrix, px, py, 0).color(red, g, b, a).next();
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        long elapsedOpen = Util.getMeasuringTimeMs() - openTime;
        
        // OPEN INTRO ANIMATION (Logo + Loading Bar fading out)
        // Runs for first 800ms
        if (elapsedOpen < 800 && !isClosing) {
            float alpha = 1.0f;
            if (elapsedOpen > 500) {
                alpha = 1.0f - ((elapsedOpen - 500) / 300.0f);
            }

            int bgAlpha = (int)(alpha * 255);
            fill(matrices, 0, 0, this.width, this.height, (bgAlpha << 24) | 0x050505);

            RenderSystem.enableBlend();
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, alpha);
            
            if (this.client != null) {
                this.client.getTextureManager().bindTexture(LOGO);
                int logoSize = 64;
                int logoX = (this.width - logoSize) / 2;
                int logoY = (this.height - logoSize) / 2 - 20;
                drawTexture(matrices, logoX, logoY, 0.0F, 0.0F, logoSize, logoSize, logoSize, logoSize);

                int barW = 140;
                int barH = 3;
                int barX = (this.width - barW) / 2;
                int barY = logoY + logoSize + 25;

                float progress = Math.min(1.0f, elapsedOpen / 500.0f);

                fill(matrices, barX, barY, barX + barW, barY + barH, (bgAlpha << 24) | 0x222222);
                fill(matrices, barX, barY, barX + (int)(barW * progress), barY + barH, (bgAlpha << 24) | 0xFFFD1523);
            }

            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
            return; // Skip rendering menu until intro finishes
        }

        // CLOSE ANIMATION
        matrices.push();
        float alphaMult = 1.0f;
        if (isClosing) {
            long elapsedClose = Util.getMeasuringTimeMs() - closeTime;
            if (elapsedClose > 250) {
                // Actually close the screen
                if (this.client != null) {
                    this.client.openScreen(parent);
                }
                matrices.pop();
                return;
            }
            float progress = elapsedClose / 250.0f;
            // Slide down and scale down
            float scale = 1.0f - (progress * 0.15f);
            matrices.translate(this.width / 2f, this.height / 2f, 0);
            matrices.scale(scale, scale, 1.0f);
            matrices.translate(-this.width / 2f, -this.height / 2f, 0);
            matrices.translate(0, progress * 100, 0);
            alphaMult = 1.0f - progress;
        }

        // Apply alpha multiplier for fading (though fillRounded doesn't natively support multiplying external alpha without modifying it, we'll keep it simple by just sliding)
        
        // Full screen slight dim for focus
        fill(matrices, 0, 0, this.width, this.height, ((int)(0x50 * alphaMult) << 24) | 0x000000);

        // Glassmorphism Main Panel
        fillRounded(matrices, panelX, panelY, panelX + panelW, panelY + panelH, 12, BG);
        
        // Header
        fillRounded(matrices, panelX, panelY, panelX + panelW, panelY + 48, 12, PANEL);
        // Re-draw flat bottom to connect to rows
        fill(matrices, panelX, panelY + 36, panelX + panelW, panelY + 48, PANEL);

        // Header Logo (using Image instead of "R" Text)
        RenderSystem.enableBlend();
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        if (this.client != null) {
            this.client.getTextureManager().bindTexture(LOGO);
            drawTexture(matrices, panelX + 12, panelY + 12, 0.0F, 0.0F, 24, 24, 24, 24);
        }
        RenderSystem.disableBlend();
        
        drawText(matrices, "RyzixClient",          panelX + 44, panelY + 15, WHITE);
        drawText(matrices, "v1.0  |  Modules",     panelX + 44, panelY + 27, GREY);

        int custX = panelX + panelW - 85;
        int custY = panelY + 14;
        int custW = 75;
        int custH = 20;
        boolean custHov = mouseX >= custX && mouseX <= custX + custW && mouseY >= custY && mouseY <= custY + custH;
        fillRounded(matrices, custX, custY, custX + custW, custY + custH, 4, custHov ? ACCENT : PANEL);
        drawTextCenter(matrices, "Customize", custX + custW/2, custY + 6, WHITE);

        fill(matrices, panelX, panelY + 48, panelX + panelW, panelY + 49, DIVIDER);

        hoveredIdx = -1;
        int rowY = panelY + 49;
        for (int i = 0; i < modules.size(); i++) {
            Module mod = modules.get(i);
            boolean hov = mouseX >= panelX && mouseX <= panelX + panelW && mouseY >= rowY && mouseY < rowY + rowH;
            if (hov) hoveredIdx = i;

            if (hov) {
                fill(matrices, panelX, rowY, panelX + panelW, rowY + rowH, PANEL_HOV);
            }

            boolean on = mod.enabled.getAsBoolean();
            float target = on ? 1f : 0f;
            mod.anim += (target - mod.anim) * Math.min(1f, delta * 0.2f);

            int centerY = rowY + (rowH / 2);

            // Icon Background
            fillRounded(matrices, panelX + 14, centerY - 12, panelX + 38, centerY + 12, 6, on ? ACCENT : PANEL);
            
            // Render PNG Icon
            RenderSystem.enableBlend();
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            if (this.client != null) {
                this.client.getTextureManager().bindTexture(mod.icon);
                drawTexture(matrices, panelX + 18, centerY - 8, 0.0F, 0.0F, 16, 16, 16, 16);
            }
            RenderSystem.disableBlend();

            drawText(matrices, mod.name, panelX + 48, centerY - 11, WHITE);
            drawText(matrices, mod.desc, panelX + 48, centerY + 2, GREY);

            // Toggle Pill
            int pillX = panelX + panelW - 52;
            int pillY = centerY - 7;
            int pillW = 34;
            int pillH = 14;
            fillRounded(matrices, pillX, pillY, pillX + pillW, pillY + pillH, 7, on ? ACCENT : PANEL);
            int dotX = (int)(pillX + 2 + (pillW - 14) * mod.anim);
            fillRounded(matrices, dotX, pillY + 2, dotX + 10, pillY + pillH - 2, 5, WHITE);

            fill(matrices, panelX + 14, rowY + rowH - 1, panelX + panelW - 14, rowY + rowH, DIVIDER);
            rowY += rowH;
        }

        // Footer
        fillRounded(matrices, panelX, panelY + panelH - 28, panelX + panelW, panelY + panelH, 12, PANEL);
        // Fix top corners of footer
        fill(matrices, panelX, panelY + panelH - 28, panelX + panelW, panelY + panelH - 14, PANEL);
        
        drawTextCenter(matrices, "Press R or ESC to close", panelX + panelW / 2, panelY + panelH - 18, GREY);

        matrices.pop();
        
        // Only run super render (for tooltips/particles) if not fully transparent
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (isClosing || Util.getMeasuringTimeMs() - openTime < 800) return false;

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
        if (isClosing || Util.getMeasuringTimeMs() - openTime < 800) return false;

        if (keyCode == 82 || keyCode == 256) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public void onClose() {
        if (!isClosing) {
            isClosing = true;
            closeTime = Util.getMeasuringTimeMs();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
