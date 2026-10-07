package net.wr4th.module;

import net.minecraft.client.MinecraftClient;
import net.wr4th.WR4TH;
import net.wr4th.modules.*;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public final class ModuleManager {
    private final List<Module> modules = new ArrayList<>();
    private final Map<Class<?>, Module> byClass = new HashMap<>();

    public void init() {
        // ---- Combat ----
        register(new CombatModules.KillAura());
        register(new CombatModules.TriggerBot());
        register(new CombatModules.Velocity());
        register(new CombatModules.AutoTotem());
        // ---- Movement ----
        register(new MovementModules.Flight());
        register(new MovementModules.Speed());
        register(new MovementModules.BunnyHop());
        register(new MovementModules.Sprint());
        register(new MovementModules.NoFall());
        register(new MovementModules.Step());
        register(new MovementModules.Spider());
        register(new MovementModules.Jesus());
        register(new MovementModules.AirJump());
        register(new MovementModules.AutoWalk());
        // ---- Player ----
        register(new PlayerModules.FastPlace());
        register(new PlayerModules.AutoRespawn());
        register(new PlayerModules.AutoEat());
        register(new PlayerModules.AntiAFK());
        register(new PlayerModules.Panic());
        // ---- Render ----
        register(new RenderModules.HudModule());
        register(new RenderModules.Fullbright());
        register(new RenderModules.ESP());
        // ---- World ----
        register(new WorldModules.Nuker());
        register(new WorldModules.Scaffold());

        get(RenderModules.HudModule.class).setEnabled(true);
    }

    private void register(Module m) {
        modules.add(m);
        byClass.put(m.getClass(), m);
    }

    public List<Module> all() { return modules; }

    public List<Module> byCategory(Category c) {
        List<Module> out = new ArrayList<>();
        for (Module m : modules) if (m.category == c) out.add(m);
        return out;
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> c) { return (T) byClass.get(c); }

    public boolean isEnabled(Class<? extends Module> c) {
        Module m = byClass.get(c);
        return m != null && m.isEnabled();
    }

    public void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        for (Module m : modules) {
            if (!m.isEnabled()) continue;
            try {
                m.onTick();
            } catch (Throwable t) {
                WR4TH.LOG.error("[{}] crashed, disabling module", m.name, t);
                m.setEnabled(false);
            }
        }
    }

    /** Polls module keybinds (edge triggered). Only fires while no screen is open. */
    public void pollKeys() {
        MinecraftClient mc = MinecraftClient.getInstance();
        long handle = mc.getWindow().getHandle();
        for (Module m : modules) {
            if (m.key < 0) continue;
            boolean down = GLFW.glfwGetKey(handle, m.key) == GLFW.GLFW_PRESS;
            if (down && !m.keyWasDown && mc.currentScreen == null) m.toggle();
            m.keyWasDown = down;
        }
    }
}
