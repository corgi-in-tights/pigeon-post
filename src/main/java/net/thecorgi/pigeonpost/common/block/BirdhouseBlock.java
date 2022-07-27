package net.thecorgi.pigeonpost.common.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.thecorgi.pigeonpost.common.entity.PigeonEntity;
import net.thecorgi.pigeonpost.common.registry.ItemRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import static net.thecorgi.pigeonpost.common.item.envelope.EnvelopeItem.*;

public class BirdhouseBlock extends BlockWithEntity {
    public static final BooleanProperty POWERED;
    public static final DirectionProperty FACING;

    static {
        POWERED = Properties.POWERED;
        FACING = Properties.HORIZONTAL_FACING;
    }

    public BirdhouseBlock(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH).with(POWERED, false));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BirdhouseBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient && hand.equals(player.getActiveHand())) {
            ItemStack stack = player.getStackInHand(player.getActiveHand());

            // validate
            BlockEntity blockEntityA = world.getBlockEntity(pos);
            if (blockEntityA instanceof BirdhouseBlockEntity birdhouseA) {
                System.out.println("IS ON COOLDOWN? " + birdhouseA.isOnCooldown());
                if (birdhouseA.isOnCooldown()) return ActionResult.SUCCESS;

                if (stack.isOf(ItemRegistry.ENVELOPE)) { // sending item
                    NbtCompound envelope = stack.getOrCreateNbt();

                    if (envelope.contains("Items") && envelope.contains("Address")) {
                        BlockPos newPosition = BlockPos.fromLong(envelope.getLong(ADDRESS_KEY));
                        if (newPosition.equals(pos)) return ActionResult.SUCCESS; // end if same

                        BlockEntity blockEntityB = world.getBlockEntity(newPosition);
                        if (blockEntityB instanceof BirdhouseBlockEntity birdhouseB && !birdhouseB.hasStoredItems()) {
                            // lazy but effective
                            birdhouseB.recieveEnvelope(envelope.getList(ITEMS_KEY, 10));
                            stack.decrement(stack.getCount());

                            // an artificial cool down where 1 second is 1 block travelled
                            // gets squared distance between A and B then rounds
                            birdhouseA.setOnCooldown(true);
                            int delay = (int) Math.round(Math.sqrt(pos.getSquaredDistance(newPosition, true))) * 20;
                            world.createAndScheduleBlockTick(pos, this, delay);
                            System.out.println("TICK: Starting pigeon :I");
                        }
                    }
                } else {
                    if (birdhouseA.hasStoredItems()) { // retrieving stored items
                        // if there is a recipient and if player isnt it, return
                        // cleaner than checking if there is and it is
                        if (!Objects.equals(birdhouseA.getRecipient(), "")) {
                            if (!Objects.equals(birdhouseA.getRecipient(), player.getName().toString())) {
                                return ActionResult.SUCCESS;
                            }
                        }
                        birdhouseA.giveEnvelope(player);

                    } else if (birdhouseA.hasPigeon() && birdhouseA.isPigeonOwner(player)) { // trying to release pigeon
                        System.out.println("RELEASING: " + birdhouseA.isOnCooldown());
                        birdhouseA.tryReleasePigeon(state, player);
                    } else if (player.hasPassengers()) { // trying to put pigeon
                        List<Entity> entities = player.getPassengerList();
                        for (Entity entity : entities) {
                            if (entity instanceof PigeonEntity pigeon) {
                                birdhouseA.enterBirdhouse(pigeon);
                            }
                        }
                    }
                }
            }
        }

        // so the godforsaken item stops opening the gui
        // there's definitely a better way for this
        return ActionResult.SUCCESS;
        // ..though i don't really care
    }

    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof BirdhouseBlockEntity birdhouse) {
                if (birdhouse.hasPigeon()) {
                    birdhouse.tryReleasePigeon(state, player); // drop items & pigeon
                }
            }
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        System.out.println("TICK: Returned pigeon :)");

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof BirdhouseBlockEntity birdhouse) {
            birdhouse.setOnCooldown(false);
        }
    }

    // for comparators
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        this.updateEnabled(world, pos, state);
    }
    private void updateEnabled(World world, BlockPos pos, BlockState state) {
        boolean bl = !world.isReceivingRedstonePower(pos);
        if (bl != state.get(POWERED)) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof BirdhouseBlockEntity) {
                world.setBlockState(pos, state.with(POWERED, bl), 4);
            }
        }
    }
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        if(world.getBlockEntity(pos) instanceof BirdhouseBlockEntity birdhouse) {
            if (birdhouse.hasPigeon()) {
                return 6;
            }
        }
        return 0;
    }

    // blockstate stuff
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate(state.get(FACING)));
    }
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(FACING, POWERED);
    }
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing().getOpposite());
    }

    // required for geckolib
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }
}
