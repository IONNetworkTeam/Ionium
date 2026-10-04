package org.taumc.ionium.remapper;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.adapter.MappingNsRenamer;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.tinyremapper.IMappingProvider;
import net.fabricmc.tinyremapper.NonClassCopyMode;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.extension.mixin.MixinExtension;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Remaps the Ornithe 1.8.9 build (Calamus intermediary names) to the SRG names Forge 1.8.9 uses at runtime,
 * including Mixin annotation targets. The two mapping sets are joined on Minecraft's obfuscated (official) names.
 * Runs as its own process so its tiny-remapper never meets the one Loom brings.
 */
public final class RemapToSrg {
    private RemapToSrg() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 5) {
            throw new IllegalArgumentException("usage: <input jar> <calamus jar> <mcp srg zip> <minecraft jar> <output jar>");
        }
        Path inputJar = Path.of(args[0]);
        Path calamusJar = Path.of(args[1]);
        Path srgZip = Path.of(args[2]);
        Path minecraftJar = Path.of(args[3]);
        Path out = Path.of(args[4]);
        MemoryMappingTree tree = new MemoryMappingTree();
        try (ZipFile calamus = new ZipFile(calamusJar.toFile());
             Reader reader = entryReader(calamus, "mappings/mappings.tiny")) {
            MappingReader.read(reader, tree);
        }
        try (ZipFile mcp = new ZipFile(srgZip.toFile());
             Reader reader = entryReader(mcp, "joined.srg")) {
            // SRG files name their sides "source"/"target"; here source is the official namespace.
            MappingReader.read(reader, MappingFormat.SRG_FILE,
                    new MappingNsRenamer(tree, Map.of("source", "official", "target", "srg")));
        }

        Path intermediaryMinecraft = out.resolveSibling("minecraft-intermediary.jar");
        Files.deleteIfExists(intermediaryMinecraft);
        run(tree, "official", "intermediary", minecraftJar, intermediaryMinecraft, null, false);

        Files.deleteIfExists(out);
        run(tree, "intermediary", "srg", inputJar, out, intermediaryMinecraft, true);
    }

    private static Reader entryReader(ZipFile zip, String name) throws IOException {
        ZipEntry entry = zip.getEntry(name);
        if (entry == null) {
            throw new IOException(name + " not found in " + zip.getName());
        }
        return new InputStreamReader(zip.getInputStream(entry), StandardCharsets.UTF_8);
    }

    private static void run(MappingTree tree, String from, String to, Path input, Path output, Path classpath,
                            boolean mixin) throws IOException {
        TinyRemapper.Builder builder = TinyRemapper.newRemapper()
                .withMappings(provider(tree, from, to))
                .ignoreConflicts(true)
                .renameInvalidLocals(false);
        if (mixin) {
            builder.extension(new MixinExtension());
        }
        TinyRemapper remapper = builder.build();
        try (OutputConsumerPath consumer = new OutputConsumerPath.Builder(output).build()) {
            consumer.addNonClassFiles(input, NonClassCopyMode.FIX_META_INF, remapper);
            remapper.readInputs(input);
            if (classpath != null) {
                remapper.readClassPath(classpath);
            }
            remapper.apply(consumer);
        } finally {
            remapper.finish();
        }
    }

    private static IMappingProvider provider(MappingTree tree, String from, String to) {
        int src = tree.getNamespaceId(from);
        int dst = tree.getNamespaceId(to);
        return acceptor -> {
            for (MappingTree.ClassMapping cls : tree.getClasses()) {
                String owner = cls.getName(src);
                String target = cls.getName(dst);
                if (owner == null) {
                    continue;
                }
                if (target != null) {
                    acceptor.acceptClass(owner, target);
                }
                for (MappingTree.MethodMapping method : cls.getMethods()) {
                    String name = method.getName(src);
                    String desc = method.getDesc(src);
                    String mapped = method.getName(dst);
                    if (name != null && desc != null && mapped != null) {
                        acceptor.acceptMethod(new IMappingProvider.Member(owner, name, desc), mapped);
                    }
                }
                for (MappingTree.FieldMapping field : cls.getFields()) {
                    String name = field.getName(src);
                    String desc = field.getDesc(src);
                    String mapped = field.getName(dst);
                    if (name != null && mapped != null) {
                        acceptor.acceptField(new IMappingProvider.Member(owner, name, desc), mapped);
                    }
                }
            }
        };
    }
}
