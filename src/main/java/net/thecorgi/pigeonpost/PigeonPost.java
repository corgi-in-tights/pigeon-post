package net.thecorgi.pigeonpost;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.thecorgi.pigeonpost.common.item.envelope.EnvelopeGuiDescription;
import net.thecorgi.pigeonpost.common.item.envelope.EnvelopeItem;
import net.thecorgi.pigeonpost.common.registry.BlockRegistry;
import net.thecorgi.pigeonpost.common.registry.EntityRegistry;
import net.thecorgi.pigeonpost.common.registry.ItemRegistry;

import static net.thecorgi.pigeonpost.common.registry.ItemRegistry.ENVELOPE;

public class PigeonPost implements ModInitializer {
    public static String ModID = "pigeonpost";
    public static Identifier ADDRESS_PACKET_ID = id("address_packet");

    public static Identifier id(String path) {
        return new Identifier(ModID, path);
    }

    public static final ItemGroup GENERAL = FabricItemGroupBuilder.create(
                    id("general"))
            .icon(() -> new ItemStack(ENVELOPE))
            .build();

    public static ScreenHandlerType<EnvelopeGuiDescription> SCREEN_HANDLER_TYPE;

    public static SoundEvent ENTITY_PIGEON_IDLE = new SoundEvent(id("entity.pigeon.idle"));

    @Override
    public void onInitialize() {
        ItemRegistry.init();
        BlockRegistry.init();
        EntityRegistry.init();

        BiomeModifications.addSpawn(BiomeSelectors.categories(Biome.Category.PLAINS, Biome.Category.FOREST, Biome.Category.MOUNTAIN), SpawnGroup.CREATURE,
                EntityRegistry.PIGEON, 1, 2, 7);

        SCREEN_HANDLER_TYPE = ScreenHandlerRegistry.registerSimple(EnvelopeItem.ID, (syncId, inventory) -> new EnvelopeGuiDescription(syncId, inventory, ENVELOPE.getDefaultStack()));

        Registry.register(Registry.SOUND_EVENT, id("entity.pigeon.idle"), ENTITY_PIGEON_IDLE);

        ServerPlayNetworking.registerGlobalReceiver(ADDRESS_PACKET_ID, (server, player, handler, buf, responseSender) -> {

            if (player.getWorld().isClient()) return;

            ItemStack stack = player.getStackInHand(player.getActiveHand());

            if (stack.isOf(ENVELOPE)) {
                NbtCompound nbtCompound = stack.getOrCreateNbt();

                nbtCompound.putLong(EnvelopeItem.ADDRESS_KEY, buf.readLong());
                nbtCompound.putString("Recipient", buf.readString());
                stack.setNbt(nbtCompound);
            }
        });

    }
}
