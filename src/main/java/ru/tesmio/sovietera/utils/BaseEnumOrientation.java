package ru.tesmio.sovietera.utils;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

/**
 * 8-направленная ориентация для ламп, размещаемых на стенах/полу/потолке.
 *
 * Вертикальные варианты (_X / _Z) различаются осью длинной стороны лампы:
 *   UP_X   — лампа на потолке, длинная ось вдоль X
 *   UP_Z   — лампа на потолке, длинная ось вдоль Z
 *   DOWN_X — лампа на полу, длинная ось вдоль X
 *   DOWN_Z — лампа на полу, длинная ось вдоль Z
 */
public enum BaseEnumOrientation implements StringRepresentable {
    NORTH("north"),
    SOUTH("south"),
    EAST("east"),
    WEST("west"),
    UP_X("up_x"),
    UP_Z("up_z"),
    DOWN_X("down_x"),
    DOWN_Z("down_z");

    private final String serializedName;

    BaseEnumOrientation(String name) {
        this.serializedName = name;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    /**
     * Определяет ориентацию по первичному направлению (сторона крепления)
     * и горизонтальному направлению взгляда игрока (для оси при вертикальном креплении).
     *
     * @param direction        сторона, к которой крепится блок (from clicked face)
     * @param horizontalFacing горизонтальное направление взгляда игрока
     * @return соответствующая ориентация
     */
    public static BaseEnumOrientation forFacing(Direction direction, Direction horizontalFacing) {
        return switch (direction) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST  -> EAST;
            case WEST  -> WEST;
            case UP    -> horizontalFacing.getAxis() == Direction.Axis.X ? UP_X : UP_Z;
            case DOWN  -> horizontalFacing.getAxis() == Direction.Axis.X ? DOWN_X : DOWN_Z;
        };
    }

    /**
     * Возвращает основное направление (сторону крепления).
     */
    public Direction getPrimaryDirection() {
        return switch (this) {
            case NORTH, UP_Z, DOWN_Z -> Direction.NORTH;  // ambiguous — use getAttachDirection
            default -> Direction.NORTH;
        };
    }

    /**
     * Возвращает направление поверхности, к которой крепится лампа.
     */
    public Direction getAttachDirection() {
        return switch (this) {
            case NORTH  -> Direction.NORTH;
            case SOUTH  -> Direction.SOUTH;
            case EAST   -> Direction.EAST;
            case WEST   -> Direction.WEST;
            case UP_X, UP_Z   -> Direction.UP;
            case DOWN_X, DOWN_Z -> Direction.DOWN;
        };
    }

    /**
     * Возвращает ось длинной стороны лампы.
     * Для настенных ориентаций — ось, перпендикулярная направлению крепления в горизонтальной плоскости.
     * Для вертикальных — явная ось из суффикса.
     */
    public Direction.Axis getLongAxis() {
        return switch (this) {
            case NORTH, SOUTH  -> Direction.Axis.X;
            case EAST, WEST    -> Direction.Axis.Z;
            case UP_X, DOWN_X  -> Direction.Axis.X;
            case UP_Z, DOWN_Z  -> Direction.Axis.Z;
        };
    }
}
