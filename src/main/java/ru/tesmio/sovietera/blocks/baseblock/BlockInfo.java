package ru.tesmio.sovietera.blocks.baseblock;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Базовый блок с поддержкой тултипа (информационной подсказки).
 * Используется для блоков, которым нужно отображать описание при наведении.
 */
public class BlockInfo extends BaseBlock {
    public String info;

    public BlockInfo(Properties p, String info) {
        super(p);
        this.info = info;
    }

    public BlockInfo(Properties p) {
        super(p);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        if (info == null) return;
        tooltip.add(Component.translatable(info));
    }
}
