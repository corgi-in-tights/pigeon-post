package net.thecorgi.pigeonpost.common.item.address_book;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class AddressBookScreen extends CottonInventoryScreen<AddressBookGuiDescription> {
    public AddressBookScreen(AddressBookGuiDescription description, PlayerInventory inventory, Text title) {
        super(description, inventory, title);
    }

}
