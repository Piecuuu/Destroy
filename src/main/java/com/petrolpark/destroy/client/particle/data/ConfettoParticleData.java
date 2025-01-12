package com.petrolpark.destroy.client.particle.data;

import com.petrolpark.destroy.client.particle.ConfettoParticle;
import com.petrolpark.destroy.client.particle.DestroyParticleTypes;
import com.simibubi.create.content.equipment.bell.BasicParticleData;

import net.minecraft.core.particles.ParticleType;

public class ConfettoParticleData extends BasicParticleData<ConfettoParticle> {
    
    @Override
    public ParticleType<?> getType() {
        return DestroyParticleTypes.CONFETTO.get();
    };

    @Override
    public IBasicParticleFactory<ConfettoParticle> getBasicFactory() {
        return ConfettoParticle::new;
    };

    public static class White extends ConfettoParticleData {

        @Override
        public ParticleType<?> getType() {
            return DestroyParticleTypes.WHITE_CONFETTO.get();
        }
    };
};
