package org.taumc.celeritas.impl.fabric;

import net.fabricmc.loader.api.FabricLoader;
import org.taumc.celeritas.impl.Celeritas;

import java.nio.file.Path;
import java.util.Optional;

/** Looks up the mixin package through Fabric's mod container. Only loaded when Fabric Loader is present. */
public final class FabricMixinPath {
    private FabricMixinPath() {
    }

    public static Optional<Path> find(String path) {
        return FabricLoader.getInstance().getModContainer(Celeritas.MODID).orElseThrow().findPath(path);
    }
}
