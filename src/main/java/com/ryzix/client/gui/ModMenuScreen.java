package com.ryzix.client.gui;

import com.ryzix.client.modules.ChestCounterHUD;
import com.ryzix.client.modules.FullBright;
import com.ryzix.client.modules.OreESP;
import com.ryzix.client.modules.PlayerESP;
import com.ryzix.client.modules.StorageESP;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class ModMenuScreen extends Screen {

    private static final int ACCENT    = 0xFFFD1523;
    private static final int BG        = 0xFF0A0A0A;
    private static final int PANEL     = 0xFF111111;
    private static final int PANEL_HOV = 0xFF1C1C1C;
    private static final int DIVIDER   = 0xFF1E1E1E;
    private static final int WHITE     = 0xFFFFFFFF;
    private static final int GREY      = 0xFF888888;
    private static final int SCROLLBAR = 0xFF333333;
    private static final int SCROLLBAR_THUMB = 0xFFFF2541;

    private static final int PANEL_W   = 280;
    private static final int HEADER_H  = 48;
    private static final int FOOTER_H  = 28;
    private static final int ROW_H     = 52;
    private static final int SCROLLBAR_W = 4;

    private int panelH;
    private int panelX, panelY;

    // Scroll state
    private int scrollOffset   = 0;
    private int maxScroll      = 0;
    private int totalRowsH     = 0;
    private int visibleRowsH   = 0;
    private boolean draggingScrollbar = false;
    private int dragStartY     = 0;
    private int dragStartScroll = 0;

    private final Screen parent;
    private long openTime;

    private static final Identifier LOGO = new Identifier("ryzixclient", "textures/gui/logo.png");

    private static class Module {
        final Identifier icon;
        final String name;
        final String desc;
        final Runnable onClick;
        final BooleanSupplier enabled;
        final boolean isSubScreen; // true = shows ">" arrow instead of toggle pill
        float anim;

        Module(Identifier icon, String name, String desc, Runnable onClick, BooleanSupplier enabled) {
            this(icon, name, desc, onClick, enabled, false);
        }

        Module(Identifier icon, String name, String desc, Runnable onClick, BooleanSupplier enabled, boolean isSubScreen) {
            this.icon = icon; this.name = name; this.desc = desc;
            this.onClick = onClick; this.enabled = enabled; this.isSubScreen = isSubScreen;
            this.anim = enabled != null && enabled.getAsBoolean() ? 1f : 0f;
        }
    }

    private final List<Module> modules = new ArrayList<>();
    private int hoveredIdx = -1;

    public ModMenuScreen(Screen parent, boolean playIntro) {
        super(new LiteralText("RyzixClient"));
        this.parent = parent;

        modules.add(new Module(
            new Identifier("ryzixclient", "textures/gui/icons/storage.png"),
            "StorageESP", "Highlight storage containers",
            StorageESP::toggle, StorageESP::isEnabled));

        modules.add(new Module(
            new Identifier("ryzixclient", "textures/gui/icons/player.png"),
            "PlayerESP", "See players through walls",
            PlayerESP::toggle, PlayerESP::isEnabled));

        modules.add(new Module(
            new Identifier("ryzixclient", "textures/gui/icons/sun.png"),
            "FullBright", "Maximum visibility in the dark",
            FullBright::toggle, FullBright::isEnabled));

        modules.add(new Module(
            new Identifier("ryzixclient", "textures/gui/icons/chest.png"),
            "Chest Counter", "HUD showing nearby storage count",
            ChestCounterHUD::toggle, ChestCounterHUD::isEnabled));

        modules.add(new Module(
            new Identifier("ryzixclient", "textures/gui/icons/ore.png"),
            "OreESP", "Highlight ores \u00BB Settings",
            () -> { if (this.client != null) this.client.openScreen(new OreESPScreen(this)); },
            null, true));
    }

    public ModMenuScreen() {
        this(null, true);
    }

    @Override
    protected void init() {
        openTime = Util.getMeasuringTimeMs();
        scrollOffset = 0;

        totalRowsH  = modules.size() * ROW_H;
        int maxPanelH = this.height - 20;
        int idealPanelH = HEADER_H + totalRowsH + FOOTER_H;

        // Panel is capped to screen height, rows scroll inside it
        panelH = Math.min(idealPanelH, maxPanelH);
        visibleRowsH = panelH - HEADER_H - FOOTER_H;
        maxScroll = Math.max(0, totalRowsH - visibleRowsH);

        panelX = (this.width - PANEL_W) / 2;
        panelY = (this.height - panelH) / 2;
    }

    private void drawText(MatrixStack m, String t, int x, int y, int color) {
        this.textRenderer.drawWithShadow(m, t, (float) x, (float) y, color);
    }

    private void drawTextCenter(MatrixStack m, String t, int cx, int y, int color) {
        int w = this.textRenderer.getWidth(t);
        this.textRenderer.drawWithShadow(m, t, (float)(cx - w / 2), (float) y, color);
    }

    private void enableScissor(int x, int y, int w, int h) {
        double scale = this.client.getWindow().getScaleFactor();
        int screenH = this.client.getWindow().getFramebufferHeight();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(
            (int)(x * scale),
            (int)(screenH - (y + h) * scale),
            (int)(w * scale),
            (int)(h * scale)
        );
    }

    private void disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        long elapsed = Util.getMeasuringTimeMs() - openTime;

        // ── INTRO ANIMATION ────────────────────────────────────────────
        if (elapsed < 600) {
            float alpha = elapsed > 400 ? 1f - ((elapsed - 400) / 200f) : 1f;
            int bgA = (int)(alpha * 255);
            fill(matrices, 0, 0, this.width, this.height, (bgA << 24) | 0x050505);

            RenderSystem.enableBlend();
            RenderSystem.color4f(1f, 1f, 1f, alpha);
            if (this.client != null) {
                this.client.getTextureManager().bindTexture(LOGO);
                int ls = 64;
                int lx = (this.width - ls) / 2;
                int ly = (this.height - ls) / 2 - 20;
                drawTexture(matrices, lx, ly, 0f, 0f, ls, ls, ls, ls);

                int bw = 140, bh = 3;
                int bx = (this.width - bw) / 2;
                int by = ly + ls + 25;
                float prog = Math.min(1f, elapsed / 400f);
                fill(matrices, bx, by, bx + bw, by + bh, (bgA << 24) | 0x222222);
                fill(matrices, bx, by, bx + (int)(bw * prog), by + bh, (bgA << 24) | 0xFFFD1523);
            }
            RenderSystem.color4f(1f, 1f, 1f, 1f);
            RenderSystem.disableBlend();
            return;
        }

        // ── BACKGROUND DIM ─────────────────────────────────────────────
        fill(matrices, 0, 0, this.width, this.height, 0x40000000);

        // ── PANEL BACKGROUND ───────────────────────────────────────────
        fill(matrices, panelX, panelY, panelX + PANEL_W, panelY + panelH, BG);

        // ── HEADER ─────────────────────────────────────────────────────
        fill(matrices, panelX, panelY, panelX + PANEL_W, panelY + 3, ACCENT);
        fill(matrices, panelX, panelY + 3, panelX + PANEL_W, panelY + HEADER_H, PANEL);

        RenderSystem.enableBlend();
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        if (this.client != null) {
            this.client.getTextureManager().bindTexture(LOGO);
            drawTexture(matrices, panelX + 12, panelY + 12, 0f, 0f, 24, 24, 24, 24);
        }
        RenderSystem.disableBlend();

        drawText(matrices, "RyzixClient",      panelX + 44, panelY + 15, WHITE);
        drawText(matrices, "v1.0  |  Modules", panelX + 44, panelY + 27, GREY);

        // Customize button
        int custX = panelX + PANEL_W - 85, custY = panelY + 14;
        boolean custHov = inBox(mouseX, mouseY, custX, custY, 75, 20);
        fill(matrices, custX, custY, custX + 75, custY + 20, custHov ? ACCENT : 0xFF222222);
        drawTextCenter(matrices, "Customize", custX + 37, custY + 6, WHITE);

        fill(matrices, panelX, panelY + HEADER_H, panelX + PANEL_W, panelY + HEADER_H + 1, DIVIDER);

        // ── ROWS (scissor-clipped, scrollable) ─────────────────────────
        int rowsAreaY = panelY + HEADER_H + 1;
        enableScissor(panelX, rowsAreaY, PANEL_W, visibleRowsH);

        hoveredIdx = -1;
        int rowY = rowsAreaY - scrollOffset;

        for (int i = 0; i < modules.size(); i++) {
            Module mod = modules.get(i);
            int rowBottom = rowY + ROW_H;

            // Only compute hover if row is actually visible
            boolean hov = false;
            if (rowBottom > rowsAreaY && rowY < rowsAreaY + visibleRowsH) {
                hov = inBox(mouseX, mouseY, panelX, rowY, PANEL_W, ROW_H);
                if (hov) hoveredIdx = i;
            }

            fill(matrices, panelX, rowY, panelX + PANEL_W, rowY + ROW_H, hov ? PANEL_HOV : BG);

            boolean on = mod.enabled != null && mod.enabled.getAsBoolean();
            float target = on ? 1f : 0f;
            mod.anim += (target - mod.anim) * Math.min(1f, delta * 0.2f);

            // Left accent bar
            if (mod.anim > 0.01f) {
                int sh = (int)(ROW_H * mod.anim);
                fill(matrices, panelX, rowY + (ROW_H - sh), panelX + 3, rowY + ROW_H, ACCENT);
            }

            int cy = rowY + ROW_H / 2;

            // Icon box
            fill(matrices, panelX + 14, cy - 11, panelX + 36, cy + 11, on ? ACCENT : 0xFF222222);
            RenderSystem.enableBlend();
            RenderSystem.color4f(1f, 1f, 1f, 1f);
            if (this.client != null) {
                this.client.getTextureManager().bindTexture(mod.icon);
                drawTexture(matrices, panelX + 17, cy - 8, 0f, 0f, 16, 16, 16, 16);
            }
            RenderSystem.disableBlend();

            drawText(matrices, mod.name, panelX + 46, cy - 10, WHITE);
            drawText(matrices, mod.desc, panelX + 46, cy + 2,  GREY);

            if (mod.isSubScreen) {
                // Show ">" arrow for sub-screen entries
                drawText(matrices, ">", panelX + PANEL_W - 22, cy - 4, ACCENT);
            } else {
                // Toggle pill
                int pillX = panelX + PANEL_W - 52;
                int pillY = cy - 7;
                fill(matrices, pillX, pillY, pillX + 34, pillY + 14, on ? ACCENT : 0xFF333333);
                int dotX = (int)(pillX + 2 + 20 * mod.anim);
                fill(matrices, dotX, pillY + 2, dotX + 10, pillY + 12, WHITE);
            }

            fill(matrices, panelX + 14, rowY + ROW_H - 1, panelX + PANEL_W - 14, rowY + ROW_H, DIVIDER);
            rowY += ROW_H;
        }

        disableScissor();

        // ── SCROLLBAR ──────────────────────────────────────────────────
        if (maxScroll > 0) {
            int sbX = panelX + PANEL_W - SCROLLBAR_W - 2;
            int sbY = rowsAreaY + 2;
            int sbH = visibleRowsH - 4;
            fill(matrices, sbX, sbY, sbX + SCROLLBAR_W, sbY + sbH, SCROLLBAR);

            int thumbH = Math.max(16, (int)((float) visibleRowsH / totalRowsH * sbH));
            int thumbY = sbY + (int)((float) scrollOffset / maxScroll * (sbH - thumbH));
            fill(matrices, sbX, thumbY, sbX + SCROLLBAR_W, thumbY + thumbH, SCROLLBAR_THUMB);
        }

        // ── FOOTER ─────────────────────────────────────────────────────
        fill(matrices, panelX, panelY + panelH - FOOTER_H, panelX + PANEL_W, panelY + panelH, PANEL);
        // Fade-shadow on bottom of rows area so cutoff looks clean
        fill(matrices, panelX, panelY + panelH - FOOTER_H - 8, panelX + PANEL_W, panelY + panelH - FOOTER_H, 0x60000000);
        drawTextCenter(matrices, "Press R or ESC to close", panelX + PANEL_W / 2, panelY + panelH - 18, GREY);

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (maxScroll > 0) {
            scrollOffset = clamp(scrollOffset - (int)(amount * 12), 0, maxScroll);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (Util.getMeasuringTimeMs() - openTime < 600) return false;

        // Customize button
        if (button == 0 && inBox((int)mx, (int)my, panelX + PANEL_W - 85, panelY + 14, 75, 20)) {
            this.client.openScreen(new HudEditScreen(this));
            return true;
        }

        // Scrollbar drag start
        if (button == 0 && maxScroll > 0) {
            int sbX = panelX + PANEL_W - SCROLLBAR_W - 2;
            int rowsAreaY = panelY + HEADER_H + 1;
            if (inBox((int)mx, (int)my, sbX, rowsAreaY, SCROLLBAR_W, visibleRowsH)) {
                draggingScrollbar = true;
                dragStartY = (int) my;
                dragStartScroll = scrollOffset;
                return true;
            }
        }

        if (button == 0 && hoveredIdx >= 0) {
            modules.get(hoveredIdx).onClick.run();
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (draggingScrollbar && maxScroll > 0) {
            int sbH = visibleRowsH - 4;
            int thumbH = Math.max(16, (int)((float) visibleRowsH / totalRowsH * sbH));
            float ratio = (float) maxScroll / (sbH - thumbH);
            scrollOffset = clamp(dragStartScroll + (int)((my - dragStartY) * ratio), 0, maxScroll);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        draggingScrollbar = false;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Util.getMeasuringTimeMs() - openTime < 600) return false;
        if (keyCode == 82 || keyCode == 256) { this.onClose(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (this.client != null) this.client.openScreen(parent);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private static boolean inBox(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my < y + h;
    }

    private static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }
}
