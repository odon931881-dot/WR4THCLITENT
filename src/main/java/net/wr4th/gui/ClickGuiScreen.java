package net.wr4th.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.wr4th.Config;
import net.wr4th.WR4TH;
import net.wr4th.module.Category;
import net.wr4th.module.Module;
import net.wr4th.setting.BoolSetting;
import net.wr4th.setting.ModeSetting;
import net.wr4th.setting.NumSetting;
import net.wr4th.setting.Setting;
import net.wr4th.util.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.*;

/**
 * WR4TH click GUI.
 *  - Left click header: drag panel      - Right click header: collapse / expand
 *  - Left click module: toggle          - Right click module: show settings
 *  - Middle click module: bind a key (ESC / BACKSPACE clears)
 */
public class ClickGuiScreen extends Screen {
    // ---- layout ----
    private static final int W = 114, HEAD = 18, ROW = 15, SROW = 13;

    // ---- palette: clear grey + black ----
    private static final int C_HEAD       = 0xF2000000;
    private static final int C_LINE       = 0xFF3A3A3A;
    private static final int C_MOD        = 0xC8121212;
    private static final int C_MOD_HOVER  = 0xD2202020;
    private static final int C_MOD_ON     = 0xE0303030;
    private static final int C_SET        = 0xD00A0A0A;
    private static final int C_SET_HOVER  = 0xD6161616;
    private static final int C_SLIDER     = 0xFF3C3C3C;
    private static final int C_ACCENT     = 0xFFD8D8D8;
    private static final int C_TEXT_ON    = 0xFFFFFFFF;
    private static final int C_TEXT_OFF   = 0xFF8C8C8C;
    private static final int C_TEXT_SET   = 0xFFB4B4B4;
    private static final int C_VEIL       = 0x70000000;

    // state that survives closing/reopening the GUI
    private static final Map<Category, int[]> POS = new EnumMap<>(Category.class);
    private static final Set<Category> COLLAPSED = EnumSet.noneOf(Category.class);
    private static final Set<Module> EXPANDED = new HashSet<>();

    private record Hit(int x, int y, int w, int h, Category cat, Module module, Setting<?> setting) {
        boolean contains(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    }

    private final List<Hit> hits = new ArrayList<>();
    private Category dragPanel;
    private int dragOffX, dragOffY;
    private NumSetting dragNum;
    private int dragNumX, dragNumW;
    private Module binding;

    public ClickGuiScreen() {
        super(Text.literal("WR4TH"));
        for (Category c : Category.values()) {
            POS.computeIfAbsent(c, k -> new int[]{12 + k.ordinal() * (W + 8), 26});
        }
    }

    @Override public boolean shouldPause() { return false; }

    /** Replace vanilla blur background with a plain dark veil. */
    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, C_VEIL);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        hits.clear();

        ctx.drawText(tr, "WR4TH", 12, 10, 0xFFFFFFFF, false);
        ctx.drawText(tr, "  click gui  |  RMB: settings  |  MMB: bind  |  RSHIFT: close", 12 + tr.getWidth("WR4TH"), 10, C_TEXT_OFF, false);

        for (Category cat : Category.values()) {
            int[] pos = POS.get(cat);
            int x = pos[0], y = pos[1];
            boolean collapsed = COLLAPSED.contains(cat);

            // header
            ctx.fill(x, y, x + W, y + HEAD, C_HEAD);
            ctx.fill(x, y + HEAD - 1, x + W, y + HEAD, C_LINE);
            ctx.fill(x, y, x + 2, y + HEAD, C_ACCENT);
            ctx.drawText(tr, cat.display, x + 8, y + 5, C_TEXT_ON, false);
            ctx.drawText(tr, collapsed ? "+" : "-", x + W - 11, y + 5, C_TEXT_OFF, false);
            hits.add(new Hit(x, y, W, HEAD, cat, null, null));

            int cy = y + HEAD;
            if (collapsed) continue;

            for (Module m : WR4TH.modules().byCategory(cat)) {
                boolean hover = mouseX >= x && mouseX < x + W && mouseY >= cy && mouseY < cy + ROW;
                ctx.fill(x, cy, x + W, cy + ROW, m.isEnabled() ? C_MOD_ON : hover ? C_MOD_HOVER : C_MOD);
                if (m.isEnabled()) ctx.fill(x, cy, x + 2, cy + ROW, C_ACCENT);
                ctx.drawText(tr, m.name, x + 8, cy + 4, m.isEnabled() ? C_TEXT_ON : C_TEXT_OFF, false);

                String right;
                if (binding == m) right = "[...]";
                else if (m.key >= 0) right = "[" + Utils.keyName(m.key) + "]";
                else right = m.settings.isEmpty() ? "" : (EXPANDED.contains(m) ? "v" : ">");
                if (!right.isEmpty()) ctx.drawText(tr, right, x + W - 6 - tr.getWidth(right), cy + 4, C_TEXT_OFF, false);

                hits.add(new Hit(x, cy, W, ROW, cat, m, null));
                cy += ROW;

                if (EXPANDED.contains(m)) {
                    for (Setting<?> s : m.settings) {
                        boolean sHover = mouseX >= x && mouseX < x + W && mouseY >= cy && mouseY < cy + SROW;
                        ctx.fill(x, cy, x + W, cy + SROW, sHover ? C_SET_HOVER : C_SET);
                        ctx.fill(x, cy, x + 1, cy + SROW, C_LINE);
                        drawSetting(ctx, tr, s, x, cy);
                        hits.add(new Hit(x, cy, W, SROW, cat, m, s));
                        cy += SROW;
                    }
                }
            }
            ctx.fill(x, cy, x + W, cy + 1, C_LINE);
        }
    }

    private void drawSetting(DrawContext ctx, TextRenderer tr, Setting<?> s, int x, int y) {
        int ty = y + 3;
        if (s instanceof BoolSetting b) {
            ctx.drawText(tr, b.name, x + 8, ty, C_TEXT_SET, false);
            int bx = x + W - 14;
            ctx.fill(bx, y + 3, bx + 7, y + 10, C_LINE);
            if (b.get()) ctx.fill(bx + 1, y + 4, bx + 6, y + 9, C_ACCENT);
        } else if (s instanceof NumSetting n) {
            int fill = (int) ((W - 2) * n.fraction());
            ctx.fill(x + 1, y, x + 1 + fill, y + SROW, C_SLIDER);
            ctx.fill(x + 1 + fill - 1, y, x + 1 + fill, y + SROW, C_ACCENT);
            ctx.drawText(tr, n.name, x + 8, ty, C_TEXT_SET, false);
            String v = n.display();
            ctx.drawText(tr, v, x + W - 6 - tr.getWidth(v), ty, C_TEXT_ON, false);
        } else if (s instanceof ModeSetting mo) {
            ctx.drawText(tr, mo.name, x + 8, ty, C_TEXT_SET, false);
            String v = mo.get();
            ctx.drawText(tr, v, x + W - 6 - tr.getWidth(v), ty, C_TEXT_ON, false);
        }
    }

    private void updateSlider(double mouseX) {
        if (dragNum == null) return;
        double f = (mouseX - dragNumX) / (double) dragNumW;
        f = Math.max(0, Math.min(1, f));
        dragNum.set(dragNum.min + f * (dragNum.max - dragNum.min));
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        int button = click.button();

        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (!h.contains(mx, my)) continue;

            if (h.setting() != null) {
                Setting<?> s = h.setting();
                if (s instanceof BoolSetting b && button == 0) b.toggle();
                else if (s instanceof ModeSetting m && button == 0) m.cycle();
                else if (s instanceof NumSetting n && button == 0) {
                    dragNum = n;
                    dragNumX = h.x();
                    dragNumW = h.w();
                    updateSlider(mx);
                }
            } else if (h.module() != null) {
                Module m = h.module();
                if (button == 0) m.toggle();
                else if (button == 1 && !m.settings.isEmpty()) {
                    if (!EXPANDED.remove(m)) EXPANDED.add(m);
                } else if (button == 2) binding = (binding == m) ? null : m;
            } else {
                if (button == 0) {
                    dragPanel = h.cat();
                    int[] p = POS.get(h.cat());
                    dragOffX = (int) mx - p[0];
                    dragOffY = (int) my - p[1];
                } else if (button == 1) {
                    if (!COLLAPSED.remove(h.cat())) COLLAPSED.add(h.cat());
                }
            }
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (dragPanel != null) {
            int[] p = POS.get(dragPanel);
            p[0] = (int) click.x() - dragOffX;
            p[1] = (int) click.y() - dragOffY;
            return true;
        }
        if (dragNum != null) {
            updateSlider(click.x());
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        dragPanel = null;
        dragNum = null;
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (binding != null) {
            binding.key = (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_BACKSPACE) ? -1 : key;
            binding = null;
            return true;
        }
        if (key == WR4TH.GUI_KEY) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void removed() {
        Config.save(WR4TH.modules());
        super.removed();
    }
}
