package net.thecorgi.pigeonpost.common.item.address_book;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
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
import net.minecraft.nbt.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.thecorgi.pigeonpost.PigeonPost;
import net.thecorgi.pigeonpost.common.item.envelope.EnvelopeGuiDescription;
import net.thecorgi.pigeonpost.common.registry.ItemRegistry;

import java.util.*;

import static net.fabricmc.api.EnvType.CLIENT;
import static net.thecorgi.pigeonpost.PigeonPost.ADDRESS_BOOK_PACKET_ID;
import static net.thecorgi.pigeonpost.PigeonPost.id;

public class AddressBookGuiDescription extends SyncedGuiDescription {
    int count = 0;
    public static int maxCount = 5;
    int selected = 0;
    WButton previousButton = null;

    @Environment(CLIENT)
    @Override
    public void addPainters() {
        getRootPanel().setBackgroundPainter((matrices, left, top, panel) -> ScreenDrawing.texturedGuiRect(matrices, left, top, panel.getWidth(), panel.getHeight(), id("textures/gui/container/address_book.png"), 0xFF_FFFFFF));
    }

    ArrayList<FieldData> fields = new ArrayList<>();

    WTextField label = new WTextField(Text.of("Label"));
    WTextField x = new WTextField();
    WTextField y = new WTextField();
    WTextField z = new WTextField();
    WLabel x_label = new WLabel("X");
    WLabel y_label = new WLabel("Y");
    WLabel z_label = new WLabel("Z");

    WIconButton confirm = new WIconButton(0);
    WIconButton remove = new WIconButton(1);
    WIconButton add = new WIconButton(2);

    WBox box = new WBox(Axis.VERTICAL);

    public AddressBookGuiDescription(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory);


        NbtCompound nbtCompound = buf.readNbt();
        if (nbtCompound != null && !nbtCompound.isEmpty()) {
            count = Math.min(nbtCompound.getSize(), maxCount); // so ye idiots dont break the game
            try {
                for (int i = 0; i < count; i++) {
                    NbtElement nbtCompound1 = nbtCompound.get(String.valueOf(i));
                    if (nbtCompound1 != null && nbtCompound1.getNbtType() == NbtCompound.TYPE) {
                        fields.add(new FieldData(
                                ((NbtCompound) nbtCompound1).getString("Label"),
                                ((NbtCompound) nbtCompound1).getLong("Address")));

                        fieldFromData(i, fields.get(i));
                    }
                }
            } catch (RuntimeException ignored) {
                // truly wonk
            }
        }
    }

    public AddressBookGuiDescription(int syncId, PlayerInventory playerInventory) {
        super(PigeonPost.ADDRESS_BOOK_SCREEN_HANDLER, syncId, playerInventory);

        WPlainPanel root = new WPlainPanel();
        setRootPanel(root);
        root.setSize(252, 162);

        setTitlePos(new Vec2i(31, 18));

        x.setTextPredicate(EnvelopeGuiDescription::checkIfCoordStr);
        y.setTextPredicate(EnvelopeGuiDescription::checkIfCoordStr);
        z.setTextPredicate(EnvelopeGuiDescription::checkIfCoordStr);

        x.setMaxLength(10);
        y.setMaxLength(10);
        z.setMaxLength(10);
        label.setMaxLength(30);

        root.add(new WScrollPanel(box).setScrollingHorizontally(TriState.FALSE), 29, 47, 79, 20*maxCount+2);
        box.setSpacing(0);

        root.add(remove.setEnabled(false), 7, 47+24, 20, 20);
        root.add(add.setOnClick(() -> {
            if (count < maxCount) {
                newField(count);
                count++;
                if (count >= maxCount) {
                    add.setEnabled(false);
                }
            }}), 7, 47, 20, 20);

        root.validate(this);
    }

    void newField(int index) {
        FieldData data = new FieldData("New Address", 0);
        this.fieldFromData(index, data);
        fields.add(data);
    }

    void fieldFromData(int index, FieldData data) {
        WButton b = new WButton(Text.of(data.getLabel()));

        b.setOnClick(() -> {
            if (previousButton != null) { previousButton.setEnabled(true); }
            previousButton = b;
            b.setEnabled(false);

            WPlainPanel root = (WPlainPanel) getRootPanel();
            this.clearDashboard(root);

            label.setText(b.getLabel().asString());
            BlockPos address = BlockPos.fromLong(data.getAddress());
            x.setText(String.valueOf(address.getX()));
            y.setText(String.valueOf(address.getY()));
            z.setText(String.valueOf(address.getZ()));

            root.add(label, 140, 18, 82, 20);
            root.add(x, 140, 45, 55, 20);
            root.add(y, 140, 45+27, 55, 20);
            root.add(z, 140, 45+27*2, 55, 20);
            root.add(x_label, 220, 50);
            root.add(y_label, 220, 50+27);
            root.add(z_label, 220, 50+27*2);

            confirm.setOnClick(() -> {
                b.setLabel(Text.of(label.getText()));
                selected = index;

                fields.set(index, new FieldData(
                        label.getText(),
                        BlockPos.asLong(Integer.parseInt(x.getText()),
                                Integer.parseInt(y.getText()),
                                Integer.parseInt(z.getText())))
                );
            });
            root.add(confirm.setEnabled(true), 224, 17, 20, 20);

            // setHost since validate has already been called
            label.setHost(this);
            x.setHost(this);
            y.setHost(this);
            z.setHost(this);
            confirm.setHost(this);

            root.remove(remove);
            remove.setHost(this);
            remove.setOnClick(() -> {
                this.clearDashboard(root);
                remove.setEnabled(false);
                box.remove(b);
                box.layout();
                this.count--;
                add.setEnabled(true);
            });
            root.add(remove.setEnabled(true), 7, 71, 20, 20);
        });

        box.add(b, 79, 20);
        box.layout();
    }

    public void clearDashboard(WPanel root) {
        root.remove(label);
        root.remove(x);
        root.remove(y);
        root.remove(z);
        root.remove(x_label);
        root.remove(y_label);
        root.remove(z_label);
        root.remove(confirm);
    }

    @Override
    public void close(PlayerEntity player) {
        if (world.isClient() && player.getStackInHand(player.getActiveHand()).isOf(ItemRegistry.ADDRESS_BOOK)) {
            NbtCompound fieldsCompound = new NbtCompound();
            try {
                for (int i = 0; i < count; i++) {
                    FieldData data = fields.get(i);
                    NbtCompound field = new NbtCompound();
                    field.putString("Label", data.getLabel());
                    field.putLong("Address", data.getAddress());

                    fieldsCompound.put(String.valueOf(i), field);
                }

                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeNbt(fieldsCompound);
                buf.writeInt(selected);
                ClientPlayNetworking.send(ADDRESS_BOOK_PACKET_ID, buf);
            } catch (RuntimeException ignored) {
                // truly wonk
            }
        }

        super.close(player);
    }

    static class FieldData {
        String label;
        long address;

        public FieldData(String label, long address) {
            this.label = label;
            this.address = address;
        }

        public String getLabel() {
            return label;
        }

        public long getAddress() {
            return address;
        }
    }
}
