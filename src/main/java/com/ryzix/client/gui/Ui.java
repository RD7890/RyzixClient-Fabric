package com.ryzix.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Shared drawing helpers for the RyzixClient GUI (matches the HTML mockup).
 * All layout numbers are the mockup's CSS pixels multiplied by {@link #S}.
 */
public final class Ui {
    private Ui() {}

    // CSS px -> GUI px: dynamic based on actual screen height for FHD quality
    // Clamps between 0.5 (small phone) and 1.4 (large/tablet)
    private static float _S = 0f;
    public static float S(int screenH) {
        if (_S == 0f) _S = Math.max(0.5f, Math.min(1.4f, screenH / 540f));
        return _S;
    }
    /** Call once per resize to refresh the cached scale. */
    public static void resetScale() { _S = 0f; }
    /** Quick accessor - uses last known scale, defaults to 0.85 if not yet computed. */
    public static float S() { return _S == 0f ? 0.85f : _S; }
    public static final float S = 0.85f; // legacy fallback, prefer S(screenH)

    // Palette (from the mockup)
    public static final int ACCENT     = 0xFFFD1523;
    public static final int BG         = 0xEB000000; // rgba(0,0,0,0.92)
    public static final int CARD       = 0x99141414; // rgba(20,20,20,0.6)
    public static final int CARD_HOV   = 0xCC232323; // rgba(35,35,35,0.8)
    public static final int ROW_SOFT   = 0x08FFFFFF; // rgba(255,255,255,0.03)
    public static final int TEXT       = 0xFFE0E0E0;
    public static final int MUTED      = 0xFF888888;
    public static final int SWITCH_OFF = 0x990A0A0A; // rgba(10,10,10,0.6)
    public static final int KNOB_OFF   = 0xFF666666;
    public static final int WHITE      = 0xFFFFFFFF;

    // Custom font (assets/ryzixclient/font/ryzix.json -> RyzixFont.otf)
    public static final Style FONT = Style.EMPTY.withFont(new Identifier("ryzixclient", "ryzix"));
    private static final float FONT_EM = 10f;   // "size" in ryzix.json
    // If text looks a bit high/low in-game, tweak this one value (in font units, 0..10).
    private static final float CAP_MID = 3.5f;

    public static final Identifier LOGO = new Identifier("ryzixclient", "textures/gui/logo.png");

    public static Identifier icon(String name) {
        return new Identifier("ryzixclient", "textures/gui/icons/" + name + ".png");
    }

    // ---------------------------------------------------------------- math

    public static int u(float css) {
        return Math.max(1, Math.round(css * S));
    }

    public static float clamp01(float t) {
        return t < 0f ? 0f : Math.min(1f, t);
    }

    public static float ease(float t) {
        t = clamp01(t);
        return t * t * (3f - 2f * t);
    }

    public static int alpha(int color, float a) {
        int na = Math.max(0, Math.min(255, Math.round((color >>> 24) * a)));
        return (na << 24) | (color & 0xFFFFFF);
    }

    public static int lerp(int c0, int c1, float t) {
        t = clamp01(t);
        int a = Math.round(((c0 >>> 24) & 0xFF) * (1 - t) + ((c1 >>> 24) & 0xFF) * t);
        int r = Math.round(((c0 >> 16) & 0xFF) * (1 - t) + ((c1 >> 16) & 0xFF) * t);
        int g = Math.round(((c0 >> 8) & 0xFF) * (1 - t) + ((c1 >> 8) & 0xFF) * t);
        int b = Math.round((c0 & 0xFF) * (1 - t) + (c1 & 0xFF) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    // ---------------------------------------------------------------- shapes

    /** Anti-aliased rounded rectangle built from horizontal strips. */
    public static void rrect(DrawContext c, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0 || (color >>> 24) == 0) return;
        r = Math.min(r, Math.min(w, h) / 2);
        if (r <= 0) {
            c.fill(x, y, x + w, y + h, color);
            return;
        }
        if (h - 2 * r > 0) c.fill(x, y + r, x + w, y + h - r, color);
        for (int i = 0; i < r; i++) {
            float dy = r - i - 0.5f;
            float ex = r - (float) Math.sqrt(Math.max(0f, (float) r * r - dy * dy));
            int inset = (int) Math.floor(ex);
            int edge = alpha(color, 1f - (ex - inset));
            int x0 = x + inset;
            int x1 = x + w - inset;
            int top = y + i;
            int bot = y + h - 1 - i;
            if (x1 - x0 > 2) {
                c.fill(x0 + 1, top, x1 - 1, top + 1, color);
                c.fill(x0, top, x0 + 1, top + 1, edge);
                c.fill(x1 - 1, top, x1, top + 1, edge);
                c.fill(x0 + 1, bot, x1 - 1, bot + 1, color);
                c.fill(x0, bot, x0 + 1, bot + 1, edge);
                c.fill(x1 - 1, bot, x1, bot + 1, edge);
            } else {
                c.fill(x0, top, x1, top + 1, color);
                c.fill(x0, bot, x1, bot + 1, color);
            }
        }
    }

    /** Soft drop shadow like `box-shadow: 0 10px 30px rgba(0,0,0,.7)`. */
    public static void shadow(DrawContext c, int x, int y, int w, int h, int r, float a) {
        for (int i = 7; i >= 1; i--) {
            rrect(c, x - i, y - i + 3, w + 2 * i, h + 2 * i, r + i, alpha(0x0F000000, a));
        }
    }

    public static void window(DrawContext c, int x, int y, int w, int h) {
        shadow(c, x, y, w, h, u(6), 1f);
        rrect(c, x, y, w, h, u(6), BG);
    }

    public static int panelW(int screenW) {
        return Math.min(u(850), (int) (screenW * 0.95f));
    }

    public static int panelH(int screenH) {
        return Math.min(u(600), (int) (screenH * 0.85f));
    }

    // ---------------------------------------------------------------- toggle

    public static final int TOGGLE_W = 17;
    public static final int TOGGLE_H = 8;

    /** The mockup's ".vape-switch". anim: 0 = off, 1 = on. */
    public static void toggle(DrawContext c, int x, int y, float anim, float a) {
        rrect(c, x, y, TOGGLE_W, TOGGLE_H, TOGGLE_H / 2, alpha(lerp(SWITCH_OFF, ACCENT, anim), a));
        int kx = x + 1 + Math.round(anim * (TOGGLE_W - 2 - 6));
        rrect(c, kx, y + 1, 6, 6, 3, alpha(lerp(KNOB_OFF, WHITE, anim), a));
    }

    // ---------------------------------------------------------------- textures

    public static void tex(DrawContext c, Identifier id, int x, int y, int size, int texSize, float a) {
        if (a <= 0.01f) return;
        RenderSystem.enableBlend();
        c.setShaderColor(1f, 1f, 1f, a);
        c.drawTexture(id, x, y, size, size, 0f, 0f, texSize, texSize, texSize, texSize);
        c.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }

    /** White icon tinted by alpha (mockup: opacity .55, 1.0 on hover). */
    public static void icon(DrawContext c, String name, int x, int y, int size, float a) {
        tex(c, icon(name), x, y, size, 64, a);
    }

    // ---------------------------------------------------------------- text

    public static float scale(float css) {
        return css * S / FONT_EM;
    }

    public static int textW(TextRenderer tr, String s, float css) {
        if (s.isEmpty()) return 0;
        return Math.round(tr.getWidth(Text.literal(s).setStyle(FONT)) * scale(css));
    }

    /** Draws text with the custom font; (x, cy) = left edge and vertical centre of the capitals. */
    public static void text(DrawContext c, TextRenderer tr, String s, int x, int cy, float css, int color) {
        if (s.isEmpty() || (color >>> 24) < 4) return;
        float sc = scale(css);
        MatrixStack m = c.getMatrices();
        m.push();
        m.translate(x, Math.round(cy - CAP_MID * sc), 0);
        m.scale(sc, sc, 1f);
        c.drawText(tr, Text.literal(s).setStyle(FONT), 0, 0, color, false);
        m.pop();
    }
}
