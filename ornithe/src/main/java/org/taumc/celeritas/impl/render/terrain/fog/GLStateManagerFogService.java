package org.taumc.celeritas.impl.render.terrain.fog;

import org.embeddedt.embeddium.impl.render.chunk.fog.FogService;
import org.embeddedt.embeddium.impl.render.chunk.shader.ChunkFogMode;
import org.lwjgl.opengl.GL11;

public class GLStateManagerFogService implements FogService {
    public static final GLStateManagerFogService INSTANCE = new GLStateManagerFogService();

    public static float fogColorRed, fogColorGreen, fogColorBlue;

    //? if >=1.8 {
    /*// Mirrors the fog state GlStateManager caches (see GlStateManagerMixin), starting from its defaults. Asking the
    // driver with glGet instead makes Mesa sync with its GL thread, several times per frame.
    public static boolean fogEnabled;
    public static int fogMode = GL11.GL_EXP;
    public static float fogDensity = 1.0F;
    public static float fogStart = 0.0F;
    public static float fogEnd = 1.0F;
    *///?}

    @Override
    public float getFogEnd() {
        //? if <1.8 {
        return GL11.glGetInteger(GL11.GL_FOG_END);
        //?} else
        //return fogEnd;
    }

    @Override
    public float getFogStart() {
        //? if <1.8 {
        return GL11.glGetInteger(GL11.GL_FOG_START);
        //?} else
        //return fogStart;
    }

    @Override
    public float getFogDensity() {
        //? if <1.8 {
        return GL11.glGetFloat(GL11.GL_FOG_DENSITY);
        //?} else
        //return fogDensity;
    }

    @Override
    public int getFogShapeIndex() {
        return 0;
    }

    @Override
    public float getFogCutoff() {
        return getFogEnd();
    }

    @Override
    public float[] getFogColor() {
        return new float[]{fogColorRed, fogColorGreen, fogColorBlue, 1.0F};
    }

    @Override
    public ChunkFogMode getFogMode() {
        //? if <1.8 {
        if (!GL11.glGetBoolean(GL11.GL_FOG)) {
            return ChunkFogMode.NONE;
        }
        return ChunkFogMode.fromGLMode(GL11.glGetInteger(GL11.GL_FOG_MODE));
        //?} else {
        /*if (!fogEnabled) {
            return ChunkFogMode.NONE;
        }
        return ChunkFogMode.fromGLMode(fogMode);
        *///?}
    }
}
