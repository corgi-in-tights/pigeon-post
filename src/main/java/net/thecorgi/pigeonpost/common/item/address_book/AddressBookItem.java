package net.thecorgi.pigeonpost.common.item.address_book;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.thecorgi.pigeonpost.PigeonPost;
import net.thecorgi.pigeonpost.common.registry.ItemRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AddressBookItem extends Item implements ExtendedScreenHandlerFactory {
    public static final Identifier ID = PigeonPost.id("item.pigeonpost.address_book.gui");

    public AddressBookItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean onStackClicked(ItemStack book, Slot slot, ClickType clickType, PlayerEntity player) {
        if (clickType == ClickType.RIGHT) {
            ItemStack envelope = slot.getStack();

            if (envelope.isOf(ItemRegistry.ENVELOPE)) {
                NbtCompound bookNbt = book.getOrCreateNbt();
                NbtCompound nbtCompound = envelope.getOrCreateNbt();
                nbtCompound.putLong("Address", bookNbt.getCompound("Fields").getCompound(String.valueOf(bookNbt.getInt("Selected"))).getLong("Address"));
                envelope.setNbt(nbtCompound);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onClicked(ItemStack book, ItemStack envelope, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType == ClickType.RIGHT) {
            if (envelope.isOf(ItemRegistry.ENVELOPE)) {
                NbtCompound bookNbt = book.getOrCreateNbt();
                NbtCompound nbtCompound = getDefaultStack().getOrCreateNbt();
                nbtCompound.putLong("Address", bookNbt.getCompound("Fields").getCompound(String.valueOf(bookNbt.getInt("Selected"))).getLong("Address"));
                envelope.setNbt(nbtCompound);
            }
            return true;
        }
        return false;
    }


    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        player.openHandledScreen(this);
        return TypedActionResult.success(player.getStackInHand(hand));
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("item.pigeonpost.address_book.gui");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new AddressBookGuiDescription(syncId, inv);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        NbtCompound nbtCompound = player.getStackInHand(player.getActiveHand()).getOrCreateNbt();
        buf.writeNbt(nbtCompound.getCompound("Fields"));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbtCompound = stack.getOrCreateNbt();

        if (nbtCompound.contains("Fields")) {
            NbtCompound fields = nbtCompound.getCompound("Fields");

            try {
                for (int i = 0; i < fields.getSize(); i++) {
                    tooltip.add(new TranslatableText("item.pigeonpost.address_book.field", fields.getCompound(String.valueOf(i)).getString("Label")).formatted(i == nbtCompound.getInt("Selected") ? Formatting.GOLD : Formatting.GRAY));
                }
            } catch (RuntimeException ignored) {
                // cant be arsed
            }
        }
    }
}
