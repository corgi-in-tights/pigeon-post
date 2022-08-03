package net.thecorgi.pigeonpost.common.item.other;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Wearable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TinySunglassesItem extends Item implements Wearable {
    public TinySunglassesItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(new TranslatableText("item.pigeonpost.tiny_sunglasses.tooltip1").formatted(Formatting.GRAY));
        tooltip.add(new TranslatableText("item.pigeonpost.tiny_sunglasses.tooltip2").formatted(Formatting.GRAY));
    }
}
