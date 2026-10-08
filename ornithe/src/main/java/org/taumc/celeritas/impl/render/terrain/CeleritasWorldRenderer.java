package org.taumc.celeritas.impl.render.terrain;

import lombok.Getter;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.embeddedt.embeddium.impl.gl.device.CommandList;
import org.embeddedt.embeddium.impl.render.chunk.ChunkRenderMatrices;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkMeshFormats;
import org.embeddedt.embeddium.impl.render.terrain.SimpleWorldRenderer;
import org.taumc.celeritas.impl.render.terrain.fog.GLStateManagerFogService;
//? if >=1.8
//import org.taumc.celeritas.impl.compat.ForgeCompat;
//? if >=1.8
//import org.embeddedt.embeddium.impl.render.viewport.Viewport;
import org.taumc.celeritas.impl.extensions.RenderGlobalExtension;
import org.taumc.celeritas.impl.render.terrain.matrix.PrimitiveChunkMatrixGetter;
import org.taumc.celeritas.mixin.core.MinecraftAccessor;

import java.util.*;

/**
 * Provides an extension to vanilla's world renderer.
 */
public class CeleritasWorldRenderer extends SimpleWorldRenderer<World, PrimitiveRenderSectionManager, Object, BlockEntity, Float> {
    @Getter
    private SpriteTransparencyTracker transparencyTracker;

    /**
     * @return The CeleritasWorldRenderer based on the current dimension
     */
    public static CeleritasWorldRenderer instance() {
        var instance = instanceNullable();

        if (instance == null) {
            throw new IllegalStateException("No renderer attached to active world");
        }

        return instance;
    }

    /**
     * @return The CeleritasWorldRenderer based on the current dimension, or null if none is attached
     */
    public static CeleritasWorldRenderer instanceNullable() {
        var world = MinecraftAccessor.celeritas$getInstance().worldRenderer;

        if (world instanceof RenderGlobalExtension extension) {
            return extension.sodium$getWorldRenderer();
        }

        return null;
    }

    @Override
    protected void loadWorld(World world) {
        this.transparencyTracker = new SpriteTransparencyTracker();
        super.loadWorld(world);
    }

    public static CameraState captureCameraState(double ticks) {
        //? if <1.8 {
        Entity viewEntity = MinecraftAccessor.celeritas$getInstance().camera;
        //?} else
        //Entity viewEntity = MinecraftAccessor.celeritas$getInstance().getCamera();

        Objects.requireNonNull(viewEntity, "Client must have view entity");

        double x = viewEntity.prevTickX + (viewEntity.x - viewEntity.prevTickX) * ticks;
        double y = viewEntity.prevTickY + (viewEntity.y - viewEntity.prevTickY) * ticks + (double) viewEntity.getEyeHeight();
        double z = viewEntity.prevTickZ + (viewEntity.z - viewEntity.prevTickZ) * ticks;

        float pitch = viewEntity.pitch;
        float yaw = viewEntity.yaw;
        float fogDistance = GLStateManagerFogService.INSTANCE.getFogCutoff();

        return new CameraState(x, y, z, pitch, yaw, fogDistance);
    }

    @Override
    public int getEffectiveRenderDistance() {
        //? if <1.7 {
        int viewDist = MinecraftAccessor.celeritas$getInstance().options.viewDistance;
        if (viewDist > 4) {
            System.err.println("View distance cannot be zero, resetting");
            MinecraftAccessor.celeritas$getInstance().options.viewDistance = viewDist = 0;
        }
        return 16 >> viewDist;
        //?} else
        //return MinecraftAccessor.celeritas$getInstance().options.viewDistance;
    }

    @Override
    public int getMinimumBuildHeight() {
        return 0;
    }

    @Override
    public int getMaximumBuildHeight() {
        //? if >=1.2 {
        return this.world.getHeight();
        //?} else
        //return 128;
    }

    @Override
    public String getChunksDebugString() {
        return super.getChunksDebugString() + "S: " + this.renderSectionManager.getSectionsWithSkyLight().size();
    }

    //? if >=1.8 {
    /*// Both matrices stay the same from setupTerrain through the translucent layer, so they are read once per frame
    // instead of once per layer. Each glGet makes Mesa sync with its GL thread.
    private ChunkRenderMatrices frameMatrices;

    @Override
    public void setupTerrain(Viewport viewport, CameraState cameraState, int frame, boolean spectator,
                             boolean updateChunksImmediately) {
        this.frameMatrices = null;
        super.setupTerrain(viewport, cameraState, frame, spectator, updateChunksImmediately);
    }
    *///?}

    @Override
    protected ChunkRenderMatrices createChunkRenderMatrices() {
        //? if <1.8 {
        return PrimitiveChunkMatrixGetter.getMatrices();
        //?} else {
        /*if (this.frameMatrices == null) {
            this.frameMatrices = PrimitiveChunkMatrixGetter.getMatrices();
        }
        return this.frameMatrices;
        *///?}
    }

    @Override
    protected PrimitiveRenderSectionManager createRenderSectionManager(CommandList commandList) {
        return PrimitiveRenderSectionManager.create(ChunkMeshFormats.VANILLA_LIKE, this.world, this.renderDistance, commandList);
    }

    @Override
    protected void renderBlockEntityList(List<BlockEntity> list, Float partialTicksBoxed) {
        float partialTicks = partialTicksBoxed;
        //? if >=1.8
        //int pass = ForgeCompat.getRenderPass();
        for (var blockEntity : list) {
            //? if >=1.8 {
            /*// Forge renders block entities in two passes per frame; draw each only in the pass it asked for
            if (!ForgeCompat.shouldRenderInPass(blockEntity, pass)) {
                continue;
            }
            *///?}
            try {
                BlockEntityRenderDispatcher.INSTANCE.render(blockEntity, partialTicks /*? if >=1.8 {*//*, -1 *//*?}*/);
            } catch(RuntimeException e) {
                if(blockEntity.isRemoved()) {
                    System.err.println("Suppressing crash from invalid tile entity");
                } else {
                    throw e;
                }
            }
        }
    }
}
