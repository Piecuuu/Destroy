package com.petrolpark.destroy.client;

import com.petrolpark.destroy.Destroy;

import net.minecraft.world.item.ItemDisplayContext;

public class DestroyItemDisplayContexts {
    
    public static final ItemDisplayContext
    
    BLOWPIPE = ItemDisplayContext.create("blowpipe", Destroy.asResource("blowpipe"), ItemDisplayContext.NONE);
    
    public static final void register() {};
};
