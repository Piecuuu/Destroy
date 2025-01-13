package com.petrolpark.destroy.block;

import javax.annotation.Nullable;

import com.petrolpark.compat.create.block.entity.behaviour.AbstractRememberPlacerBehaviour;
import com.petrolpark.destroy.block.entity.BubbleCapBlockEntity;
import com.petrolpark.destroy.block.entity.DestroyBlockEntityTypes;
import com.petrolpark.destroy.block.shape.DestroyShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BubbleCapBlock extends Block implements IBE<BubbleCapBlockEntity>, IWrenchable {

    public static final DirectionProperty PIPE_FACE = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty TOP = BooleanProperty.create("top"); // Whether this Bubble Cap is at the top of a Distillation Tower
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom"); // Whether this Bubble Cap is at the bottom of a Distillation Tower

    public BubbleCapBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(defaultBlockState()
            .setValue(PIPE_FACE, Direction.NORTH)
            .setValue(TOP, true)
            .setValue(BOTTOM, true)
        );
    };

    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (oldState.getBlock() == state.getBlock() || isMoving) return; // So we don't get in an infinite loop of noticing we've been placed, so setting the Block State, so noticing we've been 'placed', etc.
        withBlockEntityDo(worldIn, pos, be -> {
            be.attemptRotation(false);
            be.createOrAddToTower(worldIn);
        });
    };

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(PIPE_FACE, TOP, BOTTOM);
        super.createBlockStateDefinition(builder);
    };

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        BlockPos posAbove = pos.above();
        boolean isAbove = posAbove.getY() == neighbor.getY(); // If the Block changed is above
        boolean isBelow = pos.getY() - 1 == neighbor.getY(); // If the Block changed is below
        withBlockEntityDo(level, pos, be -> {
            be.attemptRotation(false);
            if (isAbove || isBelow) {
                be.createOrAddToTower(level);
            };
        });
        BlockState stateAbove = level.getBlockState(posAbove);
        if (isBelow && stateAbove.is(DestroyBlocks.BUBBLE_CAP.get())) { // If the Block below changed, update the Bubble Cap above
            stateAbove.onNeighborChange(level, posAbove, pos);
        };
        super.onNeighborChange(state, level, pos, neighbor);
    };

    @Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		if (!context.getLevel().isClientSide()) {
            BubbleCapBlockEntity be = getBlockEntity(context.getLevel(), context.getClickedPos());
            if (be == null) return InteractionResult.PASS;
            if (be.attemptRotation(true)) {
                playRotateSound(context.getLevel(), context.getClickedPos());
                updateAfterWrenched(state, context);
                return InteractionResult.SUCCESS;
            };
        };
		return InteractionResult.PASS;
	};

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        AbstractRememberPlacerBehaviour.setPlacedBy(level, pos, placer);
        super.setPlacedBy(level, pos, state, placer, stack);
    };

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null && be instanceof BubbleCapBlockEntity bubbleCapBE) {
            return bubbleCapBE.getLuminosity();
        };
        return 0;
    };

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return DestroyShapes.bubbleCap(state.getValue(BOTTOM), state.getValue(TOP));
    };

    @Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		IBE.onRemove(state, worldIn, pos, newState);
	};

    /**
     * Returns the appropriate Block State for the Blocks above and below this Bubble Cap.
     * @param level The Level in which this Bubble Cap is
     * @param pos The position of this Bubble Cap
     */
    public BlockState stateForPositionInTower(Level level, BlockPos pos) {
        return defaultBlockState()
            .setValue(TOP, !level.getBlockState(pos.above()).is(DestroyBlocks.BUBBLE_CAP.get()))
            .setValue(BOTTOM, !level.getBlockState(pos.below()).is(DestroyBlocks.BUBBLE_CAP.get()));
    };

    @Override
    public Class<BubbleCapBlockEntity> getBlockEntityClass() {
        return BubbleCapBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BubbleCapBlockEntity> getBlockEntityType() {
        return DestroyBlockEntityTypes.BUBBLE_CAP.get();
    }
    
}
