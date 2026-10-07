package net.wr4th.modules;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import net.wr4th.module.Category;
import net.wr4th.module.Module;
import net.wr4th.setting.BoolSetting;
import net.wr4th.setting.NumSetting;
import net.wr4th.util.Utils;

public final class MovementModules {
    private MovementModules() {}

    public static class Flight extends Module {
        private final NumSetting speed = add(new NumSetting("Speed", 1.0, 0.1, 5.0, 0.1));
        private final BoolSetting antiKick = add(new BoolSetting("AntiKick", true));
        private int ticks;

        public Flight() { super("Flight", "Fly like in creative mode", Category.MOVEMENT); }

        @Override
        public void onTick() {
            ClientPlayerEntity p = mc.player;
            double y = 0;
            if (mc.options.jumpKey.isPressed()) y += speed.get();
            if (mc.options.sneakKey.isPressed()) y -= speed.get();
            if (antiKick.get() && y == 0 && ++ticks % 30 == 0) y = -0.06;
            double[] h = Utils.direction(speed.get());
            p.setVelocity(h[0], y, h[1]);
        }

        @Override public String getInfo() { return speed.display(); }
    }

    public static class Speed extends Module {
        private final NumSetting speed = add(new NumSetting("Speed", 0.45, 0.1, 2.0, 0.01));
        private final BoolSetting onlyGround = add(new BoolSetting("OnlyGround", false));

        public Speed() { super("Speed", "Move faster on the ground", Category.MOVEMENT); }

        @Override
        public void onTick() {
            ClientPlayerEntity p = mc.player;
            if (!Utils.isMoving() || (onlyGround.get() && !p.isOnGround())) return;
            double[] h = Utils.direction(speed.get());
            p.setVelocity(h[0], p.getVelocity().y, h[1]);
        }

        @Override public String getInfo() { return speed.display(); }
    }

    public static class BunnyHop extends Module {
        public BunnyHop() { super("BunnyHop", "Automatically jumps while moving", Category.MOVEMENT); }

        @Override
        public void onTick() {
            ClientPlayerEntity p = mc.player;
            if (p.isOnGround() && Utils.isMoving() && !p.isSneaking()) {
                Vec3d v = p.getVelocity();
                p.setVelocity(v.x, 0.42, v.z);
            }
        }
    }

    public static class Sprint extends Module {
        public Sprint() { super("Sprint", "Always sprint while moving forward", Category.MOVEMENT); }

        @Override
        public void onTick() {
            ClientPlayerEntity p = mc.player;
            if (Utils.movingForward() && !p.horizontalCollision && !p.isSneaking()
                    && p.getHungerManager().getFoodLevel() > 6) {
                p.setSprinting(true);
            }
        }
    }

    public static class NoFall extends Module {
        public NoFall() { super("NoFall", "Prevents fall damage", Category.MOVEMENT); }

        @Override
        public void onTick() {
            ClientPlayerEntity p = mc.player;
            if (!p.isOnGround() && p.getVelocity().y < -0.5 && !p.isGliding()) {
                p.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, p.horizontalCollision));
            }
        }
    }

    public static class Step extends Module {
        private final NumSetting height = add(new NumSetting("Height", 1.0, 0.6, 5.0, 0.1));

        public Step() { super("Step", "Walk up blocks without jumping", Category.MOVEMENT); }

        @Override
        public void onTick() {
            EntityAttributeInstance a = mc.player.getAttributeInstance(EntityAttributes.STEP_HEIGHT);
            if (a != null) a.setBaseValue(height.get());
        }

        @Override
        protected void onDisable() {
            if (mc.player == null) return;
            EntityAttributeInstance a = mc.player.getAttributeInstance(EntityAttributes.STEP_HEIGHT);
            if (a != null) a.setBaseValue(0.6);
        }

        @Override public String getInfo() { return height.display(); }
    }

    public static class Spider extends Module {
        private final NumSetting speed = add(new NumSetting("Speed", 0.2, 0.05, 0.6, 0.01));

        public Spider() { super("Spider", "Climb walls like a spider", Category.MOVEMENT); }

        @Override
        public void onTick() {
            ClientPlayerEntity p = mc.player;
            if (p.horizontalCollision) {
                Vec3d v = p.getVelocity();
                p.setVelocity(v.x, speed.get(), v.z);
            }
        }
    }

    public static class Jesus extends Module {
        public Jesus() { super("Jesus", "Walk on liquids", Category.MOVEMENT); }

        @Override
        public void onTick() {
            ClientPlayerEntity p = mc.player;
            if (mc.options.sneakKey.isPressed()) return;
            if (p.isTouchingWater() || p.isInLava()) {
                Vec3d v = p.getVelocity();
                p.setVelocity(v.x, 0.11, v.z);
            }
        }
    }

    public static class AirJump extends Module {
        private boolean wasJumping;

        public AirJump() { super("AirJump", "Jump again while in mid-air", Category.MOVEMENT); }

        @Override
        public void onTick() {
            ClientPlayerEntity p = mc.player;
            boolean jumping = mc.options.jumpKey.isPressed();
            if (jumping && !wasJumping && !p.isOnGround() && !p.isTouchingWater()) {
                Vec3d v = p.getVelocity();
                p.setVelocity(v.x, 0.42, v.z);
            }
            wasJumping = jumping;
        }
    }

    public static class AutoWalk extends Module {
        public AutoWalk() { super("AutoWalk", "Holds the forward key for you", Category.MOVEMENT); }

        @Override public void onTick() { mc.options.forwardKey.setPressed(true); }

        @Override
        protected void onDisable() { mc.options.forwardKey.setPressed(false); }
    }
}
