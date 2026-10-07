package net.wr4th.module;

import net.minecraft.client.MinecraftClient;
import net.wr4th.WR4TH;
import net.wr4th.setting.Setting;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    public final String name;
    public final String description;
    public final Category category;
    public final List<Setting<?>> settings = new ArrayList<>();

    /** GLFW key code, -1 = unbound. */
    public int key = -1;
    public boolean keyWasDown;

    private boolean enabled;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    protected <T extends Setting<?>> T add(T setting) {
        settings.add(setting);
        return setting;
    }

    public boolean isEnabled() { return enabled; }
    public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean value) {
        if (value == enabled) return;
        enabled = value;
        try {
            if (value) onEnable(); else onDisable();
        } catch (Throwable t) {
            WR4TH.LOG.error("[{}] error in enable/disable", name, t);
        }
    }

    protected void onEnable() {}
    protected void onDisable() {}
    public void onTick() {}

    /** Small suffix shown next to the module in the HUD list. */
    public String getInfo() { return ""; }
    public boolean isHidden() { return false; }
}
