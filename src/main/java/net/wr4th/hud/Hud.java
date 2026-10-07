package net.wr4th.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.wr4th.WR4TH;
import net.wr4th.module.Module;
import net.wr4th.modules.RenderModules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class Hud {
    public static final int BG = 0xB0000000;
    public static final int ACCENT = 0xFFD0D0D0;
    public static final int TEXT = 0xFFF0F0F0;
    public static final int MUTED = 0xFF8C8C8C;

    private Hud() {}

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        RenderModules.HudModule hud = WR4TH.modules().get(RenderModules.HudModule.class);
        if (hud == null || !hud.isEnabled()) return;
        TextRenderer tr = mc.textRenderer;

        if (hud.watermark.get()) {
            String name = "WR4TH";
            String ver = " " + WR4TH.VERSION;
            int w = tr.getWidth(name) + tr.getWidth(ver) + 10;
            ctx.fill(4, 4, 4 + w, 18, BG);
            ctx.fill(4, 4, 6, 18, ACCENT);
            ctx.drawText(tr, name, 10, 7, 0xFFFFFFFF, false);
            ctx.drawText(tr, ver, 10 + tr.getWidth(name), 7, MUTED, false);
        }

        if (hud.arrayList.get()) {
            List<Module> active = new ArrayList<>();
            for (Module m : WR4TH.modules().all()) if (m.isEnabled() && !m.isHidden()) active.add(m);
            active.sort(Comparator.comparingInt((Module m) -> -width(tr, m)));

            int screenW = ctx.getScaledWindowWidth();
            int y = 4;
            for (Module m : active) {
                int w = width(tr, m) + 8;
                int x = screenW - w - 2;
                ctx.fill(x, y, x + w, y + 11, BG);
                ctx.fill(x + w - 2, y, x + w, y + 11, ACCENT);
                ctx.drawText(tr, m.name, x + 3, y + 2, TEXT, false);
                String info = m.getInfo();
                if (!info.isEmpty()) ctx.drawText(tr, info, x + 3 + tr.getWidth(m.name) + 4, y + 2, MUTED, false);
                y += 11;
            }
        }

        if (hud.info.get()) {
            String s = String.format("FPS %d   XYZ %d %d %d", mc.getCurrentFps(),
                    (int) Math.floor(mc.player.getX()), (int) Math.floor(mc.player.getY()), (int) Math.floor(mc.player.getZ()));
            int h = ctx.getScaledWindowHeight();
            int w = tr.getWidth(s) + 8;
            ctx.fill(4, h - 16, 4 + w, h - 4, BG);
            ctx.fill(4, h - 16, 6, h - 4, ACCENT);
            ctx.drawText(tr, s, 10, h - 14, TEXT, false);
        }
    }

    private static int width(TextRenderer tr, Module m) {
        String info = m.getInfo();
        return tr.getWidth(m.name) + (info.isEmpty() ? 0 : 4 + tr.getWidth(info));
    }
}
