package net.thecorgi.pigeonpost.common.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.thecorgi.pigeonpost.common.entity.PigeonEntity;
import net.thecorgi.pigeonpost.common.registry.BlockRegistry;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.thecorgi.pigeonpost.common.item.envelope.EnvelopeItem.offerOrDropEnvelope;

public class BirdhouseBlockEntity extends BlockEntity implements IAnimatable {
    private final AnimationFactory factory = new AnimationFactory(this);
    private Pigeon pigeon;
    private NbtList storedItems = new NbtList();
    private String recipient = "";
    private boolean onCooldown = false;
    private static final List<String> IRRELEVANT_PIGEON_NBT_KEYS = Arrays.asList("Air", "ArmorDropChances", "ArmorItems", "Brain", "CanPickUpLoot", "DeathTime", "FallDistance", "FallFlying", "Fire", "HandDropChances", "HandItems", "HurtByTimestamp", "HurtTime", "LeftHanded", "Motion", "NoGravity", "OnGround", "PortalCooldown", "Pos", "Rotation", "Passengers", "Leash", "UUID");

    public BirdhouseBlockEntity(BlockPos pos, BlockState state) {
        super(BlockRegistry.BIRDHOUSE_BLOCK_ENTITY, pos, state);
    }

    public boolean enterBirdhouse(Entity pigeon) {
        if (!this.hasPigeon()) {
            pigeon.stopRiding();
            pigeon.removeAllPassengers();

            NbtCompound nbtCompound = new NbtCompound();
            pigeon.saveNbt(nbtCompound);
            this.setPigeon(nbtCompound);

            if (this.world != null) {
                BlockPos blockPos = this.getPos();
                this.world.playSound(null, blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.BLOCK_BEEHIVE_ENTER, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
            pigeon.discard();
            this.updateListeners();

            return true;
        }
        return false;
    }

    public boolean tryReleasePigeon(BlockState state, PlayerEntity player) {
        if (pigeon != null && world != null) {
            releasePigeon(this.world, this.pos, state, player);
            this.pigeon = null;
            this.setStoredItems(new NbtList());
            this.updateListeners();

            return true;
        }
        return false;
    }

    public void recieveEnvelope(NbtList items) {
        this.setStoredItems(items);
        this.updateListeners();
    }

    public void giveEnvelope(PlayerEntity player) {
        offerOrDropEnvelope(this.getStoredItems(), player);
        this.setStoredItems(new NbtList());
        this.updateListeners();
    }

    private boolean releasePigeon(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        NbtCompound nbtCompound = this.getPigeonData();
        removeIrrelevantNbtKeys(nbtCompound);

        offerOrDropEnvelope(this.getStoredItems(), player);

        Direction direction = state.get(BirdhouseBlock.FACING);
        BlockPos exitPos = pos.offset(direction);

        boolean bl = !world.getBlockState(exitPos).getCollisionShape(world, exitPos).isEmpty();
        if (bl) {
            return false;
        } else {
            Optional<Entity> entity = EntityType.getEntityFromNbt(nbtCompound, world);
            if (entity.isPresent()) {
                if (entity.get() instanceof PigeonEntity pigeonEntity) {
                    // adjusting for release (rotations and positions)
                    float f = pigeonEntity.getWidth();
                    double d = 0.55D + (double)(f / 2.0F);
                    double e = (double)pos.getX() + 0.5D + d * (double)direction.getOffsetX();
                    double g = (double)pos.getY() + 0.5D - (double)(pigeonEntity.getHeight() / 2.0F);
                    double h = (double)pos.getZ() + 0.5D + d * (double)direction.getOffsetZ();
                    pigeonEntity.refreshPositionAndAngles(e, g, h, pigeonEntity.getYaw(), pigeonEntity.getPitch());

                    world.playSound(player, pos, SoundEvents.BLOCK_BEEHIVE_EXIT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    return world.spawnEntity(pigeonEntity); // atlast
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    // helper methods
    public boolean isPigeonOwner(PlayerEntity player) {
        return getPigeonData().getUuid("Owner").equals(player.getUuid());
    }

    public boolean hasPigeon() {
        return this.pigeon != null && !this.pigeon.entityData.isEmpty();
    }

    public NbtCompound getPigeonData() {
        if (this.pigeon != null) {
            return this.pigeon.entityData.copy();
        } else {
            return new NbtCompound();
        }
    }

    public void setPigeon(NbtCompound nbtCompound) {
        this.pigeon = new Pigeon(nbtCompound);
    }

    public boolean hasStoredItems() {
        return this.storedItems != null && !this.storedItems.isEmpty();
    }

    public NbtList getStoredItems() {
        return this.storedItems.copy();
    }

    public void setStoredItems(NbtList items) {
        if (items != null) {
            this.storedItems = items;
        }
    }

    public void setOnCooldown(boolean cd) {
        this.onCooldown = cd;
        if (this.world != null) { this.updateListeners(); }
    }

    public boolean isOnCooldown() {
        return this.onCooldown;
    }

    public String getRecipient() {
        return this.recipient;
    }


    // client server bullshittery
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        NbtCompound pigeon = nbt.getCompound("Pigeon");
        this.setPigeon(pigeon);
        this.storedItems = nbt.getList("Items", 10);
        this.setOnCooldown(nbt.getBoolean("Cooldown"));
        this.recipient = nbt.getString("Recipient");
    }

    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put("Pigeon", this.getPigeonData());
        nbt.put("Items", this.getStoredItems());
        nbt.putBoolean("Cooldown", this.isOnCooldown());
        nbt.putString("Recipient", this.getRecipient());
    }

    private void updateListeners() {
        super.markDirty();
        this.getWorld().updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), 3);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    // geckolib stuff
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.bird_house.bag", true));
        return PlayState.CONTINUE;
    }
    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    // hold pigeons hostage
    static void removeIrrelevantNbtKeys(NbtCompound compound) {
        for (String string : IRRELEVANT_PIGEON_NBT_KEYS) {
            compound.remove(string);
        }
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    record Pigeon(NbtCompound entityData) {
        Pigeon {
            BirdhouseBlockEntity.removeIrrelevantNbtKeys(entityData);
        }
    }
}
