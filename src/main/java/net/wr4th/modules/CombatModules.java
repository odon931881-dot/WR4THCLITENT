package net.wr4th.modules;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.wr4th.module.Category;
import net.wr4th.module.Module;
import net.wr4th.setting.BoolSetting;
import net.wr4th.setting.NumSetting;
import net.wr4th.util.Utils;

public final class CombatModules {
    private CombatModules() {}

    /** Wurst-style KillAura: hits the closest valid entity in range. */
    public static class KillAura extends Module {
        private final NumSetting range = add(new NumSetting("Range", 4.2, 1.0, 6.0, 0.1));
        private final BoolSetting players = add(new BoolSetting("Players", true));
        private final BoolSetting hostiles = add(new BoolSetting("Hostiles", true));
        private final BoolSetting animals = add(new BoolSetting("Animals", false));
        private final BoolSetting rotate = add(new BoolSetting("Rotate", true));

        public KillAura() { super("KillAura", "Attacks entities around you", Category.COMBAT); }

        private boolean valid(LivingEntity e) {
            if (e instanceof PlayerEntity) return players.get();
            if (e instanceof Monster) return hostiles.get();
            if (e instanceof AnimalEntity) return animals.get();
            return false;
        }

        @Override
        public void onTick() {
            ClientPlayerEntity p = mc.player;
            if (mc.interactionManager == null || p.getAttackCooldownProgress(0.5f) < 1.0f) return;

            LivingEntity best = null;
            double bestDist = range.get();
            for (Entity e : mc.world.getEntities()) {
                if (e == p || !(e instanceof LivingEntity le) || !le.isAlive() || !valid(le)) continue;
                double d = p.distanceTo(le);
                if (d <= bestDist) {
                    best = le;
                    bestDist = d;
                }
            }
            if (best == null) return;
            if (rotate.get()) Utils.face(best);
            mc.interactionManager.attackEntity(p, best);
            p.swingHand(Hand.MAIN_HAND);
        }

        @Override public String getInfo() { return range.display(); }
    }

    /** Meteor-style TriggerBot: attacks whatever your crosshair is on once the cooldown is ready. */
    public static class TriggerBot extends Module {
        private final BoolSetting players = add(new BoolSetting("Players", true));
        private final BoolSetting mobs = add(new BoolSetting("Mobs", true));

        public TriggerBot() { super("TriggerBot", "Attacks the entity under your crosshair", Category.COMBAT); }

        @Override
        public void onTick() {
            ClientPlayerEntity p = mc.player;
            if (mc.interactionManager == null || mc.currentScreen != null) return;
            if (p.getAttackCooldownProgress(0.5f) < 1.0f) return;
            if (mc.crosshairTarget instanceof EntityHitResult hit
                    && hit.getEntity() instanceof LivingEntity le && le.isAlive()) {
                boolean ok = (le instanceof PlayerEntity && players.get())
                        || (!(le instanceof PlayerEntity) && mobs.get());
                if (!ok) return;
                mc.interactionManager.attackEntity(p, le);
                p.swingHand(Hand.MAIN_HAND);
            }
        }
    }

    /** Cancels knockback. Logic lives in ClientPlayNetworkHandlerMixin. */
    public static class Velocity extends Module {
        public Velocity() { super("Velocity", "Removes knockback", Category.COMBAT); }
    }

    /** Doomsday/Meteor-style AutoTotem: keeps a totem in the offhand. */
    public static class AutoTotem extends Module {
        private final NumSetting delay = add(new NumSetting("Delay", 2, 0, 10, 1));
        private int timer;

        public AutoTotem() { super("AutoTotem", "Keeps a totem of undying in your offhand", Category.COMBAT); }

        @Override
        public void onTick() {
            ClientPlayerEntity p = mc.player;
            if (mc.interactionManager == null || mc.currentScreen != null) return;
            if (p.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) { timer = 0; return; }
            if (timer++ < delay.asInt()) return;
            timer = 0;

            int slot = -1;
            for (int i = 0; i < 36; i++) {
                if (p.getInventory().getStack(i).isOf(Items.TOTEM_OF_UNDYING)) { slot = i; break; }
            }
            if (slot == -1) return;
            // Player screen handler layout: hotbar 0-8 -> 36-44, main inventory 9-35 -> 9-35.
            int screenSlot = slot < 9 ? 36 + slot : slot;
            mc.interactionManager.clickSlot(p.playerScreenHandler.syncId, screenSlot, 40, SlotActionType.SWAP, p);
        }
    }
}
