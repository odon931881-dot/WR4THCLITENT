package net.wr4th.modules;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.wr4th.module.Category;
import net.wr4th.module.Module;
import net.wr4th.setting.BoolSetting;

public final class RenderModules {
    private RenderModules() {}

    /** Controls the on-screen overlay. Enabled by default. */
    public static class HudModule extends Module {
        public final BoolSetting watermark = add(new BoolSetting("Watermark", true));
        public final BoolSetting arrayList = add(new BoolSetting("ModuleList", true));
        public final BoolSetting info = add(new BoolSetting("Info", true));

        public HudModule() { super("HUD", "Watermark, module list and info overlay", Category.RENDER); }

        @Override public boolean isHidden() { return true; }
    }

    /** Client-side night vision. */
    public static class Fullbright extends Module {
        public Fullbright() { super("Fullbright", "See clearly in the dark", Category.RENDER); }

        @Override
        public void onTick() {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1000, 0, false, false, false));
        }

        @Override
        protected void onDisable() {
            if (mc.player != null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    /** Highlights entities through walls using the vanilla glow outline. */
    public static class ESP extends Module {
        private final BoolSetting players = add(new BoolSetting("Players", true));
        private final BoolSetting hostiles = add(new BoolSetting("Hostiles", true));
        private final BoolSetting animals = add(new BoolSetting("Animals", false));

        public ESP() { super("ESP", "Outlines entities through walls", Category.RENDER); }

        @Override
        public void onTick() {
            ClientPlayerEntity self = mc.player;
            for (Entity e : mc.world.getEntities()) {
                if (e == self || !(e instanceof LivingEntity)) continue;
                boolean show = (e instanceof PlayerEntity && players.get())
                        || (e instanceof Monster && hostiles.get())
                        || (e instanceof AnimalEntity && animals.get());
                e.setGlowing(show);
            }
        }

        @Override
        protected void onDisable() {
            if (mc.world == null) return;
            for (Entity e : mc.world.getEntities()) {
                if (e != mc.player && e instanceof LivingEntity) e.setGlowing(false);
            }
        }
    }
}
