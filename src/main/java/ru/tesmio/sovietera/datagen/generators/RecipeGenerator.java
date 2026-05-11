package ru.tesmio.sovietera.datagen.generators;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.function.Consumer;

/**
 * Генератор рецептов.
 * Добавляйте рецепты по мере добавления блоков и предметов.
 */
public class RecipeGenerator extends RecipeProvider implements IConditionBuilder {

    public RecipeGenerator(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> writer) {
        // TODO: Добавлять рецепты по мере добавления блоков и предметов, например:
        //
        // ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, BlocksSE.SOME_BLOCK.get(), 4)
        //         .pattern("SS")
        //         .pattern("SS")
        //         .define('S', Items.STONE)
        //         .unlockedBy(getHasName(Items.STONE), has(Items.STONE))
        //         .save(writer);
    }
}
