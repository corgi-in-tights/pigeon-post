package net.thecorgi.pigeonpost.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.thecorgi.pigeonpost.PigeonPost;
import net.thecorgi.pigeonpost.client.renderer.BirdhouseBlockRenderer;
import net.thecorgi.pigeonpost.client.renderer.PigeonEntityRenderer;
import net.thecorgi.pigeonpost.common.item.address_book.AddressBookGuiDescription;
import net.thecorgi.pigeonpost.common.item.address_book.AddressBookScreen;
import net.thecorgi.pigeonpost.common.item.burner.BurnerGuiDescription;
import net.thecorgi.pigeonpost.common.item.burner.BurnerScreen;
import net.thecorgi.pigeonpost.common.item.envelope.EnvelopeGuiDescription;
import net.thecorgi.pigeonpost.common.item.envelope.EnvelopeScreen;
import net.thecorgi.pigeonpost.common.item.envelope.EnvelopeTooltipComponent;
import net.thecorgi.pigeonpost.common.item.envelope.EnvelopeTooltipData;
import net.thecorgi.pigeonpost.common.registry.BlockRegistry;
import net.thecorgi.pigeonpost.common.registry.EntityRegistry;

@Environment(EnvType.CLIENT)
public class PigeonPostClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(EntityRegistry.PIGEON, PigeonEntityRenderer::new);

        TooltipComponentCallback.EVENT.register(data ->
        {
            if (data instanceof EnvelopeTooltipData) {
                return new EnvelopeTooltipComponent((EnvelopeTooltipData)data);
            }
            return null;
        });

        ScreenRegistry.<EnvelopeGuiDescription, EnvelopeScreen>register(PigeonPost.ENVELOPE_SCREEN_HANDLER, EnvelopeScreen::new);
        ScreenRegistry.<AddressBookGuiDescription, AddressBookScreen>register(PigeonPost.ADDRESS_BOOK_SCREEN_HANDLER, AddressBookScreen::new);
        ScreenRegistry.<BurnerGuiDescription, BurnerScreen>register(PigeonPost.BURNER_SCREEN_HANDLER, BurnerScreen::new);
        BlockEntityRendererRegistry.register(BlockRegistry.BIRDHOUSE_BLOCK_ENTITY, (BlockEntityRendererFactory.Context rendererDispatcherIn) -> new BirdhouseBlockRenderer());
    }
}
