package ru.tesmio.sovietera.blocks.devices.generator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;


/**
 * Рендерер топливного датчика для дизельного генератора.
 * <p>
 * Отображает процент заполнения бака текстом на передней грани блока.
 * Показывается только когда игрок приседает и смотрит на блок в пределах ~5 блоков.
 */
public class RendererDieselEngine implements BlockEntityRenderer<EntityBlockDieselEngine> {

    /** Максимальная дистанция видимости датчика (в блоках) */
    private static final double MAX_VIEW_DISTANCE = 5.0;

    /** Отступ от грани блока для предотвращения z-fighting */
    private static final float FACE_OFFSET = 0.002f;

    /** Уровень освещённости для UI-элементов (максимальный) */
    private static final int FULL_BRIGHTNESS = 15728880;

    /** Вертикальная позиция текста на грани (0 = низ, 1 = верх). Ниже центра. */
    private static final float TEXT_Y = 0.35f;

    /** Горизонтальный центр текста (0.5 = центр грани) */
    private static final float TEXT_X = 0.5f;

    private final Font font;

    public RendererDieselEngine(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(EntityBlockDieselEngine blockEntity, float partialTick,
            PoseStack poseStack, MultiBufferSource bufferSource,
            int packedLight, int packedOverlay) {

        // Проверка: рендерим только если игрок приседает и смотрит на блок
        if (!shouldRenderGauge(blockEntity)) {
            return;
        }

        // Получаем направление передней грани блока
        Direction facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);

        poseStack.pushPose();

        // Применяем поворот так, чтобы текст всегда был на передней грани
        applyFacingRotation(poseStack, facing);

        // Сдвигаем к поверхности грани (наружу) с отступом от z-fighting
        poseStack.translate(0, 0, 0.5 + FACE_OFFSET);

        // Рисуем процент заполнения текстом
        float fillRatio = getFillRatio(blockEntity);
        renderPercentageText(poseStack, bufferSource, fillRatio, facing);

        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return (int) MAX_VIEW_DISTANCE + 1;
    }

    // ===================== Проверка видимости =====================

    /**
     * Проверяет, должен ли датчик отображаться.
     * Условия: игрок приседает И смотрит на блок (луч попадает в блок) в пределах MAX_VIEW_DISTANCE.
     */
    private boolean shouldRenderGauge(EntityBlockDieselEngine blockEntity) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return false;
        }

        // Игрок должен приседать
        if (!mc.player.isShiftKeyDown()) {
            return false;
        }

        // Проверяем дистанцию
        double distanceSq = mc.player.blockPosition().distSqr(blockEntity.getBlockPos());
        if (distanceSq > MAX_VIEW_DISTANCE * MAX_VIEW_DISTANCE) {
            return false;
        }

        // Проверяем, что игрок смотрит на этот блок (луч попадает в блок)
        HitResult hitResult = mc.hitResult;
        if (hitResult instanceof BlockHitResult blockHit) {
            return blockHit.getBlockPos().equals(blockEntity.getBlockPos());
        }

        return false;
    }

    // ===================== Вращение по FACING =====================

    /**
     * Поворачивает PoseStack так, чтобы текст был обращён наружу
     * к передней грани блока.
     * <p>
     * Стратегия: по умолчанию рисуем на южной грани (z+),
     * затем поворачиваем вокруг центра блока + дополнительный 180 градусный поворот
     * чтобы текст смотрел наружу (лицевой стороной к игроку).
     * <p>
     * FACING=NORTH -> поворот 180 + 180 = 360 (0)<br>
     * FACING=SOUTH -> поворот 0 + 180 = 180<br>
     * FACING=WEST  -> поворот 90 + 180 = 270<br>
     * FACING=EAST  -> поворот -90 + 180 = 90
     */
    private void applyFacingRotation(PoseStack poseStack, Direction facing) {
        // Сдвигаем к центру блока для вращения
        poseStack.translate(0.5, 0.5, 0.5);

        // Дополнительный поворот на 180 градусов чтобы текст смотрел наружу
        poseStack.mulPose(Axis.YP.rotationDegrees(180));

        switch (facing) {
            case NORTH -> { /* 180 + 180 = 360 — без дополнительного поворота */ }
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case WEST  -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case EAST  -> poseStack.mulPose(Axis.YP.rotationDegrees(-90));
            default    -> { /* Вертикальные направления не ожидаем у этого блока */ }
        }

        // Возвращаем начало координат в нижний левый угол грани
        poseStack.translate(-0.5, -0.5, 0.0);
    }

    // ===================== Расчёт заполнения =====================

    /**
     * Возвращает отношение заполнения бака от 0.0 до 1.0.
     */
    private float getFillRatio(EntityBlockDieselEngine blockEntity) {
        int amount = blockEntity.getTank().getFluidAmount();
        int capacity = blockEntity.getTank().getCapacity();
        if (capacity <= 0) return 0.0f;
        return Mth.clamp((float) amount / capacity, 0.0f, 1.0f);
    }

    // ===================== Процент текстом =====================

    /**
     * Рисует текст с процентом заполнения на передней грани блока.
     * <p>
     * Для направлений WEST/EAST текст зеркалится из-за 90 градусного поворота,
     * поэтому применяется scale(-1, 1, 1) для коррекции.
     */
    private void renderPercentageText(PoseStack poseStack, MultiBufferSource bufferSource,
            float fillRatio, Direction facing) {
        String text = Math.round(fillRatio * 100) + "%";

        poseStack.pushPose();

        // Позиция текста — ниже центра грани
        poseStack.translate(TEXT_X, 0.78f, 0.001f); // чуть ближе к камере

        // Масштаб шрифта — мелкий, чтобы уместиться на блоке
        float scale = 0.014f;

        // Для WEST/EAST направлений 90 градусный поворот вызывает зеркальное отражение текста.
        // Компенсируем масштабированием по X на -1.
        boolean needsMirrorFix = (facing == Direction.WEST || facing == Direction.EAST);


            poseStack.scale(scale, -scale, -scale); // Y инвертирован для Font


        // Центрируем текст по горизонтали
        float textWidth = this.font.width(text) / 2.0f;

        // Белый цвет текста с тенью для читаемости
        int textColor = 0xFFFFFF;
        this.font.drawInBatch(text, -textWidth, 0, textColor, false,
                poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL,
                0, FULL_BRIGHTNESS);

        poseStack.popPose();
    }
}
