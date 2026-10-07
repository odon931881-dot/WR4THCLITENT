package net.wr4th.modules;

import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.wr4th.module.Category;
import net.wr4th.module.Module;
import net.wr4th.setting.NumSetting;
import net.wr4th.util.Utils;

public final class WorldModules {
    private WorldModules() {}

    /** Wurst-style Nuker: breaks the closest block in range each tick. */
    public static class Nuker extends Module {
        private final NumSetting radius = add(new NumSetting("Radius", 4, 1, 6, 1));

        public Nuker() { super("Nuker", "Breaks blocks around you", Category.WORLD); }

        @Override
        public void onTick() {
            ClientPlayerEntity p = mc.player;
            if (mc.interactionManager == null || mc.currentScreen != null) return;
            int r = radius.asInt();
            BlockPos base = p.getBlockPos();
            BlockPos best = null;
            double bestDist = Double.MAX_VALUE;

            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos pos = base.add(x, y, z);
                        BlockState state = mc.world.getBlockState(pos);
                        if (state.isAir() || !state.getFluidState().isEmpty()) continue;
                        if (state.getHardness(mc.world, pos) < 0) continue; // bedrock etc.
                        double d = p.squaredDistanceTo(Vec3d.ofCenter(pos));
                        if (d > r * r || d >= bestDist) continue;
                        best = pos;
                        bestDist = d;
                    }
                }
            }
            if (best != null) {
                mc.interactionManager.updateBlockBreakingProgress(best, Direction.UP);
                p.swingHand(Hand.MAIN_HAND);
            }
        }

        @Override public String getInfo() { return radius.display(); }
    }

    /** Meteor-style Scaffold: places blocks beneath your feet. */
    public static class Scaffold extends Module {
        public Scaffold() { super("Scaffold", "Places blocks under you as you walk", Category.WORLD); }

        @Override
        public void onTick() {
            ClientPlayerEntity p = mc.player;
            if (mc.interactionManager == null || mc.currentScreen != null) return;

            BlockPos target = p.getBlockPos().down();
            if (!mc.world.getBlockState(target).isReplaceable()) return;

            int slot = Utils.findHotbar(s -> s.getItem() instanceof BlockItem);
            if (slot == -1) return;

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = target.offset(dir);
                if (mc.world.getBlockState(neighbor).isReplaceable()) continue;

                Direction face = dir.getOpposite();
                Vec3d hitPos = Vec3d.ofCenter(neighbor).add(face.getOffsetX() * 0.5, face.getOffsetY() * 0.5, face.getOffsetZ() * 0.5);
                BlockHitResult hit = new BlockHitResult(hitPos, face, neighbor, false);

                int previous = p.getInventory().getSelectedSlot();
                p.getInventory().setSelectedSlot(slot);
                mc.interactionManager.interactBlock(p, Hand.MAIN_HAND, hit);
                p.swingHand(Hand.MAIN_HAND);
                p.getInventory().setSelectedSlot(previous);
                return;
            }
        }
    }
}
