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

    static int maxTypes = 3;
    private static final float PX = 1f/48f;
    private static final float ICON_WIDTH = 16*PX;

    public WIconButton(int type) {

        this.type = type;
        /*
        each type adds 16 pixels to the x
         */
    }

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        super.paint(matrices, x, y, mouseX, mouseY);
        ScreenDrawing.texturedRect(matrices, x+2, y+2, 16, 16, texture, type*ICON_WIDTH, 0, type*ICON_WIDTH+ICON_WIDTH, 1, 0xFF_FFFFFF);
    }
}