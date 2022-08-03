package net.thecorgi.pigeonpost.common.item.burner;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class BurnerScreen extends CottonInventoryScreen<BurnerGuiDescription> {
    public BurnerScreen(BurnerGuiDescription description, PlayerInventory inventory, Text title) {
        super(description, inventory, title);
    }

}
