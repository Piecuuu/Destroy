package com.petrolpark.destroy.network.packet;

import java.util.function.Supplier;

import com.petrolpark.destroy.client.gui.menu.RedstoneProgrammerMenu;
import com.petrolpark.destroy.client.gui.menu.RedstoneProgrammerMenu.DummyRedstoneProgram;
import com.petrolpark.destroy.config.DestroyAllConfigs;
import com.petrolpark.destroy.network.DestroyMessages;
import com.petrolpark.destroy.util.RedstoneProgram;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent.Context;

public class RedstoneProgramSyncC2SPacket extends C2SPacket {

    public final RedstoneProgram program;

    public RedstoneProgramSyncC2SPacket(RedstoneProgram program) {
        this.program = program;
    };

    public RedstoneProgramSyncC2SPacket(FriendlyByteBuf buffer) {
        program = new DummyRedstoneProgram();
        program.read(buffer);
    };

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        program.write(buffer);
    };

    @Override
    public boolean handle(Supplier<Context> supplier) {
        supplier.get().enqueueWork(() -> {
            ServerPlayer player = supplier.get().getSender();
            AbstractContainerMenu menu = player.containerMenu;
            if (menu instanceof RedstoneProgrammerMenu programMenu) {
                RedstoneProgram program = programMenu.contentHolder;
                program.unload();
                program.copyFrom(this.program);
                program.load();
                program.whenChanged();
                DestroyMessages.sendToClient(new RedstoneProgramSyncReplyS2CPacket(), player);
                if (Math.min(program.getChannels().size() + 1, DestroyAllConfigs.SERVER.blocks.redstoneProgrammerMaxChannels.get()) * 2 > programMenu.slots.size()) programMenu.refreshSlots();
            };
        });
        return true;
    };
    
};
