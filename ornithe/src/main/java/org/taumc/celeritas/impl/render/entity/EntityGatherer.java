package org.taumc.celeritas.impl.render.entity;

//? if >=1.8 {
/*
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.TypeInstanceMultiMap;
import net.minecraft.world.chunk.WorldChunk;
import org.taumc.celeritas.mixin.core.ClientChunkCacheAccessor;
import org.taumc.celeritas.mixin.core.WorldChunkAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.taumc.celeritas.impl.compat.ForgeCompat;

public class EntityGatherer {
    /^* Forge draws entities in two passes (see {@link ForgeCompat#getRenderPass()}); vanilla only has pass 0. ^/
    public static final int NUM_PASSES = 2;

    private final List<Entity>[] entityLists;
    private final Consumer<Entity> addEntity;

    @SuppressWarnings("unchecked")
    public EntityGatherer() {
        this.entityLists = new List[NUM_PASSES];
        for (int i = 0; i < NUM_PASSES; i++) {
            this.entityLists[i] = new ArrayList<>();
        }
        var entityLists = this.entityLists;
        if (ForgeCompat.PRESENT) {
            this.addEntity = entity -> {
                for (int i = 0; i < NUM_PASSES; i++) {
                    if (ForgeCompat.shouldRenderInPass(entity, i)) {
                        entityLists[i].add(entity);
                    }
                }
            };
        } else {
            this.addEntity = entityLists[0]::add;
        }
    }

    public void clear() {
        for (int i = 0; i < NUM_PASSES; i++) {
            this.entityLists[i].clear();
        }
    }

    /^*
     * @return the loaded entities, split by the render pass they draw in
     ^/
    public List<Entity>[] getLoadedEntityLists(ClientWorld world) {
        Consumer<Entity> addEntity = this.addEntity;
        // Iterate directly over chunk entity lists where possible - mods may create multipart entities that are not
        // added to the main loadedEntityList.
        if (world.getChunkSource() instanceof ClientChunkCacheAccessor provider) {
            var loadedChunks = provider.getAllChunks();
            for (WorldChunk chunk : loadedChunks) {
                if (!((WorldChunkAccessor)chunk).getHasEntities()) {
                    continue;
                }
                TypeInstanceMultiMap<Entity>[] entityMaps = chunk.getEntities();
                for (TypeInstanceMultiMap<Entity> map : entityMaps) {
                    map.forEach(addEntity);
                }
            }
        } else {
            // Best we can do is the loaded entity list - this will miss some multipart entities
            world.entities.forEach(addEntity);
        }
        return this.entityLists;
    }
}

*///?}