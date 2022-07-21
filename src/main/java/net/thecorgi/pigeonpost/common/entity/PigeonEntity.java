package net.thecorgi.pigeonpost.common.entity;

import com.google.common.collect.Sets;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.EnumSet;
import java.util.Set;

public class PigeonEntity extends TameableHeadEntity implements IAnimatable, Flutterer {
    public static final TrackedData<Integer> VARIANT;
    private final AnimationFactory factory = new AnimationFactory(this);
    private static final Set<Item> TAMING_INGREDIENTS;
    private boolean songPlaying;
    private NbtCompound envelope;
    private boolean hasLocation;
    @Nullable
    private BlockPos songSource;

    static {
        VARIANT = DataTracker.registerData(PigeonEntity.class, TrackedDataHandlerRegistry.INTEGER);
        TAMING_INGREDIENTS = Sets.newHashSet(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS);
    }

    public PigeonEntity(EntityType<? extends TameableHeadEntity> entityType, World world) {
        super(entityType, world);
        this.moveControl = new FlightMoveControl(this, 10, false);
        this.hasLocation = false;
        this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, -1.0F);
    }

    public static DefaultAttributeContainer.Builder createPigeonAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 5.0F)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.42F)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.14F);
    }


    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new EscapeDangerGoal(this, 1.25D));
        this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 4.0F));
        this.goalSelector.add(3, new SitGoal(this));
        this.goalSelector.add(4, new ConditionalFollowOwner(this, 1.0D, 8.0F, 14.0F, true));
        this.goalSelector.add(5, new FlyGoal(this, 1.0D));
    }

    protected EntityNavigation createNavigation(World world) {
        BirdNavigation nav = new BirdNavigation(this, world);
        nav.setCanPathThroughDoors(true);
        nav.setCanSwim(true);
        nav.setCanEnterOpenDoors(true);
        return nav;
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (this.hasVehicle()) {
            return PlayState.STOP;
        } else if (this.isInAir()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.pigeonpost.fall_flight", true));
        } else if (this.isSongPlaying()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.pigeonpost.dance", true));
        } else if (this.isSitting()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.pigeonpost.on_stand", true));
        } else if (event.isMoving()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.pigeonpost.walk", true));
        } else {
            return PlayState.STOP;
        }
        return PlayState.CONTINUE;
    }

    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        this.setVariant(random.nextInt(1, 6));
        this.setEnvelope(new NbtCompound());
        if (entityData == null) {
            entityData = new PassiveData(false);
        }
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(VARIANT, 0);
    }

    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Variant", this.getVariant());
        nbt.put("Envelope", this.getEnvelope());
    }

    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setVariant(nbt.getInt("Variant"));
        this.setEnvelope(nbt.getCompound("Envelope"));
    }

    public int getVariant() {
        return MathHelper.clamp(this.dataTracker.get(VARIANT), 1, 6);
    }

    public void setVariant(int variant) {
        this.dataTracker.set(VARIANT, variant);
    }

    public NbtCompound getEnvelope() {
        return this.envelope;
    }

    public void setEnvelope(NbtCompound nbtCompound) {
        this.envelope = nbtCompound;
    }

    public boolean hasLocation() {
        return this.hasLocation;
    }

    public void setHasLocation(boolean bl) {
        this.hasLocation = bl;
    }

    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    public void tickMovement() {
        if (this.songSource == null || !this.songSource.isWithinDistance(this.getPos(), 3.46D) || !this.world.getBlockState(this.songSource).isOf(Blocks.JUKEBOX)) {
            this.songPlaying = false;
            this.songSource = null;
        }

        Vec3d vec3d = this.getVelocity();
        if (!this.onGround && vec3d.y < 0.0D) {
            this.setVelocity(vec3d.multiply(1.0D, 0.55D, 1.0D));
        }

        this.flapWings();
        super.tickMovement();
    }

    private void flapWings() {
        Vec3d vec3d = this.getVelocity();
        if (!this.onGround && vec3d.y < 0.0D) {
            this.setVelocity(vec3d.multiply(1.0D, 1.3D, 1.0D));
        }
    }

    public void setNearbySongPlaying(BlockPos songPosition, boolean playing) {
        this.songSource = songPosition;
        this.songPlaying = playing;
    }

    public boolean isSongPlaying() {
        return this.songPlaying;
    }

    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (TAMING_INGREDIENTS.contains(stack.getItem())) {
            if (!this.isTamed()) {
                if (!player.getAbilities().creativeMode) {
                    stack.decrement(1);
                }

                if (!this.isSilent()) {
                    this.world.playSound((PlayerEntity) null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PARROT_EAT, this.getSoundCategory(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
                }

                if (!this.world.isClient) {
                    if (this.random.nextInt(8) == 0) {
                        this.setOwner(player);
                        this.world.sendEntityStatus(this, (byte) 7);
                    } else {
                        this.world.sendEntityStatus(this, (byte) 6);
                    }
                }

            } else {
                if (this.getHealth() < this.getMaxHealth()) {
                    this.heal(1.5F);
                    this.world.addImportantParticle(ParticleTypes.HEART, this.getX(), this.getY(), this.getZ(), 0, 0.02, 0);
                }
            }
            return ActionResult.success(this.world.isClient);
        }
//        else if (this.isOwner(player) && stack.isOf(ItemRegistry.ENVELOPE)) {
//            NbtCompound nbtCompound = stack.getOrCreateNbt();
//            if (nbtCompound.contains("Address") && nbtCompound.contains("Envelope")) {
//                if (!player.getAbilities().creativeMode) {
//                    stack.decrement(1);
//                }
//
//                if (!this.world.isClient) {
//                    long l = stack.getOrCreateNbt().getLong("Address");
//                    BlockPos pos = new BlockPos(BlockPos.unpackLongX(l), BlockPos.unpackLongY(l), BlockPos.unpackLongZ(l));
//
//                    "Envelope"
//////                            this.navigation.startMovingTo(pos.getX(), pos.getY(), pos.getZ(), 1D);
//////                            this.setHasLocation(true);
//////                            this.setPosition(Vec3d.ofCenter(pos));
////                        } else {
////                            player.sendMessage(new TranslatableText("envelope.pigeonpost.envelope.birdhouse_full").formatted(Formatting.RED), true);
////                        }
////                    } else {
////                        player.sendMessage(new TranslatableText("envelope.pigeonpost.envelope.not_valid_birdhouse").formatted(Formatting.RED), true);
////                    }
//                }
//
//                return ActionResult.success(this.world.isClient);
//            }
//        }
        else if (this.isTamed() && this.isOwner(player) && player.isSneaking()) {
            this.mountOnHead(player);
        } else if (this.isTamed() && this.isOwner(player) && this.isOnGround() && !player.isSneaking()) {
            this.setSitting(!this.isSitting());
        } else {
            return super.interactMob(player, hand);
        }
        return ActionResult.success(this.world.isClient);
    }


    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    public boolean isInAir() {
        return !this.onGround;
    }

    protected void pushAway(Entity entity) {
        if (!(entity instanceof PlayerEntity)) {
            super.pushAway(entity);
        }
    }



    public double getHeightOffset() {
        return 0.5D;
    }

    public class ConditionalFollowOwner extends FollowOwnerGoal {
        private final PigeonEntity pigeon;

        public ConditionalFollowOwner(PigeonEntity pigeon, double speed, float minDistance, float maxDistance, boolean leavesAllowed) {
            super(pigeon, speed, minDistance, maxDistance, leavesAllowed);
            this.pigeon = pigeon;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        public boolean canStart() {
            if (this.pigeon.hasLocation()) {
                return false;
            }
            return super.canStart();
        }
    }
}
