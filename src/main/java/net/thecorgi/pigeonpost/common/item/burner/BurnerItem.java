package net.thecorgi.pigeonpost.common.item.burner;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.client.gui.screen.ingame.BookScreen;
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

public class BurnerItem extends Item implements ExtendedScreenHandlerFactory {
    public static final Identifier ID = PigeonPost.id("item.pigeonpost.burner.gui");

    public BurnerItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        NbtCompound nbtCompound = stack.getOrCreateNbt();
        if (!nbtCompound.contains("Text")) {
            nbtCompound.putString("Text", "");
            stack.setNbt(nbtCompound);
        }

        player.openHandledScreen(this);
        return TypedActionResult.success(player.getStackInHand(hand));
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("item.pigeonpost.burner.gui");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new BurnerGuiDescription(syncId, inv);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        NbtCompound nbtCompound = player.getStackInHand(player.getActiveHand()).getOrCreateNbt();
        buf.writeString(nbtCompound.getString("Text"));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(new TranslatableText("item.pigeonpost.burner.tooltip").formatted(Formatting.GRAY));
    }
}
