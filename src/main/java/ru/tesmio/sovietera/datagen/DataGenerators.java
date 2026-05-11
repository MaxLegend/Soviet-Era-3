package ru.tesmio.sovietera.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.tesmio.sovietera.SovietEra;
import ru.tesmio.sovietera.datagen.generators.*;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = SovietEra.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        // Клиентские провайдеры
        generator.addProvider(event.includeClient(), new BlockStateGenerator(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new ItemModelGenerator(packOutput, existingFileHelper));
        // Серверные провайдеры
        generator.addProvider(event.includeServer(), new RecipeGenerator(packOutput));
        generator.addProvider(event.includeServer(), LootGenerator.create(packOutput));
        BlockTagGenerator blockTagGen = generator.addProvider(event.includeServer(),
                new BlockTagGenerator(packOutput, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(),
                new ItemTagGenerator(packOutput, lookupProvider, blockTagGen.contentsGetter(), existingFileHelper));


    }
}
