package com.petrolpark.destroy.effect;

import java.util.List;

import com.petrolpark.destroy.MoveToPetrolparkLibrary;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;

@MoveToPetrolparkLibrary
public class UncurableMobEffect extends DestroyMobEffect {
    public UncurableMobEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    };
    
    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }
}
