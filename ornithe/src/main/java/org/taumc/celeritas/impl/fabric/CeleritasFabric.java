package org.taumc.celeritas.impl.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.taumc.celeritas.impl.Celeritas;

/** Fabric (Ornithe) entrypoint, kept out of {@link Celeritas} so Forge never loads a Fabric type. */
public class CeleritasFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Celeritas.VERSION = FabricLoader.getInstance().getModContainer(Celeritas.MODID).orElseThrow().getMetadata().getVersion().toString();
    }
}
