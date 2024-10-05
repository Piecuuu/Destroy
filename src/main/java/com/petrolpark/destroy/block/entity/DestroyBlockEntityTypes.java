package com.petrolpark.destroy.block.entity;

import static com.petrolpark.destroy.Destroy.REGISTRATE;

import com.petrolpark.destroy.block.DestroyBlocks;
import com.petrolpark.destroy.block.entity.SimpleMixtureTankBlockEntity.SimplePlaceableMixtureTankBlockEntity;
import com.petrolpark.destroy.block.instance.CentrifugeCogInstance;
import com.petrolpark.destroy.block.instance.DynamoCogInstance;
import com.petrolpark.destroy.block.instance.HorizontalShaftlessCogwheelInstance;
import com.petrolpark.destroy.block.renderer.AgingBarrelRenderer;
import com.petrolpark.destroy.block.renderer.BlowpipeRenderer;
import com.petrolpark.destroy.block.renderer.BubbleCapRenderer;
import com.petrolpark.destroy.block.renderer.CentrifugeRenderer;
import com.petrolpark.destroy.block.renderer.CoolerRenderer;
import com.petrolpark.destroy.block.renderer.CustomExplosiveMixRenderer;
import com.petrolpark.destroy.block.renderer.DynamoRenderer;
import com.petrolpark.destroy.block.renderer.ElementTankRenderer;
import com.petrolpark.destroy.block.renderer.KeypunchRenderer;
import com.petrolpark.destroy.block.renderer.MechanicalSieveRenderer;
import com.petrolpark.destroy.block.renderer.PollutometerRenderer;
import com.petrolpark.destroy.block.renderer.PumpjackRenderer;
import com.petrolpark.destroy.block.renderer.RedstoneProgrammerRenderer;
import com.petrolpark.destroy.block.renderer.SimpleMixtureTankRenderer;
import com.petrolpark.destroy.block.renderer.SiphonRenderer;
import com.petrolpark.destroy.block.renderer.TestTubeRackRenderer;
import com.petrolpark.destroy.block.renderer.TreeTapRenderer;
import com.petrolpark.destroy.block.renderer.VatRenderer;
import com.petrolpark.destroy.block.renderer.VatSideRenderer;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

public class DestroyBlockEntityTypes {

    public static final BlockEntityEntry<AgingBarrelBlockEntity> AGING_BARREL = REGISTRATE
        .blockEntity("aging_barrel", AgingBarrelBlockEntity::new)
        .validBlocks(DestroyBlocks.AGING_BARREL)
        .renderer(() -> AgingBarrelRenderer::new)
        .register();

    public static final BlockEntityEntry<SimplePlaceableMixtureTankBlockEntity> SIMPLE_MIXTURE_TANK = REGISTRATE
        .blockEntity("simple_mixture_tank", SimplePlaceableMixtureTankBlockEntity::new)
        .validBlocks(DestroyBlocks.BEAKER, DestroyBlocks.ROUND_BOTTOMED_FLASK)
        .renderer(() -> SimpleMixtureTankRenderer::new)
        .register();

    public static final BlockEntityEntry<BlowpipeBlockEntity> BLOWPIPE = REGISTRATE
        .blockEntity("blowpipe", BlowpipeBlockEntity::new)
        .validBlocks(DestroyBlocks.BLOWPIPE)
        .renderer(() -> BlowpipeRenderer::new)
        .register();

    public static final BlockEntityEntry<BubbleCapBlockEntity> BUBBLE_CAP = REGISTRATE
        .blockEntity("bubble_cap", BubbleCapBlockEntity::new)
        .validBlocks(DestroyBlocks.BUBBLE_CAP)
        .renderer(() -> BubbleCapRenderer::new)
        .register();

    public static final BlockEntityEntry<CatalyticConverterBlockEntity> CATALYTIC_CONVERTER = REGISTRATE
        .blockEntity("catalytic_converter", CatalyticConverterBlockEntity::new)
        .validBlocks(DestroyBlocks.CATALYTIC_CONVERTER)
        .register();

    public static final BlockEntityEntry<CentrifugeBlockEntity> CENTRIFUGE = REGISTRATE
        .blockEntity("centrifuge", CentrifugeBlockEntity::new)
        .instance(() -> CentrifugeCogInstance::new)
        .validBlocks(DestroyBlocks.CENTRIFUGE)
        .renderer(() -> CentrifugeRenderer::new)
        .register();

    public static final BlockEntityEntry<ColorimeterBlockEntity> COLORIMETER = REGISTRATE
        .blockEntity("colorimeter", ColorimeterBlockEntity::new)
        .validBlocks(DestroyBlocks.COLORIMETER)
        .register();

    public static final BlockEntityEntry<CoolerBlockEntity> COOLER = REGISTRATE
        .blockEntity("cooler", CoolerBlockEntity::new)
        .validBlocks(DestroyBlocks.COOLER)
        .renderer(() -> CoolerRenderer::new)
        .register();

    public static final BlockEntityEntry<CreativePumpBlockEntity> CREATIVE_PUMP = REGISTRATE
        .blockEntity("creative_pump", CreativePumpBlockEntity::new)
        .validBlocks(DestroyBlocks.CREATIVE_PUMP)
        .register();

    public static final BlockEntityEntry<CustomExplosiveMixBlockEntity> CUSTOM_EXPLOSIVE_MIX = REGISTRATE
        .blockEntity("custom_explosive_mix", CustomExplosiveMixBlockEntity::new)
        .validBlocks(DestroyBlocks.CUSTOM_EXPLOSIVE_MIX)
        .renderer(() -> CustomExplosiveMixRenderer::new)
        .register();

    public static final BlockEntityEntry<DynamiteBlockEntity> DYNAMITE = REGISTRATE
        .blockEntity("dynamite", DynamiteBlockEntity::new)
        .validBlocks(DestroyBlocks.DYNAMITE_BLOCK)
        .register();

    public static final BlockEntityEntry<DynamoBlockEntity> DYNAMO = REGISTRATE
        .blockEntity("dynamo", DynamoBlockEntity::new)
        .instance(() -> DynamoCogInstance::new)
        .validBlocks(DestroyBlocks.DYNAMO)
        .renderer(() -> DynamoRenderer::new)
        .register();
    
    public static final BlockEntityEntry<ElementTankBlockEntity> ELEMENT_TANK = REGISTRATE
        .blockEntity("element_tank", ElementTankBlockEntity::new)
        .validBlocks(DestroyBlocks.ELEMENT_TANK)
        .renderer(() -> ElementTankRenderer::new)
        .register();

    public static final BlockEntityEntry<ExtrusionDieBlockEntity> EXTRUSION_DIE = REGISTRATE
        .blockEntity("extrusion_die", ExtrusionDieBlockEntity::new)
        .validBlocks(DestroyBlocks.EXTRUSION_DIE)
        .register();

    public static final BlockEntityEntry<KeypunchBlockEntity> KEYPUNCH = REGISTRATE
        .blockEntity("keypunch", KeypunchBlockEntity::new)
        .instance(() -> HorizontalShaftlessCogwheelInstance::new)
        .validBlocks(DestroyBlocks.KEYPUNCH)
        .renderer(() -> KeypunchRenderer::new)
        .register();

    public static final BlockEntityEntry<MeasuringCylinderBlockEntity> MEASURING_CYLINDER = REGISTRATE
        .blockEntity("measuring_cylinder", MeasuringCylinderBlockEntity::new)
        .validBlock(DestroyBlocks.MEASURING_CYLINDER)
        .renderer(() -> SimpleMixtureTankRenderer::new)
        .register();

    public static final BlockEntityEntry<MechanicalSieveBlockEntity> MECHANICAL_SIEVE = REGISTRATE
        .blockEntity("mechanical_sieve", MechanicalSieveBlockEntity::new)
        .validBlock(DestroyBlocks.MECHANICAL_SIEVE)
        .renderer(() -> MechanicalSieveRenderer::new)
        .register();

    public static final BlockEntityEntry<PollutometerBlockEntity> POLLUTOMETER = REGISTRATE
        .blockEntity("pollutometer", PollutometerBlockEntity::new)
        .validBlocks(DestroyBlocks.POLLUTOMETER)
        .renderer(() -> PollutometerRenderer::new)
        .register();

    public static final BlockEntityEntry<PumpjackBlockEntity> PUMPJACK = REGISTRATE
        .blockEntity("pumpjack", PumpjackBlockEntity::new)
		//.instance(() -> PumpjackInstance::new, false) Can't use instancing because that can't render cutout for some reason
		.validBlocks(DestroyBlocks.PUMPJACK)
		.renderer(() -> PumpjackRenderer::new)
		.register();

    public static final BlockEntityEntry<PumpjackCamBlockEntity> PUMPJACK_CAM = REGISTRATE
        .blockEntity("pumpjack_cam", PumpjackCamBlockEntity::new)
		.validBlocks(DestroyBlocks.PUMPJACK_CAM)
		.register();

    public static final BlockEntityEntry<RedstoneProgrammerBlockEntity> REDSTONE_PROGRAMMER = REGISTRATE
        .blockEntity("redstone_programmer", RedstoneProgrammerBlockEntity::new)
        .validBlocks(DestroyBlocks.REDSTONE_PROGRAMMER)
        .renderer(() -> RedstoneProgrammerRenderer::new)
        .register();

    public static final BlockEntityEntry<SandCastleBlockEntity> SAND_CASTLE = REGISTRATE
        .blockEntity("sand_castle", SandCastleBlockEntity::new)
        .validBlocks(DestroyBlocks.SAND_CASTLE)
        .register();

    public static final BlockEntityEntry<SiphonBlockEntity> SIPHON = REGISTRATE
        .blockEntity("siphon", SiphonBlockEntity::new)
        .validBlocks(DestroyBlocks.SIPHON)
        .renderer(() -> SiphonRenderer::new)
        .register();

    public static final BlockEntityEntry<TestTubeRackBlockEntity> TEST_TUBE_RACK = REGISTRATE
        .blockEntity("test_tube_rack", TestTubeRackBlockEntity::new)
        .validBlocks(DestroyBlocks.TEST_TUBE_RACK)
        .renderer(() -> TestTubeRackRenderer::new)
        .register();

    public static final BlockEntityEntry<TreeTapBlockEntity> TREE_TAP = REGISTRATE
        .blockEntity("tree_tap", TreeTapBlockEntity::new)
        .validBlock(DestroyBlocks.TREE_TAP)
        .renderer(() -> TreeTapRenderer::new)
        .register();

    public static final BlockEntityEntry<VatControllerBlockEntity> VAT_CONTROLLER = REGISTRATE
        .blockEntity("vat_controller", VatControllerBlockEntity::new)
        .validBlock(DestroyBlocks.VAT_CONTROLLER)
        .renderer(() -> VatRenderer::new)
        .register();

    public static final BlockEntityEntry<VatSideBlockEntity> VAT_SIDE = REGISTRATE
        .blockEntity("vat_side", VatSideBlockEntity::new)
        .validBlock(DestroyBlocks.VAT_SIDE)
        .renderer(() -> VatSideRenderer::new)
        .register();

    public static void register() {};
    
};
