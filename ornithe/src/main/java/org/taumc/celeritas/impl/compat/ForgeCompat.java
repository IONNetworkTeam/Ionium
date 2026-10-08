package org.taumc.celeritas.impl.compat;

//? if >=1.8 {
/*
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.render.block.BlockLayer;
import net.minecraft.entity.Entity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/^*
 * Calls the rendering hooks Forge patches into 1.8.9, for Ionium's Forge build. This code is compiled against vanilla
 * (Ornithe), so the hooks are looked up at runtime. Forge-added members keep their real names at runtime, and the
 * class literals here are remapped to SRG along with the rest of the jar. On Ornithe every method falls back to what
 * vanilla does.
 ^/
public final class ForgeCompat {
    public static final boolean PRESENT;

    /^* The block layers in ordinal order, so the meshing loop does not allocate a new array per block. ^/
    public static final BlockLayer[] LAYERS = BlockLayer.values();

    private static final MethodHandle GET_RENDER_PASS;
    private static final MethodHandle SET_RENDER_LAYER;
    private static final MethodHandle CAN_RENDER_IN_LAYER;
    private static final MethodHandle HAS_TILE_ENTITY;
    private static final MethodHandle ENTITY_SHOULD_RENDER_IN_PASS;
    private static final MethodHandle BLOCK_ENTITY_SHOULD_RENDER_IN_PASS;

    static {
        MethodHandle getRenderPass = null, setRenderLayer = null, canRenderInLayer = null, hasTileEntity = null,
                entityInPass = null, blockEntityInPass = null;
        boolean present = false;

        try {
            ClassLoader loader = ForgeCompat.class.getClassLoader();
            Class<?> forgeClient = Class.forName("net.minecraftforge.client.MinecraftForgeClient", false, loader);
            Class<?> forgeHooksClient = Class.forName("net.minecraftforge.client.ForgeHooksClient", false, loader);
            var lookup = MethodHandles.publicLookup();

            getRenderPass = lookup.findStatic(forgeClient, "getRenderPass", MethodType.methodType(int.class));
            setRenderLayer = lookup.findStatic(forgeHooksClient, "setRenderLayer", MethodType.methodType(void.class, BlockLayer.class));
            canRenderInLayer = lookup.findVirtual(Block.class, "canRenderInLayer", MethodType.methodType(boolean.class, BlockLayer.class));
            hasTileEntity = lookup.findVirtual(Block.class, "hasTileEntity", MethodType.methodType(boolean.class, BlockState.class));
            entityInPass = lookup.findVirtual(Entity.class, "shouldRenderInPass", MethodType.methodType(boolean.class, int.class));
            blockEntityInPass = lookup.findVirtual(BlockEntity.class, "shouldRenderInPass", MethodType.methodType(boolean.class, int.class));
            present = true;
        } catch (ClassNotFoundException e) {
            // not running on Forge
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Forge is present but its rendering hooks could not be found", e);
        }

        PRESENT = present;
        GET_RENDER_PASS = getRenderPass;
        SET_RENDER_LAYER = setRenderLayer;
        CAN_RENDER_IN_LAYER = canRenderInLayer;
        HAS_TILE_ENTITY = hasTileEntity;
        ENTITY_SHOULD_RENDER_IN_PASS = entityInPass;
        BLOCK_ENTITY_SHOULD_RENDER_IN_PASS = blockEntityInPass;
    }

    private ForgeCompat() {
    }

    /^*
     * Forge calls {@code RenderGlobal.renderEntities} twice per frame: pass 0 for everything, then pass 1 for entities
     * and block entities that asked to be drawn after translucent terrain. Without Forge there is only pass 0.
     ^/
    public static int getRenderPass() {
        if (!PRESENT) {
            return 0;
        }
        try {
            return (int) GET_RENDER_PASS.invokeExact();
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    /^* Tells Forge (and ISmartBlockModels that ask {@code MinecraftForgeClient.getRenderLayer()}) which layer is being built. ^/
    public static void setRenderLayer(BlockLayer layer) {
        if (!PRESENT) {
            return;
        }
        try {
            SET_RENDER_LAYER.invokeExact(layer);
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    /^* Forge lets a block draw into several layers; vanilla draws each block into exactly one. ^/
    public static boolean canRenderInLayer(Block block, BlockLayer layer) {
        if (!PRESENT) {
            return block.getRenderLayer() == layer;
        }
        try {
            return (boolean) CAN_RENDER_IN_LAYER.invokeExact(block, layer);
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    /^* Forge mods can give a block a tile entity depending on its state, without setting vanilla's flag. ^/
    public static boolean hasBlockEntity(Block block, BlockState state) {
        if (!PRESENT) {
            return block.hasBlockEntity();
        }
        try {
            return (boolean) HAS_TILE_ENTITY.invokeExact(block, state);
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    public static boolean shouldRenderInPass(Entity entity, int pass) {
        if (!PRESENT) {
            return pass == 0;
        }
        try {
            return (boolean) ENTITY_SHOULD_RENDER_IN_PASS.invokeExact(entity, pass);
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    public static boolean shouldRenderInPass(BlockEntity blockEntity, int pass) {
        if (!PRESENT) {
            return pass == 0;
        }
        try {
            return (boolean) BLOCK_ENTITY_SHOULD_RENDER_IN_PASS.invokeExact(blockEntity, pass);
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    private static RuntimeException rethrow(Throwable t) {
        if (t instanceof RuntimeException e) {
            throw e;
        }
        if (t instanceof Error e) {
            throw e;
        }
        throw new RuntimeException(t);
    }
}

*///?}
