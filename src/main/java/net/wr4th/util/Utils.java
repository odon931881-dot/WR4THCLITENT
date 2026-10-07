package net.wr4th.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.function.Predicate;

public final class Utils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private Utils() {}

    public static boolean movingForward() { return mc.options.forwardKey.isPressed(); }

    public static boolean isMoving() {
        GameOptions o = mc.options;
        return o.forwardKey.isPressed() || o.backKey.isPressed() || o.leftKey.isPressed() || o.rightKey.isPressed();
    }

    /** Horizontal velocity {x, z} in the direction the player is moving, for the given speed. */
    public static double[] direction(double speed) {
        ClientPlayerEntity p = mc.player;
        GameOptions o = mc.options;
        double forward = (o.forwardKey.isPressed() ? 1 : 0) - (o.backKey.isPressed() ? 1 : 0);
        double strafe = (o.leftKey.isPressed() ? 1 : 0) - (o.rightKey.isPressed() ? 1 : 0);
        if (forward == 0 && strafe == 0) return new double[]{0, 0};
        double len = Math.sqrt(forward * forward + strafe * strafe);
        forward /= len;
        strafe /= len;
        double yaw = Math.toRadians(p.getYaw());
        double sin = Math.sin(yaw), cos = Math.cos(yaw);
        return new double[]{(forward * -sin + strafe * cos) * speed, (forward * cos + strafe * sin) * speed};
    }

    public static void face(Entity target) {
        ClientPlayerEntity p = mc.player;
        double dx = target.getX() - p.getX();
        double dy = (target.getY() + target.getHeight() * 0.5) - p.getEyeY();
        double dz = target.getZ() - p.getZ();
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));
        p.setYaw(yaw);
        p.setPitch(MathHelper.clamp(pitch, -90f, 90f));
    }

    /** Finds a hotbar slot (0-8) whose stack matches, or -1. */
    public static int findHotbar(Predicate<ItemStack> filter) {
        for (int i = 0; i < 9; i++) {
            if (filter.test(mc.player.getInventory().getStack(i))) return i;
        }
        return -1;
    }

    public static String keyName(int key) {
        if (key < 0) return "";
        switch (key) {
            case GLFW.GLFW_KEY_RIGHT_SHIFT: return "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_SHIFT: return "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_CONTROL: return "RCTRL";
            case GLFW.GLFW_KEY_LEFT_CONTROL: return "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_ALT: return "RALT";
            case GLFW.GLFW_KEY_LEFT_ALT: return "LALT";
            case GLFW.GLFW_KEY_SPACE: return "SPACE";
            case GLFW.GLFW_KEY_TAB: return "TAB";
            default:
                String n = GLFW.glfwGetKeyName(key, 0);
                return n != null ? n.toUpperCase() : "#" + key;
        }
    }
}
