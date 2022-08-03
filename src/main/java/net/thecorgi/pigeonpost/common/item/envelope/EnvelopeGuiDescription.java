package net.thecorgi.pigeonpost.common.item.envelope;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.thecorgi.pigeonpost.PigeonPost;

import java.util.Objects;
import java.util.function.Predicate;

import static net.thecorgi.pigeonpost.PigeonPost.ENVELOPE_PACKET_ID;

public class EnvelopeGuiDescription extends SyncedGuiDescription {
    WTextField fieldX = new WTextField();
    WTextField fieldY = new WTextField();
    WTextField fieldZ = new WTextField();
    WTextField intendedReciever = new WTextField();

    public static boolean checkIfCoordStr(String v){
        if (v.length() > 0) {
            char ch = v.substring(v.length() - 1).charAt(0);
            return Character.isDigit(ch) || ch == '-';
        }
        return false;
    }
    public static Predicate<String> coordsPredicate = EnvelopeGuiDescription::checkIfCoordStr;

    public EnvelopeGuiDescription(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory);

        String r = buf.readString();
        if (!Objects.equals(r, "") || !Objects.equals(r, " ")) { intendedReciever.setText(r);}
        long address = buf.readLong();
        fieldX.setText(String.valueOf(BlockPos.unpackLongX(address)));
        fieldY.setText(String.valueOf(BlockPos.unpackLongY(address)));
        fieldZ.setText(String.valueOf(BlockPos.unpackLongZ(address)));
    }

    public EnvelopeGuiDescription(int syncId, PlayerInventory playerInventory) {
        super(PigeonPost.ENVELOPE_SCREEN_HANDLER, syncId, playerInventory);

        WPlainPanel root = new WPlainPanel();
        setRootPanel(root);
        root.setSize(180, 75);
        root.setInsets(Insets.ROOT_PANEL);


        WLabel xLabel = new WLabel(new LiteralText("X"));
        root.add(xLabel, 27, 16);

        WLabel yLabel = new WLabel(new LiteralText("Y"));
        root.add(yLabel, 81, 16);

        WLabel zLabel = new WLabel(new LiteralText("Z"));
        root.add(zLabel, 135, 16);

        root.add(fieldX, 8, 25);
        fieldX.setSize(44, 15);
        fieldX.setTextPredicate(coordsPredicate);

        root.add(fieldY, 62, 25);
        fieldY.setSize(44, 15);
        fieldY.setTextPredicate(coordsPredicate);

        root.add(fieldZ, 116, 25);
        fieldZ.setSize(44, 15);
        fieldZ.setTextPredicate(coordsPredicate);

        root.add(intendedReciever, 8, 50);
        intendedReciever.setSize(142, 15);
        intendedReciever.setSuggestion("Recipient (blank for any)");

        root.validate(this);
    }


    @Override
    public void close(PlayerEntity player) {
        ItemStack stack = player.getStackInHand(player.getActiveHand());

        if (world.isClient() && stack.getItem() instanceof EnvelopeItem) {
            try {
                int x = Integer.parseInt(fieldX.getText());
                int y = Integer.parseInt(fieldY.getText());
                int z = Integer.parseInt(fieldZ.getText());
                long pos = BlockPos.asLong(x, y, z);

                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeLong(pos);
                buf.writeString(intendedReciever.getText());
                ClientPlayNetworking.send(ENVELOPE_PACKET_ID, buf);

            } catch (NumberFormatException ex) { // should ideally not happen :) but you never know
                return;
            }
        }

        super.close(player);
    }
}