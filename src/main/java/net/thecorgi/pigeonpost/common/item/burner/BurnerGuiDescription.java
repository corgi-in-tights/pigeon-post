package net.thecorgi.pigeonpost.common.item.burner;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.Vec2i;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.thecorgi.pigeonpost.PigeonPost;
import net.thecorgi.pigeonpost.common.item.envelope.EnvelopeGuiDescription;
import net.thecorgi.pigeonpost.common.registry.ItemRegistry;

import java.util.ArrayList;
import java.util.Objects;

import static net.fabricmc.api.EnvType.CLIENT;
import static net.thecorgi.pigeonpost.PigeonPost.*;

public class BurnerGuiDescription extends SyncedGuiDescription {
    WInvisibleTextField text3 = new WInvisibleTextField();
    WInvisibleTextField text2 = new WInvisibleTextField();
    WInvisibleTextField text = new WInvisibleTextField();

    public BurnerGuiDescription(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory);
        String e = buf.readString();
        if (!Objects.equals(e, "")) { text.setText(buf.readString()); }
    }

    public BurnerGuiDescription(int syncId, PlayerInventory playerInventory) {
        super(BURNER_SCREEN_HANDLER, syncId, playerInventory);

        WPlainPanel root = new WPlainPanel();
        setRootPanel(root);
        root.setSize(146, 100);
        text.setMaxLength(25);
        root.add(text, 0, 10, 130, 20);
        text2.setMaxLength(25);
        root.add(text2, 0, 30, 130, 20);
        text3.setMaxLength(25);
        root.add(text3, 0, 50, 130, 20);

        root.validate(this);
        text.requestFocus();
    }

    @Environment(CLIENT)
    @Override
    public void addPainters() {
        getRootPanel().setBackgroundPainter((matrices, left, top, panel) -> ScreenDrawing.texturedGuiRect(matrices, left, top, panel.getWidth(), panel.getHeight(), id("textures/gui/container/burner.png"), 0xFF_FFFFFF));
    }

    @Override
    public void close(PlayerEntity player) {
        if (world.isClient() && player.getStackInHand(player.getActiveHand()).isOf(ItemRegistry.BURNER)) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(text.getText());
            ClientPlayNetworking.send(BURNER_PACKET_ID, buf);
        }

        super.close(player);
    }
}
