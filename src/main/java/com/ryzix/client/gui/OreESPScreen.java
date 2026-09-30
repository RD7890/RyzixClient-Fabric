package com.ryzix.client.gui;

import com.ryzix.client.modules.OreESP;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class OreESPScreen extends Screen {

    private static final int ACCENT    = 0xFFFD1523;
    private static final int BG        = 0xFF0A0A0A;
    private static final int PANEL     = 0xFF111111;
    private static final int PANEL_HOV = 0xFF1C1C1C;
    private static final int DIVIDER   = 0xFF1E1E1E;
    private static final int WHITE     = 0xFFFFFFFF;
    private static final int GREY      = 0xFF888888;

    private final Screen parent;
    private long openTime;

    private int panelW = 260;
    private int panelH = 0;
    private int panelX, panelY;
    private static final int ROW_H = 50;
    private static final int HEADER_H = 44;
    private static final int MASTER_H = 44; // Master toggle row height
    private static final int FOOTER_H = 24;

    // Ore definitions
    private static final OreEntry[] ORES = {
        new OreEntry("Iron Ore",    "Shows iron ore blocks",    0xFFAAAAAA, () -> OreESP.showIron,    v -> OreESP.showIron = v),
        new OreEntry("Gold Ore",    "Shows gold ore blocks",    0xFFFFDD00, () -> OreESP.showGold,    v -> OreESP.showGold = v),
        new OreEntry("Lapis Ore",   "Shows lapis lazuli ore",   0xFF2255CC, () -> OreESP.showLapis,   v -> OreESP.showLapis = v),
        new OreEntry("Diamond Ore", "Shows diamond ore blocks", 0xFF00DDDD, () -> OreESP.showDiamond, v -> OreESP.showDiamond = v),
    };

    // Hovered row: -1 = none, -2 = master toggle, 0..3 = ores
    private int hoveredIdx = -1;
    private float masterAnim = 0f;
    private final float[] pillAnim = new float[ORES.length];

    private interface BoolGetter { boolean get(); }
    private interface BoolSetter { void set(boolean v); }

    private static class OreEntry {
        final String name;
        final String desc;
        final int color;
        final BoolGetter getter;
        final BoolSetter setter;
        OreEntry(String name, String desc, int color, BoolGetter getter, BoolSetter setter) {
            this.name = name; this.desc = desc; this.color = color;
            this.getter = getter; this.setter = setter;
        }
    }

    public OreESPScreen(Screen parent) {
        super(Text.literal("OreESP"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        openTime = Util.getMeasuringTimeMs();
        panelH = HEADER_H + MASTER_H + ORES.length * ROW_H + FOOTER_H;
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;
        masterAnim = OreESP.isEnabled() ? 1f : 0f;
        for (int i = 0; i < ORES.length; i++) {
            pillAnim[i] = ORES[i].getter.get() ? 1f : 0f;
        }
    }

    private void drawText(DrawContext m, String t, int x, int y, int color) {
        m.drawText(this.textRenderer, t, x, y, color, true);
    }

    private void drawTextCenter(DrawContext m, String t, int cx, int y, int color) {
        int w = this.textRenderer.getWidth(t);
        m.drawText(this.textRenderer, t, cx - w / 2, y, color, true);
    }

    @Override
    public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
        matrices.fill(0, 0, this.width, this.height, 0x60000000);

        // Main panel
        matrices.fill(panelX, panelY, panelX + panelW, panelY + panelH, BG);

        // Header
        matrices.fill(panelX, panelY, panelX + panelW, panelY + 3, ACCENT);
        matrices.fill(panelX, panelY + 3, panelX + panelW, panelY + HEADER_H, PANEL);

        // Back arrow
        matrices.fill(panelX + 10, panelY + 13, panelX + 28, panelY + 31, ACCENT);
        drawTextCenter(matrices, "<", panelX + 19, panelY + 18, WHITE);

        drawText(matrices, "OreESP Settings", panelX + 36, panelY + 14, WHITE);
        drawText(matrices, "Press Z to quick toggle", panelX + 36, panelY + 25, GREY);

        matrices.fill(panelX, panelY + HEADER_H, panelX + panelW, panelY + HEADER_H + 1, DIVIDER);

        hoveredIdx = -1;

        // ── MASTER TOGGLE ROW ────────────────────────────────────────
        int masterY = panelY + HEADER_H + 1;
        boolean masterOn = OreESP.isEnabled();
        boolean masterHov = mouseX >= panelX && mouseX <= panelX + panelW
                         && mouseY >= masterY && mouseY < masterY + MASTER_H;
        if (masterHov) hoveredIdx = -2;

        matrices.fill(panelX, masterY, panelX + panelW, masterY + MASTER_H, masterHov ? PANEL_HOV : PANEL);

        // Master left accent bar
        float masterTarget = masterOn ? 1f : 0f;
        masterAnim += (masterTarget - masterAnim) * Math.min(1f, delta * 0.2f);
        if (masterAnim > 0.01f) {
            int sh = (int)(MASTER_H * masterAnim);
            matrices.fill(panelX, masterY + (MASTER_H - sh), panelX + 3, masterY + MASTER_H, ACCENT);
        }

        int mcy = masterY + MASTER_H / 2;
        matrices.fill(panelX + 14, mcy - 11, panelX + 36, mcy + 11, masterOn ? ACCENT : 0xFF222222);
        drawTextCenter(matrices, "O", panelX + 25, mcy - 4, WHITE);

        drawText(matrices, "OreESP", panelX + 46, mcy - 9, WHITE);
        drawText(matrices, masterOn ? "Enabled" : "Disabled", panelX + 46, mcy + 3, masterOn ? ACCENT : GREY);

        // Master toggle pill
        int mpX = panelX + panelW - 50;
        int mpY = mcy - 7;
        matrices.fill(mpX, mpY, mpX + 32, mpY + 14, masterOn ? ACCENT : 0xFF333333);
        int mdotX = (int)(mpX + 2 + 18 * masterAnim);
        matrices.fill(mdotX, mpY + 2, mdotX + 10, mpY + 10, WHITE);

        matrices.fill(panelX + 10, masterY + MASTER_H - 1, panelX + panelW - 10, masterY + MASTER_H, DIVIDER);

        // ── INDIVIDUAL ORE ROWS ──────────────────────────────────────
        int rowY = masterY + MASTER_H;

        for (int i = 0; i < ORES.length; i++) {
            OreEntry ore = ORES[i];
            boolean on = ore.getter.get();
            boolean hov = mouseX >= panelX && mouseX <= panelX + panelW
                       && mouseY >= rowY && mouseY < rowY + ROW_H;
            if (hov) hoveredIdx = i;

            matrices.fill(panelX, rowY, panelX + panelW, rowY + ROW_H, hov ? PANEL_HOV : BG);

            // Left accent bar
            float target = on ? 1f : 0f;
            pillAnim[i] += (target - pillAnim[i]) * Math.min(1f, delta * 0.2f);
            if (pillAnim[i] > 0.01f) {
                int sh = (int)(ROW_H * pillAnim[i]);
                matrices.fill(panelX, rowY + (ROW_H - sh), panelX + 3, rowY + ROW_H, ACCENT);
            }

            int centerY = rowY + ROW_H / 2;

            // Ore color square
            matrices.fill(panelX + 14, centerY - 10, panelX + 34, centerY + 10, on ? ore.color : 0xFF333333);

            drawText(matrices, ore.name, panelX + 44, centerY - 9, WHITE);
            drawText(matrices, ore.desc, panelX + 44, centerY + 3, GREY);

            // Toggle pill
            int pillX = panelX + panelW - 50;
            int pillY = centerY - 7;
            matrices.fill(pillX, pillY, pillX + 32, pillY + 14, on ? ACCENT : 0xFF333333);
            int dotX = (int)(pillX + 2 + 18 * pillAnim[i]);
            matrices.fill(dotX, pillY + 2, dotX + 10, pillY + 10, WHITE);

            matrices.fill(panelX + 10, rowY + ROW_H - 1, panelX + panelW - 10, rowY + ROW_H, DIVIDER);
            rowY += ROW_H;
        }

        // Footer
        matrices.fill(panelX, panelY + panelH - FOOTER_H, panelX + panelW, panelY + panelH, PANEL);
        drawTextCenter(matrices, "Press ESC to go back", panelX + panelW / 2, panelY + panelH - 15, GREY);

    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn == 0) {
            // Back arrow
            if (mx >= panelX + 10 && mx <= panelX + 28 && my >= panelY + 13 && my <= panelY + 31) {
                this.close();
                return true;
            }
            // Master toggle
            if (hoveredIdx == -2) {
                OreESP.toggle();
                return true;
            }
            // Individual ore toggle
            if (hoveredIdx >= 0) {
                OreEntry ore = ORES[hoveredIdx];
                ore.setter.set(!ore.getter.get());
                return true;
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 || keyCode == 82) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
