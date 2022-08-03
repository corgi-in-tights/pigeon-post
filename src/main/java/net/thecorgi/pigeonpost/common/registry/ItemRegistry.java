package net.thecorgi.pigeonpost.common.registry;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import net.thecorgi.pigeonpost.PigeonPost;
import net.thecorgi.pigeonpost.common.item.address_book.AddressBookItem;
import net.thecorgi.pigeonpost.common.item.envelope.EnvelopeItem;
import net.thecorgi.pigeonpost.common.item.other.TinySunglassesItem;

import static net.thecorgi.pigeonpost.PigeonPost.id;

public class ItemRegistry {
    public static Item TINY_SUNGLASSES = new TinySunglassesItem(new FabricItemSettings().rarity(Rarity.UNCOMMON).maxCount(1).group(PigeonPost.GENERAL));
    public static Item ENVELOPE = new EnvelopeItem(new FabricItemSettings().maxCount(1).group(PigeonPost.GENERAL));
    public static Item ADDRESS_BOOK = new AddressBookItem(new FabricItemSettings().maxCount(1).group(PigeonPost.GENERAL));
    public static Item PIGEON_SPAWN_EGG = new SpawnEggItem(EntityRegistry.PIGEON, 0x8e8f9b, 0xb9b9b9, new FabricItemSettings().group(PigeonPost.GENERAL));

    public static void init() {
        register(TINY_SUNGLASSES,"tiny_sunglasses");
        register(ENVELOPE,"envelope");
        register(ADDRESS_BOOK,"address_book");
        register(PIGEON_SPAWN_EGG,"pigeon_spawn_egg");
    }

    private static void register(Item item, String path) {
        Registry.register(Registry.ITEM, id(path), item);
    }
}
