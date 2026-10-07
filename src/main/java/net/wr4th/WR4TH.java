package net.wr4th;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.wr4th.gui.ClickGuiScreen;
import net.wr4th.hud.Hud;
import net.wr4th.module.ModuleManager;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WR4TH implements ClientModInitializer {
    public static final String MOD_ID = "wr4th";
    public static final String NAME = "WR4TH";
    public static final String VERSION = "1.0.0";
    public static final Logger LOG = LoggerFactory.getLogger(NAME);

    /** Key that opens the click GUI. */
    public static final int GUI_KEY = GLFW.GLFW_KEY_RIGHT_SHIFT;

    private static ModuleManager modules;
    private static boolean guiKeyWasDown;

    public static ModuleManager modules() { return modules; }

    @Override
    public void onInitializeClient() {
        modules = new ModuleManager();
        modules.init();
        Config.load(modules);

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.getWindow() == null) return;
            boolean down = GLFW.glfwGetKey(mc.getWindow().getHandle(), GUI_KEY) == GLFW.GLFW_PRESS;
            if (down && !guiKeyWasDown && mc.currentScreen == null && mc.player != null) {
                mc.setScreen(new ClickGuiScreen());
            }
            guiKeyWasDown = down;

            modules.pollKeys();
            modules.tick();
        });

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.of(MOD_ID, "hud"),
                Hud::render);

        LOG.info("{} {} loaded - press RIGHT SHIFT in-game to open the click GUI", NAME, VERSION);
    }
}
