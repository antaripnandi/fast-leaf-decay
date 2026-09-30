package com.antarip.fastleafdecay.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeavesBlock.class)
public abstract class LeavesBlockMixin {

    @Inject(method = "updateShape", at = @At("RETURN"))
    private void fastleafdecay$onUpdateShape(
            BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
            BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
        BlockState result = cir.getReturnValue();
        if (result != null && !level.isClientSide() && result.getBlock() instanceof LeavesBlock && level instanceof ServerLevel serverLevel) {
            if (!result.getValue(LeavesBlock.PERSISTENT) && result.getValue(LeavesBlock.DISTANCE) == 7) {
                level.scheduleTick(pos, result.getBlock(), 2 + serverLevel.getRandom().nextInt(5));
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void fastleafdecay$onTick(
            BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!state.getValue(LeavesBlock.PERSISTENT) && state.getValue(LeavesBlock.DISTANCE) == 7) {
            Block.dropResources(state, level, pos);
            level.removeBlock(pos, false);

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                BlockState neighborState = level.getBlockState(neighbor);
                if (neighborState.getBlock() instanceof LeavesBlock
                        && !neighborState.getValue(LeavesBlock.PERSISTENT)
                        && neighborState.getValue(LeavesBlock.DISTANCE) == 7) {
                    level.scheduleTick(neighbor, neighborState.getBlock(), 2 + random.nextInt(4));
                }
            }
        }
    }
}
