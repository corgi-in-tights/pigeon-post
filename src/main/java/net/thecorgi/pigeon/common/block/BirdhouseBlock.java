package net.thecorgi.pigeon.common.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BirdhouseBlock extends HorizontalFacingBlock implements BlockEntityProvider {
    public static final BooleanProperty POWERED;

    static {
        POWERED = Properties.POWERED;
    }

    public BirdhouseBlock(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH).with(POWERED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.HORIZONTAL_FACING, POWERED);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing().getOpposite()).with(POWERED, false);
    }


    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BirdhouseBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient)
            if (player.getActiveHand().equals(hand) && !player.isSneaking()) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof BirdhouseBlockEntity birdHouse) {
                     if (birdHouse.hasPigeon()) {
                        birdHouse.tryReleasePigeon(state, player);
                    }
                    return ActionResult.SUCCESS;
                }
            }

        return ActionResult.PASS;
    }

    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof BirdhouseBlockEntity birdHouse) {
                if (birdHouse.hasPigeon()) {
                    birdHouse.tryReleasePigeon(state, player);
                }
            }
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        this.updateEnabled(world, pos, state);
    }

    private void updateEnabled(World world, BlockPos pos, BlockState state) {
        boolean bl = !world.isReceivingRedstonePower(pos);
        if (bl != (Boolean)state.get(POWERED)) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof BirdhouseBlockEntity) {
                world.setBlockState(pos, (BlockState) state.with(POWERED, bl), 4);
            }
        }
    }

    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        if(world.getBlockEntity(pos) instanceof BirdhouseBlockEntity birdHouse) {
            if (birdHouse.hasPigeon()) {
                return 6;
            }
        }

        return 0;
    }

}