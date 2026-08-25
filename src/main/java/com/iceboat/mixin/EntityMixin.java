package com.iceboat.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 冰上划船 Mixin（目标 {@link Boat}）。
 *
 * 核心功能：让船能从水面平滑地滑上冰面。
 * 原版船的 {@code Entity#maxUpStep()} 为 0（没有“上台阶”能力），
 * 因此船在水里会被同高度冰块的侧面挡住，无法爬上冰面。
 * 本 Mixin 在船处于水中、玩家按住前进键、且船头前方一格（船底高度）是冰时，
 * 给船一个温和的向上速度（每帧最多 0.1 格），使其平滑爬升到冰顶，
 * 之后依靠冰的摩擦力自然滑行，不会“跳”上冰面。
 */
@Mixin(Boat.class)
public abstract class EntityMixin {

    @Shadow
    private Boat.Status status;

    @Shadow
    private boolean inputUp;

    @Inject(method = "floatBoat", at = @At("RETURN"))
    private void iceboat$climbOntoIce(CallbackInfo ci) {
        Boat boat = (Boat) (Object) this;

        // 只在“船在水中”且“玩家按住前进键划桨”时生效
        if (this.status != Boat.Status.IN_WATER) {
            return;
        }
        if (!this.inputUp) {
            return;
        }

        // 船头朝向（玩家划桨前进的方向）
        float yRot = boat.getYRot();
        double dirX = -Mth.sin(yRot * (float) (Math.PI / 180.0));
        double dirZ = Mth.cos(yRot * (float) (Math.PI / 180.0));

        // 探测船体前端一格、位于船底高度处的方块是否为冰
        double reach = boat.getBbWidth() / 2.0 + 0.25;
        BlockPos probe = BlockPos.containing(
            boat.getX() + dirX * reach,
            boat.getBoundingBox().minY,
            boat.getZ() + dirZ * reach
        );
        BlockState state = boat.level().getBlockState(probe);
        if (!state.is(BlockTags.ICE)) {
            return;
        }

        // 需要抬升的高度：船底到冰顶
        double iceTop = probe.getY() + 1.0;
        double hullBottom = boat.getBoundingBox().minY;
        double needed = iceTop - hullBottom;
        if (needed <= 0.0) {
            return;
        }

        // 温和抬升：每帧最多 0.1 格，且直接覆盖竖直速度，不继承之前的高速
        double lift = Math.min(needed, 0.1);
        Vec3 motion = boat.getDeltaMovement();
        boat.setDeltaMovement(motion.x, lift, motion.z);
    }
}
