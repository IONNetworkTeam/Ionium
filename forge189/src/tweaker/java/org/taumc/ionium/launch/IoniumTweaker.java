package org.taumc.ionium.launch;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * The jar's TweakClass. Everything else in the jar is Java 21 bytecode, so this class is compiled for Java 8: on a
 * stock Forge 1.8.9 install it still loads and can tell the player what is missing instead of failing with an
 * UnsupportedClassVersionError. When the setup is complete it hands over to Mixin's tweaker, which the manifest
 * used to name directly.
 */
public final class IoniumTweaker implements ITweaker {
    private static final String MIXIN_TWEAKER = "org.spongepowered.asm.launch.MixinTweaker";
    private static final String MIXINS = "org.spongepowered.asm.mixin.Mixins";
    private static final String MIXIN_CONFIG = "mixins.celeritas.json";
    private static final String LAUNCHER_URL = "https://launcher.ion-network.de";
    private static final String SETUP_URL = "https://github.com/IONNetworkTeam/Ionium#what-you-need";

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        List<String> problems = findProblems(classLoader);
        if (!problems.isEmpty()) {
            refuseToStart(problems);
        }
        // Not from the constructor: Launch is still iterating this list while it constructs tweakers.
        @SuppressWarnings("unchecked")
        List<String> tweakClasses = (List<String>) Launch.blackboard.get("TweakClasses");
        tweakClasses.add(MIXIN_TWEAKER);
        addMixinConfig(classLoader);
    }

    /**
     * Mixin only reads MixinConfigs from jars whose TweakClass is its own tweaker, so it never finds this jar's config.
     * And when another jar (8to25, ION Client) already brought MixinTweaker, the one queued above is dropped as a
     * duplicate. Register the config directly; Mixin is already running by now, started by that earlier tweaker.
     */
    private static void addMixinConfig(ClassLoader classLoader) {
        try {
            Class.forName(MIXINS, true, classLoader).getMethod("addConfiguration", String.class).invoke(null, MIXIN_CONFIG);
        } catch (Exception e) {
            throw new IllegalStateException("Ionium couldn't register its mixins", e);
        }
    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.client.main.Main";
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }

    private static List<String> findProblems(ClassLoader classLoader) {
        List<String> problems = new ArrayList<String>();
        int java = javaVersion();
        if (java < 21) {
            problems.add("The game is running on Java " + java + ", but Ionium needs Java 21 or newer.");
        }
        // LWJGL 2 has no org.lwjgl.Version; look the class file up so nothing gets loaded this early
        if (classLoader.getResource("org/lwjgl/Version.class") == null) {
            problems.add("LWJGL 3 isn't loaded. The game is using the old LWJGL 2.");
        }
        if (classLoader.getResource(MIXIN_TWEAKER.replace('.', '/') + ".class") == null) {
            problems.add("Mixin isn't loaded.");
        }
        // OptiFine's own tweakers; FML has already put every tweaker jar from the mods folder on the class path
        if (classLoader.getResource("optifine/OptiFineForgeTweaker.class") != null
                || classLoader.getResource("optifine/OptiFineTweaker.class") != null) {
            problems.add("OptiFine is installed. Ionium and OptiFine can't run together, so remove one of them.");
        }
        return problems;
    }

    private static int javaVersion() {
        String spec = System.getProperty("java.specification.version", "0");
        if (spec.startsWith("1.")) {
            spec = spec.substring(2);
        }
        try {
            return Integer.parseInt(spec);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static void refuseToStart(List<String> problems) {
        StringBuilder message = new StringBuilder("Ionium can't start:\n");
        for (String problem : problems) {
            message.append("\n\u2022 ").append(problem);
        }
        message.append("\n\nThe ION Launcher sets up everything Ionium needs. To play without Ionium,\n")
                .append("remove it from your mods folder. The game will close now.");
        System.err.println("[Ionium] " + message);

        if (!GraphicsEnvironment.isHeadless()) {
            try {
                showDialog(message.toString());
            } catch (Throwable t) {
                System.err.println("[Ionium] Couldn't show the setup dialog: " + t);
            }
        }
        // FML's security manager blocks System.exit here, so stop the launch by failing it
        StringBuilder error = new StringBuilder("Ionium can't start:");
        for (String problem : problems) {
            error.append(' ').append(problem);
        }
        throw new IllegalStateException(error.append(" Get the ION Launcher at ").append(LAUNCHER_URL).toString());
    }

    private static void showDialog(final String message) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                }
                // An owner of our own instead of Swing's shared hidden frame: it gives the dialog a taskbar entry,
                // and once it's disposed AWT can shut down instead of keeping the JVM alive.
                JFrame owner = new JFrame("Ionium");
                try {
                    owner.setUndecorated(true);
                    owner.setIconImage(Toolkit.getDefaultToolkit().getImage(
                            IoniumTweaker.class.getResource("/assets/ionium/icon.png")));
                    owner.setLocationRelativeTo(null);
                    owner.setVisible(true);
                    Object[] options = {"Get the ION Launcher", "Manual setup", "Close"};
                    int choice = JOptionPane.showOptionDialog(owner, message, "Ionium can't start",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                    if (choice == 0) {
                        browse(owner, LAUNCHER_URL);
                    } else if (choice == 1) {
                        browse(owner, SETUP_URL);
                    }
                } finally {
                    owner.dispose();
                }
            }
        });
    }

    private static void browse(JFrame owner, String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
                return;
            }
        } catch (Exception ignored) {
        }
        JOptionPane.showMessageDialog(owner, "Open " + url + " in your browser.", "Ionium",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
