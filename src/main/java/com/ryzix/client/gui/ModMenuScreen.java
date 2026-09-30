package com.ryzix.client.gui;

import com.ryzix.client.modules.ChestCounterHUD;
import com.ryzix.client.modules.FullBright;
import com.ryzix.client.modules.OreESP;
import com.ryzix.client.modules.PlayerESP;
import com.ryzix.client.modules.StorageESP;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class ModMenuScreen extends Screen {

    private static final int INTRO_LOAD = 1500; // splash bar fill
    private static final int INTRO_FADE = 400;  // splash fade-out
    private static final int INTRO_MS   = INTRO_LOAD + INTRO_FADE;
    private static final int SLIDE_MS   = 200;
    private static final String[] TABS  = {"ALL", "VISUALS", "UTILITY"};

    private static class Module {
        final String name;
        final String category;
        final Identifier icon;
        final Runnable toggle;
        final BooleanSupplier enabled;
        final Runnable open; // null = no settings page
        float anim;

        Module(String name, String category, String icon, Runnable toggle, BooleanSupplier enabled, Runnable open) {
            this.name = name;
            this.category = category;
            this.icon = Ui.icon(icon);
            this.toggle = toggle;
            this.enabled = enabled;
            this.open = open;
            this.anim = enabled.getAsBoolean() ? 1f : 0f;
        }

        boolean on() { return enabled.getAsBoolean(); }
    }

    private static class Card {
        final Module mod;
        final int x, y;
        Card(Module mod, int x, int y) { this.mod = mod; this.x = x; this.y = y; }
    }

    private final List<Module> modules = new ArrayList<>();
    private final Screen parent;
    private final long openTime = Util.getMeasuringTimeMs();
    private long lastFrame = openTime;

    // view slide animation
    private long enterStart = 0;
    private int enterDir = 0;
    private boolean leaving = false;
    private long leaveStart = 0;
    private int leaveDir = 0;
    private Runnable leaveAction;
    private Runnable pendingAction;

    // tab switching
    private int activeTab = 0;
    private int shownTab = 0;
    private int tabDir = 0;
    private long tabStart = 0;

    // search
    private String query = "";
    private boolean searchFocused = false;

    // layout (px = panel origin)
    private int px, py, pw, ph;
    private int padX, padY;
    private int gridX, gridY, gridW, gridH;
    private int cols, cardW, cardH, gap;
    private int scroll = 0, maxScroll = 0, contentH = 0;
    private boolean draggingSb = false;
    private double dragStartMy;
    private int dragStartScroll;

    // per-frame animation values
    private float va = 1f;
    private int vx = 0;

    public ModMenuScreen(Screen parent, boolean playIntro) {
        super(Text.literal("RyzixClient"));
        this.parent = parent;

        modules.add(new Module("StorageESP", "VISUALS", "boxes-stacked",
                StorageESP::toggle, StorageESP::isEnabled, null));
        modules.add(new Module("PlayerESP", "VISUALS", "user",
                PlayerESP::toggle, PlayerESP::isEnabled, null));
        modules.add(new Module("FullBright", "VISUALS", "sun",
                FullBright::toggle, FullBright::isEnabled, null));
        modules.add(new Module("Chest Counter", "UTILITY", "box-open",
                ChestCounterHUD::toggle, ChestCounterHUD::isEnabled, null));
        modules.add(new Module("OreESP", "VISUALS", "gem",
                OreESP::toggle, OreESP::isEnabled, this::openOreSettings));
    }

    public ModMenuScreen() {
        this(null, true);
    }

    // ------------------------------------------------------------ navigation

    private void openOreSettings() {
        startLeave(-1, () -> {
            if (this.client != null) this.client.setScreen(new OreESPScreen(this));
        });
    }

    private void startLeave(int dir, Runnable action) {
        if (leaving) return;
        leaving = true;
        leaveStart = Util.getMeasuringTimeMs();
        leaveDir = dir;
        leaveAction = action;
    }

    /** Called by the sub-screen when returning: main view slides in from the given side. */
    void slideIn(int dir) {
        leaving = false;
        enterDir = dir;
        enterStart = Util.getMeasuringTimeMs();
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
        gap = Ui.u(12);
        cardH = Ui.u(90);
        gridX = px + padX;
        gridW = pw - padX * 2;
        gridY = navY() + navH() + Ui.u(12) + Ui.u(5);
        gridH = py + ph - padY - gridY;
    }

    private int navH() { return Ui.u(26); }
    private int navY() { return py + padY + Ui.u(22) + Ui.u(12); }

    private boolean matches(Module m) {
        boolean tab = shownTab == 0 || m.category.equals(TABS[shownTab]);
        boolean q = query.isEmpty() || m.name.toLowerCase().contains(query.toLowerCase());
        return tab && q;
    }

    private List<Card> layoutCards() {
        int minW = Ui.u(180);
        int avail = gridW - Ui.u(5);
        cols = Math.max(1, (avail + gap) / (minW + gap));
        cardW = (avail - gap * (cols - 1)) / cols;

        List<Card> out = new ArrayList<>();
        int i = 0;
        for (Module m : modules) {
            if (!matches(m)) continue;
            int col = i % cols;
            int row = i / cols;
            out.add(new Card(m, gridX + col * (cardW + gap), gridY + row * (cardH + gap) - scroll));
            i++;
        }
        int rows = (i + cols - 1) / cols;
        contentH = rows == 0 ? 0 : rows * (cardH + gap) - gap;
        maxScroll = Math.max(0, contentH - gridH);
        scroll = Math.max(0, Math.min(maxScroll, scroll));
        return out;
    }

    // tab rects: x positions + widths
    private int[] tabXs() {
        int[] xs = new int[TABS.length];
        int x = gridX;
        for (int i = 0; i < TABS.length; i++) {
            xs[i] = x;
            x += Ui.textW(this.textRenderer, TABS[i], 12) + Ui.u(20);
        }
        return xs;
    }

    private int searchW() { return Ui.u(204); }
    private int searchX() { return gridX + gridW - searchW(); }

    private String customizeLabel() { return "CUSTOMIZE"; }
    private int customizeX() { return gridX + gridW - Ui.textW(this.textRenderer, customizeLabel(), 11); }

    // ------------------------------------------------------------ render

    private boolean interactive(long now) {
        return now - openTime >= INTRO_MS && !leaving && enterDir == 0;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        long now = Util.getMeasuringTimeMs();
        float dt = Math.min(100f, now - lastFrame);
        lastFrame = now;

        // view slide (enter / leave)
        va = 1f;
        vx = 0;
        int slide = Ui.u(20);
        if (leaving) {
            float t = Ui.ease((now - leaveStart) / (float) SLIDE_MS);
            va = 1f - t;
            vx = Math.round(leaveDir * slide * t);
            if (now - leaveStart >= SLIDE_MS && pendingAction == null) pendingAction = leaveAction;
        } else if (enterDir != 0) {
            float t = (now - enterStart) / (float) SLIDE_MS;
            if (t >= 1f) {
                enterDir = 0;
            } else {
                float e = Ui.ease(t);
                va = e;
                vx = Math.round(enterDir * slide * (1f - e));
            }
        }

        // grid slide (tab switch)
        float gva = 1f;
        int gvx = 0;
        if (tabStart != 0) {
            long e = now - tabStart;
            if (e < SLIDE_MS) {
                float t = Ui.ease(e / (float) SLIDE_MS);
                gva = 1f - t;
                gvx = Math.round(-tabDir * slide * t);
            } else if (e < SLIDE_MS * 2L) {
                if (shownTab != activeTab) { shownTab = activeTab; scroll = 0; }
                float t = Ui.ease((e - SLIDE_MS) / (float) SLIDE_MS);
                gva = t;
                gvx = Math.round(tabDir * slide * (1f - t));
            } else {
                shownTab = activeTab;
                tabStart = 0;
            }
        }

        boolean canHover = interactive(now);

        // background dim + window
        ctx.fill(0, 0, this.width, this.height, 0x4D000000);
        Ui.window(ctx, px, py, pw, ph);

        ctx.enableScissor(px, py, px + pw, py + ph);
        drawHeader(ctx, mouseX, mouseY, canHover);
        drawNav(ctx, mouseX, mouseY, canHover, now);
        drawGrid(ctx, mouseX, mouseY, canHover, dt, va * gva, vx + gvx);
        ctx.disableScissor();

        drawSplash(ctx, now);

        if (pendingAction != null) {
            Runnable r = pendingAction;
            pendingAction = null;
            leaveAction = null;
            r.run();
        }
    }

    private void drawHeader(DrawContext ctx, int mx, int my, boolean canHover) {
        int logo = Ui.u(22);
        Ui.tex(ctx, Ui.LOGO, gridX + vx, py + padY, logo, 128, va);

        int cx = customizeX();
        int cy = py + padY + logo / 2;
        int w = Ui.textW(this.textRenderer, customizeLabel(), 11);
        boolean hov = canHover && Ui.in(mx, my, cx - 2, cy - 7, w + 4, 14);
        Ui.text(ctx, this.textRenderer, customizeLabel(), cx + vx, cy, 11,
                Ui.alpha(hov ? Ui.TEXT : Ui.MUTED, va));
    }

    private void drawNav(DrawContext ctx, int mx, int my, boolean canHover, long now) {
        int ny = navY();
        int nh = navH();
        int cy = ny + nh / 2;

        // tabs
        int[] xs = tabXs();
        for (int i = 0; i < TABS.length; i++) {
            int w = Ui.textW(this.textRenderer, TABS[i], 12);
            boolean hov = canHover && Ui.in(mx, my, xs[i], ny, w, nh);
            boolean active = i == activeTab;
            int col = (active || hov) ? Ui.TEXT : Ui.MUTED;
            Ui.text(ctx, this.textRenderer, TABS[i], xs[i] + vx, cy, 12, Ui.alpha(col, va));
            if (active) {
                ctx.fill(xs[i] + vx, cy + Ui.u(13), xs[i] + w + vx, cy + Ui.u(13) + 1, Ui.alpha(Ui.TEXT, va));
            }
        }

        // search bar
        int sx = searchX() + vx;
        int sw = searchW();
        Ui.rrect(ctx, sx, ny, sw, nh, Ui.u(4), Ui.alpha(Ui.CARD, va));

        int tx = sx + Ui.u(12);
        if (query.isEmpty()) {
            Ui.text(ctx, this.textRenderer, "Search mods", tx, cy, 12, Ui.alpha(Ui.MUTED, va));
        } else {
            Ui.text(ctx, this.textRenderer, query, tx, cy, 12, Ui.alpha(Ui.TEXT, va));
        }
        if (searchFocused && (now / 500) % 2 == 0) {
            int cw = Ui.textW(this.textRenderer, query, 12);
            ctx.fill(tx + cw + 1, cy - 3, tx + cw + 2, cy + 4, Ui.alpha(Ui.TEXT, va));
        }
        int isz = Ui.u(11);
        Ui.icon(ctx, "magnifying-glass", sx + sw - Ui.u(12) - isz, cy - isz / 2, isz, 0.55f * va);
    }

    private void drawGrid(DrawContext ctx, int mx, int my, boolean canHover, float dt, float a, int ox) {
        List<Card> cards = layoutCards();

        ctx.enableScissor(Math.max(px, gridX + ox), gridY, Math.min(px + pw, gridX + gridW + ox), gridY + gridH);

        int pad = Ui.u(12);
        int iconSz = Ui.u(15);
        int arrowSz = Ui.u(14);
        int arrowBox = arrowSz + Ui.u(8);
        boolean mouseInGrid = Ui.in(mx, my, gridX, gridY, gridW, gridH);

        for (Card c : cards) {
            Module m = c.mod;
            float target = m.on() ? 1f : 0f;
            float step = dt / 300f;
            m.anim += Math.max(-step, Math.min(step, target - m.anim));

            int x = c.x + ox;
            int y = c.y;
            if (y + cardH < gridY || y > gridY + gridH) continue;

            boolean hov = canHover && mouseInGrid && Ui.in(mx, my, c.x, c.y, cardW, cardH);
            Ui.rrect(ctx, x, y, cardW, cardH, Ui.u(4), Ui.alpha(hov ? Ui.CARD_HOV : Ui.CARD, a));

            // icon (opacity .55, 1.0 when hovered)
            int ix = x + pad;
            int iy = y + pad;
            boolean iconHov = canHover && mouseInGrid && Ui.in(mx, my, c.x + pad, c.y + pad, iconSz, iconSz);
            Ui.tex(ctx, m.icon, ix, iy, iconSz, 32, (iconHov ? 1f : 0.55f) * a);

            // toggle (+ chevron)
            int right = x + cardW - pad;
            if (m.open != null) {
                boolean ah = canHover && mouseInGrid && Ui.in(mx, my, c.x + cardW - pad - arrowBox, c.y + pad, arrowBox, iconSz);
                int asz = arrowSz;
                Ui.tex(ctx, Ui.icon("chevron-right"), right - arrowBox + (arrowBox - asz) / 2,
                        iy + (iconSz - asz) / 2, asz, 32, (ah ? 1f : 0.55f) * a);
                right -= arrowBox + Ui.u(8);
            }
            Ui.toggle(ctx, right - Ui.TOGGLE_W, iy + (iconSz - Ui.TOGGLE_H) / 2, m.anim, a);

            // name (bottom-left)
            float sc = Ui.scale(14);
            int nameCy = y + cardH - pad - Ui.u(3) - Math.round(3.5f * sc);
            Ui.text(ctx, this.textRenderer, m.name, x + pad, nameCy, 14, Ui.alpha(Ui.TEXT, a));
        }

        // thin scrollbar
        if (maxScroll > 0) {
            int thumbH = Math.max(Ui.u(30), gridH * gridH / contentH);
            int thumbY = gridY + (int) ((gridH - thumbH) * (scroll / (float) maxScroll));
            Ui.rrect(ctx, gridX + gridW - Ui.u(4) + ox, thumbY, Ui.u(4), thumbH, Ui.u(2), Ui.alpha(0x1AFFFFFF, a));
        }

        ctx.disableScissor();
    }

    private void drawSplash(DrawContext ctx, long now) {
        long t = now - openTime;
        if (t >= INTRO_MS) return;
        float a = t < INTRO_LOAD ? 1f : 1f - (t - INTRO_LOAD) / (float) INTRO_FADE;

        ctx.fill(0, 0, this.width, this.height, Ui.alpha(0xFF050505, a));

        int logo = Ui.u(64);
        int barW = Ui.u(140);
        int barH = Ui.u(3);
        int margin = Ui.u(25);
        int total = logo + margin + barH;
        int ly = (this.height - total) / 2;
        int lx = (this.width - logo) / 2;
        Ui.tex(ctx, Ui.LOGO, lx, ly, logo, 128, a);

        int bx = (this.width - barW) / 2;
        int by = ly + logo + margin;
        float p = Math.min(1f, t / (float) INTRO_LOAD);
        p = 1f - (float) Math.pow(1f - p, 4); // ease-out
        ctx.fill(bx, by, bx + barW, by + barH, Ui.alpha(0xFF222222, a));
        ctx.fill(bx, by, bx + Math.round(barW * p), by + barH, Ui.alpha(Ui.ACCENT, a));
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
    public boolean mouseClicked(double mx, double my, int button) {
        long now = Util.getMeasuringTimeMs();
        if (!interactive(now)) return true;
        if (button != 0) return super.mouseClicked(mx, my, button);

        // customize (HUD editor)
        int cy = py + padY + Ui.u(22) / 2;
        int cw = Ui.textW(this.textRenderer, customizeLabel(), 11);
        if (Ui.in(mx, my, customizeX() - 2, cy - 7, cw + 4, 14)) {
            if (this.client != null) this.client.setScreen(new HudEditScreen(this));
            return true;
        }

        // tabs
        int ny = navY();
        int[] xs = tabXs();
        for (int i = 0; i < TABS.length; i++) {
            int w = Ui.textW(this.textRenderer, TABS[i], 12);
            if (Ui.in(mx, my, xs[i], ny, w, navH())) {
                searchFocused = false;
                if (i != activeTab) {
                    tabDir = i > activeTab ? 1 : -1;
                    activeTab = i;
                    tabStart = now;
                }
                return true;
            }
        }

        // search
        if (Ui.in(mx, my, searchX(), ny, searchW(), navH())) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;

        // scrollbar drag
        if (maxScroll > 0 && Ui.in(mx, my, gridX + gridW - Ui.u(4) - 2, gridY, Ui.u(4) + 4, gridH)) {
            draggingSb = true;
            dragStartMy = my;
            dragStartScroll = scroll;
            return true;
        }

        // cards
        if (Ui.in(mx, my, gridX, gridY, gridW, gridH)) {
            int pad = Ui.u(12);
            int iconSz = Ui.u(15);
            int arrowBox = Ui.u(14) + Ui.u(8);
            for (Card c : layoutCards()) {
                if (!Ui.in(mx, my, c.x, c.y, cardW, cardH)) continue;
                Module m = c.mod;
                int right = c.x + cardW - pad;
                if (m.open != null) {
                    if (Ui.in(mx, my, right - arrowBox, c.y + pad, arrowBox, iconSz)) {
                        m.open.run();
                        return true;
                    }
                    right -= arrowBox + Ui.u(8);
                }
                if (Ui.in(mx, my, right - Ui.TOGGLE_W - 2, c.y + pad, Ui.TOGGLE_W + 4, iconSz)) {
                    m.toggle.run();
                } else if (m.open != null) {
                    m.open.run();
                } else {
                    m.toggle.run();
                }
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (draggingSb && maxScroll > 0) {
            int thumbH = Math.max(Ui.u(30), gridH * gridH / contentH);
            float ratio = maxScroll / (float) Math.max(1, gridH - thumbH);
            scroll = Math.max(0, Math.min(maxScroll, dragStartScroll + (int) ((my - dragStartMy) * ratio)));
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        draggingSb = false;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!searchFocused) return super.charTyped(chr, modifiers);
        if (chr >= 32 && chr != 127 && query.length() < 24) {
            query += chr;
            scroll = 0;
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        long since = Util.getMeasuringTimeMs() - openTime;
        if (keyCode == 256) { // ESC
            if (searchFocused) { searchFocused = false; return true; }
            this.close();
            return true;
        }
        if (searchFocused) {
            if (keyCode == 259 && !query.isEmpty()) { // backspace
                query = query.substring(0, query.length() - 1);
                scroll = 0;
            }
            return true;
        }
        if (keyCode == 82 && since >= 300) { // R
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() { return false; }
}
