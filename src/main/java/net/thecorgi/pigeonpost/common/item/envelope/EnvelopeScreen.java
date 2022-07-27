package net.thecorgi.pigeonpost.common.item.envelope;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class EnvelopeScreen extends CottonInventoryScreen<EnvelopeGuiDescription> {
    public EnvelopeScreen(EnvelopeGuiDescription gui, PlayerEntity player, Text title) {
        super(gui, player, title);
    }
}