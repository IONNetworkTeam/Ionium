package org.taumc.celeritas.impl;

/**
 * Loader-neutral identity of the mod. Ionium keeps Celeritas' mod id and packages so upstream changes merge
 * cleanly, and only changes what players see.
 */
public class Celeritas {
    public static final String MODID = "celeritas";
    /** Shown on the F3 screen. */
    public static final String NAME = "Ionium";
    /** Set by the Fabric entrypoint; on Forge it comes from the jar manifest. */
    public static String VERSION = String.valueOf(Celeritas.class.getPackage().getImplementationVersion());
}
