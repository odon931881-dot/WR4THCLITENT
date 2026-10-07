package net.wr4th.modules;

import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.util.Hand;
import net.wr4th.WR4TH;
import net.wr4th.mixin.MinecraftClientAccessor;
import net.wr4th.module.Category;
import net.wr4th.module.Module;
import net.wr4th.setting.NumSetting;
import net.wr4th.util.Utils;

public final class PlayerModules {
    private PlayerModules() {}

    public static class FastPlace extends Module {
        public FastPlace() { super("FastPlace", "Removes the delay between placing blocks", Category.PLAYER); }

        @Override
        public void onTick() { ((MinecraftClientAccessor) mc).wr4th$setItemUseCooldown(0); }
    }

    public static class AutoRespawn extends Module {
        public AutoRespawn() { super("AutoRespawn", "Respawns immediately after dying", Category.PLAYER); }

        @Override
        public void onTick() {
            if (mc.currentScreen instanceof DeathScreen) mc.player.requestRespawn();
        }
    }

    public static class AutoEat extends Module {
        private final NumSetting hunger = add(new NumSetting("Hunger", 14, 1, 19, 1));
        private boolean eating;
        private int previousSlot;

        public AutoEat() { super("AutoEat", "Eats food from your hotbar when hungry", Category.PLAYER); }

        @Override
        public void onTick() {
            ClientPlayerEntity p = mc.player;
            if (mc.currentScreen != null) return;
            int food = p.getHungerManager().getFoodLevel();

            if (eating) {
                if (food >= 20 || !p.getMainHandStack().contains(DataComponentTypes.FOOD)) {
                    stopEating();
                } else {
                    mc.options.useKey.setPressed(true);
                }
                return;
            }
            if (food > hunger.asInt()) return;
            int slot = Utils.findHotbar(s -> s.contains(DataComponentTypes.FOOD));
            if (slot == -1) return;
            previousSlot = p.getInventory().getSelectedSlot();
            p.getInventory().setSelectedSlot(slot);
            eating = true;
        }

        private void stopEating() {
            mc.options.useKey.setPressed(false);
            if (mc.player != null) mc.player.getInventory().setSelectedSlot(previousSlot);
            eating = false;
        }

        @Override
        protected void onDisable() { if (eating) stopEating(); }
    }

    public static class AntiAFK extends Module {
        private int ticks;

        public AntiAFK() { super("AntiAFK", "Moves slightly so you don't get kicked for idling", Category.PLAYER); }

        @Override
        public void onTick() {
            ClientPlayerEntity p = mc.player;
            ticks++;
            if (ticks % 200 == 0) p.swingHand(Hand.MAIN_HAND);
            if (ticks % 400 == 0) p.setYaw(p.getYaw() + 15f);
        }
    }

    /** Wurst-style Panic: turns every module off at once. */
    public static class Panic extends Module {
        public Panic() { super("Panic", "Disables all modules", Category.PLAYER); }

        @Override
        protected void onEnable() {
            for (Module m : WR4TH.modules().all()) {
                if (m != this && m.isEnabled()) m.setEnabled(false);
            }
            setEnabled(false);
        }
    }
}
