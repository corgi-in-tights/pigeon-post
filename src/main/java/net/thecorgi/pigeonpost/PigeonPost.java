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
import net.thecorgi.pigeonpost.common.item.address_book.AddressBookGuiDescription;
import net.thecorgi.pigeonpost.common.item.address_book.AddressBookItem;
import net.thecorgi.pigeonpost.common.item.envelope.EnvelopeGuiDescription;
import net.thecorgi.pigeonpost.common.item.envelope.EnvelopeItem;
import net.thecorgi.pigeonpost.common.registry.BlockRegistry;
import net.thecorgi.pigeonpost.common.registry.EntityRegistry;
import net.thecorgi.pigeonpost.common.registry.ItemRegistry;

public class PigeonPost implements ModInitializer {
    public static String ModID = "pigeonpost";
    public static Identifier ENVELOPE_PACKET_ID = id("packet.pigeonpost.envelope.close");
    public static Identifier ADDRESS_BOOK_PACKET_ID = id("packet.pigeonpost.address_book.close");

    public static Identifier id(String path) {
        return new Identifier(ModID, path);
    }

    public static final ItemGroup GENERAL = FabricItemGroupBuilder.create(
                    id("general"))
            .icon(() -> new ItemStack(ItemRegistry.ENVELOPE))
            .build();

    public static ScreenHandlerType<EnvelopeGuiDescription> ENVELOPE_SCREEN_HANDLER;
    public static ScreenHandlerType<AddressBookGuiDescription> ADDRESS_BOOK_SCREEN_HANDLER;

    public static SoundEvent ENTITY_PIGEON_IDLE = new SoundEvent(id("entity.pigeon.idle"));

    @Override
    public void onInitialize() {
        ItemRegistry.init();
        BlockRegistry.init();
        EntityRegistry.init();

        BiomeModifications.addSpawn(BiomeSelectors.categories(Biome.Category.PLAINS, Biome.Category.FOREST, Biome.Category.MOUNTAIN), SpawnGroup.CREATURE,
                EntityRegistry.PIGEON, 1, 2, 7);

//        ENVELOPE_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(EnvelopeItem.ID, (syncId, inventory) -> new EnvelopeGuiDescription(syncId, inventory, ENVELOPE.getDefaultStack()));

//        ENVELOPE_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(EnvelopeItem.ID, ((syncId, inventory, buf) -> new EnvelopeGuiDescription(syncId, inventory, ENVELOPE.getDefaultStack())));
        ENVELOPE_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(EnvelopeItem.ID, EnvelopeGuiDescription::new);
        ADDRESS_BOOK_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(AddressBookItem.ID, AddressBookGuiDescription::new);

        Registry.register(Registry.SOUND_EVENT, id("entity.pigeon.idle"), ENTITY_PIGEON_IDLE);

        ServerPlayNetworking.registerGlobalReceiver(ENVELOPE_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            ItemStack stack = player.getStackInHand(player.getActiveHand());
            if (stack.isOf(ItemRegistry.ENVELOPE)) {
                NbtCompound nbtCompound = stack.getOrCreateNbt();
                nbtCompound.putLong(EnvelopeItem.ADDRESS_KEY, buf.readLong());
                nbtCompound.putString("Recipient", buf.readString());
                stack.setNbt(nbtCompound);
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(ADDRESS_BOOK_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            ItemStack stack = player.getStackInHand(player.getActiveHand());
            if (stack.isOf(ItemRegistry.ADDRESS_BOOK)) {
                NbtCompound nbtCompound = stack.getOrCreateNbt();
                nbtCompound.put("Fields", buf.readNbt());
                nbtCompound.putInt("Selected", buf.readInt());
                stack.setNbt(nbtCompound);
            }
        });


    }
}
