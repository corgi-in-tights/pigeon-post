package net.thecorgi.pigeonpost.common.item.address_book;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.thecorgi.pigeonpost.PigeonPost;

public class WIconButton extends WButton {
    private final Identifier texture = PigeonPost.id("textures/gui/buttons.png");
    int type;

    static float totalTypes = 3f; // float for later division
    private static final float PX = 1f/16f*totalTypes;
    private static final float ICON_WIDTH = 16f*PX;

    public WIconButton(int type) {

        this.type = type;
        /*
        confirm: 0
        remove: 1
        add: 2
         */
    }

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        super.paint(matrices, x, y, mouseX, mouseY);
        ScreenDrawing.texturedRect(matrices, x+2, y+2, 16, 16, texture, type/totalTypes, 0, (type+1)/totalTypes, 1, 0xFF_FFFFFF);
    }
}