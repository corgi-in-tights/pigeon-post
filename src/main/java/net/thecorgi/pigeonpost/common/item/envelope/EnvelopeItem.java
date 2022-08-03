package net.thecorgi.pigeonpost.common.item.envelope;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.thecorgi.pigeonpost.PigeonPost;
import net.thecorgi.pigeonpost.common.registry.ItemRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class EnvelopeItem extends Item implements ExtendedScreenHandlerFactory {
    public static final String ITEMS_KEY = "Items";
    public static final String ADDRESS_KEY = "Address";

    public static final Identifier ID = PigeonPost.id("item.pigeonpost.envelope.gui");

    static int size = 3;
    private static final int ITEM_BAR_COLOR = MathHelper.packRgb(1.0F, 0.55F, 0.1F);

    public EnvelopeItem(Settings settings) {
        super(settings);
    }

    private static Stream<ItemStack> getStoredItems(ItemStack envelope) {
        NbtCompound nbtCompound = envelope.getNbt();

        if (nbtCompound == null) {
            return Stream.empty();
        } else {
            NbtList nbtList = nbtCompound.getList(ITEMS_KEY, 10);
            return nbtList.stream().map(NbtCompound.class::cast).map(ItemStack::fromNbt);
        }
    }

    public boolean isItemBarVisible(ItemStack envelope) {
        return envelope.getOrCreateNbt().contains(ITEMS_KEY);
    }

    public int getItemBarStep(ItemStack stack) {
        return Math.min(Math.round(13F / size * getStoredItems(stack).count()), 13);
    }

    public int getItemBarColor(ItemStack stack) {
        return ITEM_BAR_COLOR;
    }

    void unpackEnvelope(ItemStack envelope, PlayerEntity player) {
        NbtList stacks = envelope.getOrCreateNbt().getList(ITEMS_KEY, 10);

        PlayerInventory pl = player.getInventory();
        for(int i = 0; i < size; ++i) {
            NbtCompound item = stacks.getCompound(i);
            pl.offerOrDrop(ItemStack.fromNbt(item));
        }
        envelope.removeSubNbt(ITEMS_KEY);
        envelope.setCount(0);
        pl.offerOrDrop(Items.PAPER.getDefaultStack());
    }

    @Override
    public boolean onStackClicked(ItemStack envelope, Slot slot, ClickType clickType, PlayerEntity player) {
        if (clickType == ClickType.RIGHT) {
            ItemStack selectedStack = slot.getStack();

            if (!selectedStack.isEmpty()) {
                addItem(envelope, selectedStack);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onClicked(ItemStack envelope, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType == ClickType.RIGHT) {
            if (otherStack.isEmpty()) {
                unpackEnvelope(envelope, player);
            } else {
                addItem(envelope, otherStack);
            }
            return true;
        }
        return false;
    }

    void addItem(ItemStack envelope, ItemStack stack) {
        NbtCompound nbtCompound = envelope.getOrCreateNbt();
        SimpleInventory inv = new SimpleInventory(size);

        if (nbtCompound.contains(ITEMS_KEY)) {
            inv.readNbtList(nbtCompound.getList(ITEMS_KEY, 10));
        }

        ItemStack result = inv.addStack(stack);
        if (result != stack) {
            stack.setCount(result.getCount());
        }

        nbtCompound.put(ITEMS_KEY, inv.toNbtList());
        envelope.setNbt(nbtCompound);
    }

    public static boolean isEnvelope(ItemStack stack) {
        return stack.isOf(ItemRegistry.ENVELOPE);
    }

    public Optional<TooltipData> getTooltipData(ItemStack envelope) {
        DefaultedList<ItemStack> stacks = DefaultedList.of();
        getStoredItems(envelope).forEach(stacks::add);

        return Optional.of(new EnvelopeTooltipData(stacks, size));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        NbtCompound nbtCompound = stack.getOrCreateNbt();
        if (!nbtCompound.contains(ADDRESS_KEY)) {
            nbtCompound.putLong(ADDRESS_KEY, 0);
            stack.setNbt(nbtCompound);
        }

        if (!nbtCompound.contains("Recipient")) {
            nbtCompound.putString("Recipient", "");
            stack.setNbt(nbtCompound);
        }

//        NamedScreenHandlerFactory factory = new SimpleNamedScreenHandlerFactory((syncId, inventory, user) -> new EnvelopeGuiDescription(syncId, inventory, stack, ScreenHandlerContext.create(world, player.getBlockPos())), new TranslatableText("item.pigeonpost.envelope.gui"));
//        player.openHandledScreen(factory);


        player.openHandledScreen(this);
//
//        player.openHandledScreen(this);
//        }

        return TypedActionResult.success(player.getStackInHand(hand));
    }

    public static void offerOrDropEnvelope(NbtList items, PlayerEntity player) {
        PlayerInventory pl = player.getInventory();
        for(int i = 0; i < items.size(); ++i) {
            NbtCompound item = items.getCompound(i);
            pl.offerOrDrop(ItemStack.fromNbt(item));
        }
    }


    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbtCompound = stack.getOrCreateNbt();
        if (nbtCompound.contains(ADDRESS_KEY)) {
            long pos = nbtCompound.getLong(ADDRESS_KEY);
            int x = BlockPos.unpackLongX(pos);
            int y = BlockPos.unpackLongY(pos);
            int z = BlockPos.unpackLongZ(pos);

            tooltip.add(new TranslatableText("item.pigeonpost.envelope.address.valid", Integer.toString(x), Integer.toString(y), Integer.toString(z)).formatted(Formatting.GRAY));
        } else {
            tooltip.add(new TranslatableText("item.pigeonpost.envelope.address.empty").formatted(Formatting.GRAY));
        }

        if (nbtCompound.contains("Recipient")) {
            String r = nbtCompound.getString("Recipient");
            if (!Objects.equals(r, "")) {
                tooltip.add(new TranslatableText("item.pigeonpost.envelope.recipient", nbtCompound.getString("Recipient")).formatted(Formatting.GRAY));
            }
        }
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("item.pigeonpost.envelope.gui");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new EnvelopeGuiDescription(syncId, inv);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        ItemStack stack = player.getStackInHand(player.getActiveHand());
        if (stack.isOf(ItemRegistry.ENVELOPE)) {
            NbtCompound nbtCompound = stack.getOrCreateNbt();
            buf.writeString(nbtCompound.getString("Recipient"));
            buf.writeLong(nbtCompound.getLong("Address"));
        }
    }
}
