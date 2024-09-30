package com.petrolpark.destroy.util.vat;

import com.petrolpark.destroy.config.DestroyAllConfigs;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Interface for Blocks which can heat or cool a {@link Vat}.
 */
public interface IVatHeaterBlock {
    
    /**
     * Get the power (in watts) the given Block State supplies or withdraws from a {@link Vat}.
     * @param level
     * @param blockState
     * @param blockPos
     * @param face The face of the Block State touching the Vat
     * @return Positive value for heaters, negative value for coolers
     */
    float getHeatingPower(Level level, BlockState blockState, BlockPos blockPos, Direction face);

    public static float getHeatingPower(Level level, BlockPos blockPos, Direction face) {
        BlockState state = level.getBlockState(blockPos);

        if (state.isAir()) return 0f;

        // IVatHeaters
        if (state.getBlock() instanceof IVatHeaterBlock heater) {
            return heater.getHeatingPower(level, state, blockPos, face);
        };

        // Blaze Burners, Coolers, etc.
        if (state.hasProperty(BlazeBurnerBlock.HEAT_LEVEL) && face == Direction.UP) {
            HeatLevel heatLevel = state.getValue(BlazeBurnerBlock.HEAT_LEVEL);
            if (heatLevel == HeatLevel.KINDLED) {
                return DestroyAllConfigs.SERVER.blocks.blazeBurnerHeatingPower.getF();
            } else if (heatLevel == HeatLevel.SEETHING) {
                return DestroyAllConfigs.SERVER.blocks.blazeBurnerSuperHeatingPower.getF();
            } else if (heatLevel.name().equals("FROSTING")) {
                return DestroyAllConfigs.SERVER.blocks.coolerHeatingPower.getF();
            };
        };

        return 0f;
    };
};
