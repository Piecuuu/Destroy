package com.petrolpark.destroy.block;

import javax.annotation.Nullable;

import com.petrolpark.compat.create.block.entity.behaviour.AbstractRememberPlacerBehaviour;
import com.petrolpark.destroy.block.entity.DestroyBlockEntityTypes;
import com.petrolpark.destroy.block.entity.DynamoBlockEntity;
import com.petrolpark.destroy.block.shape.DestroyShapes;
import com.petrolpark.destroy.util.DestroyTags.DestroyBlockTags;
import com.petrolpark.recipe.ingredient.BlockIngredient;
import com.petrolpark.recipe.ingredient.BlockIngredient.BlockTagIngredient;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DynamoBlock extends KineticBlock implements IBE<DynamoBlockEntity> {

    public BlockIngredient<?> arcFurnaceBlockIngredient = BlockIngredient.IMPOSSIBLE;

    public static final EnumProperty<Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final BooleanProperty ARC_FURNACE = BooleanProperty.create("arc_furnace");

    public DynamoBlock(Properties properties) {
        super(properties);
        arcFurnaceBlockIngredient = new BlockTagIngredient(DestroyBlockTags.ARC_FURNACE_TRANSFORMABLE.tag);

        registerDefaultState(
            defaultBlockState()
            .setValue(AXIS, Axis.Z)
            .setValue(ARC_FURNACE, false)
        );
    };

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder
            .add(AXIS)
            .add(ARC_FURNACE);
        super.createBlockStateDefinition(builder);
    };

    @Override
    public Axis getRotationAxis(BlockState state) {
        return Axis.Y;
    };

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (direction != Direction.UP) return 0;
        return getBlockEntityOptional(level, pos).map(DynamoBlockEntity::getRedstoneSignal).orElse(0);
    };

    @Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return face == Direction.UP;
	};

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        AbstractRememberPlacerBehaviour.setPlacedBy(level, pos, placer);
        checkForArcFurnace(level, pos, state);
        super.setPlacedBy(level, pos, state, placer, stack);
    };

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock() && DestroyBlocks.ARC_FURNACE_LID.has(level.getBlockState(pos.below()))) {
            level.setBlock(pos.below(), getBlockEntity(level, pos).arcFurnaceBlock.get(), 3);
        };  
        super.onRemove(state, level, pos, newState, isMoving);
    };

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        checkForArcFurnace(pLevel, pPos, pState);
    };

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext pContext) {
        return state.getValue(ARC_FURNACE) ? DestroyShapes.DYNAMO_ARC_FURNACE : DestroyShapes.DYNAMO;
    };

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (context.getLevel().getBlockState(context.getClickedPos().below()).getBlock() instanceof ArcFurnaceLidBlock) context.getLevel().setBlock(context.getClickedPos().below(), state.cycle(ArcFurnaceLidBlock.AXIS), 3);
        context.getLevel().setBlock(context.getClickedPos(), state.cycle(AXIS), 3);
        return InteractionResult.SUCCESS;
    };

    public void checkForArcFurnace(Level level, BlockPos pos, BlockState state) {
        BlockState stateBelow = level.getBlockState(pos.below());
        if (state.getValue(ARC_FURNACE)) return;
        if (stateBelow.is(DestroyBlocks.CARBON_FIBER_BLOCK.get()) || level.getBlockState(pos.below()).getBlock() instanceof ArcFurnaceLidBlock) {
            level.getBlockEntity(pos, DestroyBlockEntityTypes.DYNAMO.get()).ifPresent(dynamo -> dynamo.arcFurnaceBlock = () -> stateBelow);
            level.setBlock(pos.below(), DestroyBlocks.ARC_FURNACE_LID.getDefaultState().setValue(ArcFurnaceLidBlock.AXIS, state.getValue(AXIS)), 3);
            level.setBlock(pos, state.setValue(ARC_FURNACE, true), 3);
        };
    };

    @Override
    public Class<DynamoBlockEntity> getBlockEntityClass() {
        return DynamoBlockEntity.class;
    };

    @Override
    public BlockEntityType<? extends DynamoBlockEntity> getBlockEntityType() {
        return DestroyBlockEntityTypes.DYNAMO.get();
    };
    
}
