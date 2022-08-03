package net.thecorgi.pigeonpost.common.item.envelope;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.BundleTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class EnvelopeTooltipComponent implements TooltipComponent {
    public static final Identifier TEXTURE = new Identifier("textures/gui/container/bundle.png");
    private final DefaultedList<ItemStack> inventory;
    private final int inventorySize;

    public EnvelopeTooltipComponent(EnvelopeTooltipData data) {
        this.inventory = data.getInventory();
        this.inventorySize = data.getInventorySize();
    }

    public int getHeight() {
        return this.getRows() * 20 + 2 + 4;
    }

    public int getWidth(TextRenderer textRenderer) {
        return this.getColumns() * 18 + 2;
    }

    public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z) {
        int columns = this.getColumns();
        int rows = this.getRows();
        int index = 0;

        for(int row = 0; row < rows; ++row) {
            for(int column = 0; column < columns; ++column) {
                this.drawSlot(x + column * 18 + 1, y + row * 20 + 1, index++,
                        textRenderer, matrices, itemRenderer, z);
            }
        }

        this.drawOutline(x, y, columns, rows, matrices, z);
    }

    private void drawSlot(int x, int y, int index, TextRenderer textRenderer, MatrixStack matrices, ItemRenderer itemRenderer, int z) {
        if (index < this.inventory.size()) {
            ItemStack itemStack = this.inventory.get(index);
            this.draw(matrices, x, y, z, EnvelopeTooltipComponent.Sprite.SLOT);
            itemRenderer.renderInGuiWithOverrides(itemStack, x + 1, y + 1, index);
            itemRenderer.renderGuiItemOverlay(textRenderer, itemStack, x + 1, y + 1);
        } else if (index < inventorySize) {
            ItemStack itemStack = ItemStack.EMPTY;
            this.draw(matrices, x, y, z, EnvelopeTooltipComponent.Sprite.SLOT);
            itemRenderer.renderInGuiWithOverrides(itemStack, x + 1, y + 1, index);
            itemRenderer.renderGuiItemOverlay(textRenderer, itemStack, x + 1, y + 1);
        }
//        if (index >= this.inventory.size()) {
//            this.draw(matrices, x, y, z, index == 0 ? EnvelopeTooltipComponent.Sprite.POSTCARD_SLOT : EnvelopeTooltipComponent.Sprite.SLOT);
//        } else {
//            ItemStack itemStack = this.inventory.get(index);
//            this.draw(matrices, x, y, z, EnvelopeTooltipComponent.Sprite.SLOT);
//            itemRenderer.renderInGuiWithOverrides(itemStack, x + 1, y + 1, index);
//            itemRenderer.renderGuiItemOverlay(textRenderer, itemStack, x + 1, y + 1);
//        }
    }

    private void drawOutline(int x, int y, int columns, int rows, MatrixStack matrices, int z) {
        this.draw(matrices, x, y, z, EnvelopeTooltipComponent.Sprite.BORDER_CORNER_TOP);
        this.draw(matrices, x + columns * 18 + 1, y, z, EnvelopeTooltipComponent.Sprite.BORDER_CORNER_TOP);

        int i;
        for(i = 0; i < columns; ++i) {
            this.draw(matrices, x + 1 + i * 18, y, z, EnvelopeTooltipComponent.Sprite.BORDER_HORIZONTAL_TOP);
            this.draw(matrices, x + 1 + i * 18, y + rows * 20, z, EnvelopeTooltipComponent.Sprite.BORDER_HORIZONTAL_BOTTOM);
        }

        for(i = 0; i < rows; ++i) {
            this.draw(matrices, x, y + i * 20 + 1, z, EnvelopeTooltipComponent.Sprite.BORDER_VERTICAL);
            this.draw(matrices, x + columns * 18 + 1, y + i * 20 + 1, z, EnvelopeTooltipComponent.Sprite.BORDER_VERTICAL);
        }

        this.draw(matrices, x, y + rows * 20, z, EnvelopeTooltipComponent.Sprite.BORDER_CORNER_BOTTOM);
        this.draw(matrices, x + columns * 18 + 1, y + rows * 20, z, EnvelopeTooltipComponent.Sprite.BORDER_CORNER_BOTTOM);
    }

    private void draw(MatrixStack matrices, int x, int y, int z, EnvelopeTooltipComponent.Sprite sprite) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        DrawableHelper.drawTexture(matrices, x, y, z, (float)sprite.u, (float)sprite.v, sprite.width, sprite.height, 128, 128);
    }

    private int getColumns() {
        return inventorySize;
    }

    private int getRows() {
        return inventorySize / 4;
    }

    @Environment(EnvType.CLIENT)
    private enum Sprite {
        SLOT(0, 0, 18, 20),
        BORDER_VERTICAL(0, 18, 1, 20),
        BORDER_HORIZONTAL_TOP(0, 20, 18, 1),
        BORDER_HORIZONTAL_BOTTOM(0, 60, 18, 1),
        BORDER_CORNER_TOP(0, 20, 1, 1),
        BORDER_CORNER_BOTTOM(0, 60, 1, 1);

        public final int u;
        public final int v;
        public final int width;
        public final int height;

        Sprite(int u, int v, int width, int height) {
            this.u = u;
            this.v = v;
            this.width = width;
            this.height = height;
        }
    }

}
