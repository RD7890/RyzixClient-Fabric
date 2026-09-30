package com.ryzix.client.gui;

import com.ryzix.client.modules.OreESP;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class OreESPScreen extends Screen {

    private static final int SLIDE_MS = 200;

    private interface BoolGetter { boolean get(); }
    private interface BoolSetter { void set(boolean v); }

    private static class OreEntry {
        final String name;
        final int color;
        final BoolGetter getter;
        final BoolSetter setter;
        float anim;
        OreEntry(String name, int color, BoolGetter getter, BoolSetter setter) {
            this.name = name; this.color = color; this.getter = getter; this.setter = setter;
            this.anim = getter.get() ? 1f : 0f;
        }
    }

    private final OreEntry[] ores = {
        new OreEntry("Diamond Ore",  0xFF00E5FF, () -> OreESP.showDiamond, v -> OreESP.showDiamond = v),
        new OreEntry("Gold Ore",     0xFFFFD700, () -> OreESP.showGold,    v -> OreESP.showGold = v),
        new OreEntry("Iron Ore",     0xFFD8AF93, () -> OreESP.showIron,    v -> OreESP.showIron = v),
        new OreEntry("Lapis Lazuli", 0xFF345BEB, () -> OreESP.showLapis,   v -> OreESP.showLapis = v),
        new OreEntry("Coal Ore",     0xFF333333, () -> OreESP.showCoal,    v -> OreESP.showCoal = v),
    };

    private final Screen parent;
    private float masterAnim = OreESP.isEnabled() ? 1f : 0f;
    private long lastFrame = Util.getMeasuringTimeMs();

    // slide animation: enters from the right, leaves to the right
    private final long enterStart = Util.getMeasuringTimeMs();
    private boolean leaving = false;
    private long leaveStart = 0;
    private boolean leaveDone = false;

    private int px, py, pw, ph, padX, padY;
    private int scroll = 0, maxScroll = 0;

    public OreESPScreen(Screen parent) {
        super(Text.literal("OreESP"));
        this.parent = parent;
    }

    // ------------------------------------------------------------ layout

    @Override
    protected void init() {
        pw = Ui.panelW(this.width);
        ph = Ui.panelH(this.height);
        px = (this.width - pw) / 2;
        py = (this.height - ph) / 2;
        padX = Ui.u(20);
        padY = Ui.u(15);
    }

    private int titleH()   { return Ui.u(24); }
    private int masterY()  { return py + padY + titleH() + Ui.u(20); }
    private int masterH()  { return Ui.u(52); }
    private int listY()    { return masterY() + masterH() + Ui.u(20); }
    private int listH()    { return py + ph - padY - listY(); }
    private int rowH()     { return Ui.u(40); }
    private int rowGap()   { return Ui.u(8); }
    private int rowW()     { return pw - padX * 2 - Ui.u(5); }

    private int backX()    { return px + padX; }
    private int backSz()   { return Ui.u(18); }
    private int backY()    { return py + padY + (titleH() - backSz()) / 2; }

    private void updateScroll() {
        int content = ores.length * (rowH() + rowGap()) - rowGap();
        maxScroll = Math.max(0, content - listH());
        scroll = Math.max(0, Math.min(maxScroll, scroll));
    }

    // ------------------------------------------------------------ render

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        long now = Util.getMeasuringTimeMs();
        float dt = Math.min(100f, now - lastFrame);
        lastFrame = now;
        int slide = Ui.u(20);

        float va = 1f;
        int vx = 0;
        boolean animating = false;
        if (leaving) {
            float t = Ui.ease((now - leaveStart) / (float) SLIDE_MS);
            va = 1f - t;
            vx = Math.round(slide * t);
            animating = true;
            if (now - leaveStart >= SLIDE_MS) leaveDone = true;
        } else {
            float t = (now - enterStart) / (float) SLIDE_MS;
            if (t < 1f) {
                float e = Ui.ease(t);
                va = e;
                vx = Math.round(slide * (1f - e));
                animating = true;
            }
        }
        boolean canHover = !animating;

        ctx.fill(0, 0, this.width, this.height, 0x4D000000);
        Ui.window(ctx, px, py, pw, ph);

        ctx.enableScissor(px, py, px + pw, py + ph);
        updateScroll();
        drawTitle(ctx, mouseX, mouseY, canHover, va, vx);
        drawMaster(ctx, dt, va, vx);
        drawList(ctx, mouseX, mouseY, canHover, dt, va, vx);
        ctx.disableScissor();

        if (leaveDone) {
            leaveDone = false;
            if (this.client != null) {
                this.client.setScreen(parent);
                if (parent instanceof ModMenuScreen) ((ModMenuScreen) parent).slideIn(-1);
            }
        }
    }

    private void drawTitle(DrawContext ctx, int mx, int my, boolean canHover, float va, int vx) {
        int bs = backSz();
        boolean hov = canHover && Ui.in(mx, my, backX() - Ui.u(5), backY() - Ui.u(5), bs + Ui.u(10), bs + Ui.u(10));
        Ui.icon(ctx, "arrow-left", backX() + vx, backY(), bs, (hov ? 1f : 0.55f) * va);

        int cy = py + padY + titleH() / 2;
        int tx = backX() + bs + Ui.u(5) + Ui.u(15) + vx;
        Ui.icon(ctx, "gem", tx, cy - bs / 2, bs, 0.55f * va);
        Ui.text(ctx, this.textRenderer, "OreESP", tx + bs + Ui.u(10), cy, 20, Ui.alpha(Ui.TEXT, va));
    }

    private void drawMaster(DrawContext ctx, float dt, float va, int vx) {
        float target = OreESP.isEnabled() ? 1f : 0f;
        float step = dt / 300f;
        masterAnim += Math.max(-step, Math.min(step, target - masterAnim));

        int x = px + padX + vx;
        int y = masterY();
        int w = pw - padX * 2;
        int h = masterH();
        Ui.rrect(ctx, x, y, w, h, Ui.u(6), Ui.alpha(Ui.ROW_SOFT, va));
        int cy = y + h / 2;
        Ui.text(ctx, this.textRenderer, "Enable Module", x + Ui.u(20), cy, 15, Ui.alpha(Ui.TEXT, va));
        Ui.toggle(ctx, x + w - Ui.u(20) - Ui.TOGGLE_W, cy - Ui.TOGGLE_H / 2, masterAnim, va);
    }

    private void drawList(DrawContext ctx, int mx, int my, boolean canHover, float dt, float va, int vx) {
        int lx = px + padX;
        int ly = listY();
        int lh = listH();
        ctx.enableScissor(Math.max(px, lx + vx), ly, Math.min(px + pw, lx + rowW() + Ui.u(5) + vx), ly + lh);

        int rw = rowW();
        for (int i = 0; i < ores.length; i++) {
            OreEntry o = ores[i];
            float target = o.getter.get() ? 1f : 0f;
            float step = dt / 300f;
            o.anim += Math.max(-step, Math.min(step, target - o.anim));

            int y = ly + i * (rowH() + rowGap()) - scroll;
            if (y + rowH() < ly || y > ly + lh) continue;
            int x = lx + vx;

            Ui.rrect(ctx, x, y, rw, rowH(), Ui.u(4), Ui.alpha(Ui.CARD, va));
            int cy = y + rowH() / 2;

            // colour dot with glow
            int dot = Ui.u(12);
            int dx = x + Ui.u(18);
            Ui.rrect(ctx, dx - 1, cy - dot / 2 - 1, dot + 2, dot + 2, dot / 2 + 1, Ui.alpha((o.color & 0xFFFFFF) | 0x33000000, va));
            Ui.rrect(ctx, dx, cy - dot / 2, dot, dot, dot / 2, Ui.alpha(o.color, va));

            Ui.text(ctx, this.textRenderer, o.name, dx + dot + Ui.u(10), cy, 13, Ui.alpha(Ui.TEXT, va));
            Ui.toggle(ctx, x + rw - Ui.u(18) - Ui.TOGGLE_W, cy - Ui.TOGGLE_H / 2, o.anim, va);
        }

        if (maxScroll > 0) {
            int content = ores.length * (rowH() + rowGap()) - rowGap();
            int thumbH = Math.max(Ui.u(30), lh * lh / content);
            int thumbY = ly + (int) ((lh - thumbH) * (scroll / (float) maxScroll));
            Ui.rrect(ctx, lx + rw + Ui.u(5) - Ui.u(4) + vx, thumbY, Ui.u(4), thumbH, Ui.u(2), Ui.alpha(0x1AFFFFFF, va));
        }
        ctx.disableScissor();
    }

    // ------------------------------------------------------------ input

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double amount) {
        if (maxScroll > 0) {
            scroll = Math.max(0, Math.min(maxScroll, scroll - (int) (amount * 12)));
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (leaving || Util.getMeasuringTimeMs() - enterStart < SLIDE_MS) return true;
        if (btn == 0) {
            int bs = backSz();
            if (Ui.in(mx, my, backX() - Ui.u(5), backY() - Ui.u(5), bs + Ui.u(10), bs + Ui.u(10))) {
                this.close();
                return true;
            }
            // master toggle row
            if (Ui.in(mx, my, px + padX, masterY(), pw - padX * 2, masterH())) {
                OreESP.toggle();
                return true;
            }
            // ore rows (only inside the visible list area)
            if (Ui.in(mx, my, px + padX, listY(), rowW(), listH())) {
                for (int i = 0; i < ores.length; i++) {
                    int y = listY() + i * (rowH() + rowGap()) - scroll;
                    if (Ui.in(mx, my, px + padX, y, rowW(), rowH())) {
                        OreEntry o = ores[i];
                        o.setter.set(!o.getter.get());
                        return true;
                    }
                }
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

    /** Slides out to the right, then returns to the main menu (which slides in from the left). */
    @Override
    public void close() {
        if (leaving) return;
        leaving = true;
        leaveStart = Util.getMeasuringTimeMs();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
