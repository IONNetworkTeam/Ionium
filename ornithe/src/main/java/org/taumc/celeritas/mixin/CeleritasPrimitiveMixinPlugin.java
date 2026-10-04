package org.taumc.celeritas.mixin;

import org.embeddedt.embeddium.impl.util.MixinClassValidator;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.taumc.celeritas.impl.fabric.FabricMixinPath;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class CeleritasPrimitiveMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    /** True when running on Forge (Ionium's Forge 1.8.9 build) rather than Fabric/Ornithe. */
    private static final boolean FORGE = CeleritasPrimitiveMixinPlugin.class.getClassLoader()
            .getResource("net/minecraftforge/fml/relauncher/IFMLLoadingPlugin.class") != null;

    /**
     * Mixins left out on Forge 1.8.9. MinecraftMixin only carries Beta 1.7.3 window fixes and targets LWJGL2's
     * Display, which the LWJGL3 layer renames. ChunkCacheMixin reports the server world's chunks, which only
     * pre-1.3 versions render from; Forge also moved its target call into the async chunk loader.
     */
    private static final Set<String> FORGE_EXCLUDED = Set.of("core.MinecraftMixin", "core.ChunkCacheMixin");

    private Path getMixinPath() {
        String path = "org/taumc/celeritas/mixin";
        if (!FORGE) {
            var pathOpt = FabricMixinPath.find(path);
            if (pathOpt.isPresent()) {
                return pathOpt.get();
            }
        }
        try {
            var resource = CeleritasPrimitiveMixinPlugin.class.getResource("/" + path);
            if (resource == null) {
                return null;
            }
            URI uri = resource.toURI();
            if ("jar".equals(uri.getScheme())) {
                // Forge: the classes sit in a plain jar, so open it as a zip file system to list them
                try {
                    FileSystems.getFileSystem(uri);
                } catch (FileSystemNotFoundException e) {
                    FileSystems.newFileSystem(uri, Map.of());
                }
            }
            Path clPath = Path.of(uri);
            return Files.exists(clPath) ? clPath : null;
        } catch (URISyntaxException | IOException e) {
            throw new IllegalStateException("Cannot locate the mixin package", e);
        }
    }

    private static String mixinClassify(Path baseFolder, Path path) {
        try {
            String className = baseFolder.relativize(path).toString().replace('/', '.').replace('\\', '.');
            return className.substring(0, className.length() - 6);
        } catch(RuntimeException e) {
            throw new IllegalStateException("Error relativizing " + path + " to " + baseFolder, e);
        }
    }

    private static final boolean ENABLED = false;

    @Override
    public List<String> getMixins() {
        Path rootPath = getMixinPath();
        Set<String> possibleMixinClasses = new HashSet<>();
        try(Stream<Path> mixinStream = Files.find(rootPath, Integer.MAX_VALUE, (path, attrs) -> attrs.isRegularFile() && path.getFileName().toString().endsWith(".class"))) {
            mixinStream
                    .map(Path::toAbsolutePath)
                    .filter(MixinClassValidator::isMixinClass)
                    .map(path -> mixinClassify(rootPath, path))
                    .filter(name -> !(FORGE && FORGE_EXCLUDED.contains(name)))
                    .forEach(possibleMixinClasses::add);
        } catch(IOException e) {
            System.err.println("Error reading path");
            e.printStackTrace();
        }

        return List.copyOf(possibleMixinClasses);
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
