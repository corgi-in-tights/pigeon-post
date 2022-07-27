package net.thecorgi.pigeonpost.common.registry;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.registry.Registry;
import net.thecorgi.pigeonpost.PigeonPost;
import net.thecorgi.pigeonpost.common.item.envelope.EnvelopeItem;

import static net.thecorgi.pigeonpost.PigeonPost.id;

public class ItemRegistry {
    public static Item ENVELOPE = new EnvelopeItem(new FabricItemSettings().maxCount(1).group(PigeonPost.GENERAL));
    public static Item PIGEON_SPAWN_EGG = new SpawnEggItem(EntityRegistry.PIGEON, 0x8e8f9b, 0xb9b9b9, new FabricItemSettings().group(PigeonPost.GENERAL));

    public static void init() {
        register(ENVELOPE,"item");
        register(PIGEON_SPAWN_EGG,"pigeon_spawn_egg");
    }

    private static void register(Item item, String path) {
        Registry.register(Registry.ITEM, id(path), item);
    }
}
