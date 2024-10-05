package com.petrolpark.destroy.block.entity;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.petrolpark.destroy.advancement.DestroyAdvancementTrigger;
import com.petrolpark.destroy.block.ColorimeterBlock;
import com.petrolpark.destroy.block.display.MixtureContentsDisplaySource;
import com.petrolpark.destroy.block.entity.behaviour.DestroyAdvancementBehaviour;
import com.petrolpark.destroy.block.entity.behaviour.RedstoneQuantityMonitorBehaviour;
import com.petrolpark.destroy.chemistry.legacy.LegacySpecies;
import com.petrolpark.destroy.chemistry.legacy.ReadOnlyMixture;
import com.petrolpark.destroy.fluid.DestroyFluids;
import com.petrolpark.destroy.util.DestroyLang;
import com.petrolpark.destroy.util.vat.VatMaterial;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.DisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

public class ColorimeterBlockEntity extends SmartBlockEntity {

    public static final DecimalFormat df = new DecimalFormat();
    static {
        df.setMinimumFractionDigits(8);
        df.setMaximumFractionDigits(8);
    };

    public boolean observingGas;
    protected LegacySpecies molecule;
    public RedstoneQuantityMonitorBehaviour redstoneMonitor;

    protected DestroyAdvancementBehaviour advancementBehaviour;

    public ColorimeterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    };

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        redstoneMonitor = new RedstoneQuantityMonitorBehaviour(this)
            .withLabel(f -> DestroyLang.translate("tooltip.colorimeter.menu.current_concentration", df.format(f)).component())
            .onStrengthChanged(strength -> getLevel().setBlockAndUpdate(getBlockPos(), getBlockState().setValue(ColorimeterBlock.POWERED, strength != 0)));
        behaviours.add(redstoneMonitor);

        advancementBehaviour = new DestroyAdvancementBehaviour(this, DestroyAdvancementTrigger.COLORIMETER);
        behaviours.add(advancementBehaviour);
    };

    @Override
    public void tick() {
        super.tick();
        if (molecule != null && getVatOptional().isPresent()) advancementBehaviour.awardDestroyAdvancement(DestroyAdvancementTrigger.COLORIMETER);
    };

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        setMolecule(LegacySpecies.getMolecule(tag.getString("Molecule")));
        if (tag.contains("ObservingGas")) observingGas = true;
    };

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        if (molecule != null) tag.putString("Molecule", molecule.getFullID());
        if (observingGas) tag.putBoolean("ObservingGas", true);
    };

    public void configure(LegacySpecies observedMolecule, boolean observingGas) {
        this.observingGas = observingGas;
        setMolecule(observedMolecule);
    };

    public LegacySpecies getMolecule() {
        return molecule;
    };

    public void setMolecule(LegacySpecies molecule) {
        this.molecule = molecule;
        updateVat();
        notifyUpdate();
    };

    public Optional<VatControllerBlockEntity> getVatOptional() {
        if (!hasLevel()) return Optional.empty();
        BlockPos vatPos = getBlockPos().relative(getBlockState().getValue(ColorimeterBlock.FACING));
        return getLevel().getBlockEntity(vatPos, DestroyBlockEntityTypes.VAT_SIDE.get()).map(vbe -> {
            if (!VatMaterial.getMaterial(vbe.getMaterial()).map(VatMaterial::transparent).orElse(false)) return null;
            return vbe.getController();
        });
    };

    public void updateVat() {
        Optional<VatControllerBlockEntity> vat = getVatOptional();
        if (molecule != null && vat.isPresent()) {
            redstoneMonitor.quantityObserved = Optional.of(() -> {
                FluidStack mixtureStack = (observingGas ? vat.get().getGasTankContents() : vat.get().getLiquidTankContents());
                if (DestroyFluids.isMixture(mixtureStack)) {
                    ReadOnlyMixture mixture = ReadOnlyMixture.readNBT(ReadOnlyMixture::new, mixtureStack.getOrCreateChildTag("Mixture"));
                    return mixture.getConcentrationOf(molecule);
                };
                return 0f;
            });
            return;
        };
        redstoneMonitor.quantityObserved = Optional.empty();
    };

    public static class ColorimeterDisplaySource extends DisplaySource {

        private static final DecimalFormat df = new DecimalFormat();
        static {
            df.setMinimumFractionDigits(3);
            df.setMaximumFractionDigits(3);
        };

        @Override
        public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
            if (!(context.getSourceBlockEntity() instanceof ColorimeterBlockEntity cbe)) return Collections.emptyList();
            Optional<VatControllerBlockEntity> vat = cbe.getVatOptional();
            if (!vat.isPresent()) return Collections.emptyList();
            FluidStack fluid = cbe.observingGas ? vat.get().getGasTankContents() : vat.get().getGasTankContents();
            return Collections.singletonList(
                Lang.builder()
                    .add(context.sourceConfig().getBoolean("ShowSpeciesName") ? cbe.molecule.getName(!context.sourceConfig().getBoolean("MoleculeNameType")).copy().append(" ") : Component.literal(""))
                    .add(DestroyLang.quantity(ReadOnlyMixture.readNBT(ReadOnlyMixture::new, fluid.getOrCreateChildTag("Mixture")).getConcentrationOf(cbe.molecule), false, df))
                    .component() 
            );
        };

        @Override
        public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
            if (isFirstLine) {
                builder.addSelectionScrollInput(0, 137, (si, l) -> {
                si
                    .forOptions(List.of(
                        DestroyLang.translate("display_source.colorimeter.species_name.dont_include").component(), DestroyLang.translate("display_source.colorimeter.species_name").component()
                    ))
                    .titled(DestroyLang.translate("display_source.colorimeter.species_name").component());
                }, "ShowSpeciesName");
            } else {
                MixtureContentsDisplaySource.addMoleculeNameTypeSelection(builder);
            };
        };

    };
    
};
