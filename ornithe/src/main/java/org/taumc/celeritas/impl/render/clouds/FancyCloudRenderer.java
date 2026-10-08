package org.taumc.celeritas.impl.render.clouds;

//? if >=1.8 {
/*import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.resource.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

/^*
 * Draws the fancy clouds of 1.8 from four display lists instead of rebuilding about 9,000 vertices every frame.
 *
 * In vanilla the cloud geometry only depends on the frame through a position offset, a texture offset, one colour per
 * face direction and whether the bottom and top faces are drawn. So the lists hold the geometry once, without colours,
 * and each frame applies the offsets with the modelview and texture matrices and the colours with glColor. Everything
 * else (the depth-only first pass, anaglyph colour masks, blending) works like vanilla.
 ^/
public final class FancyCloudRenderer {
    private static final Identifier CLOUDS = new Identifier("textures/environment/clouds.png");

    private static final float TEXEL = 1.0F / 256.0F;
    private static final float EPSILON = 9.765625E-4F;

    private static final int BOTTOM = 0, TOP = 1, SIDES_X = 2, SIDES_Z = 3;

    private static int lists = -1;

    private FancyCloudRenderer() {
    }

    public static void render(Minecraft minecraft, ClientWorld world, int ticks, float tickDelta, int anaglyphPass) {
        if (lists < 0) {
            lists = compile();
        }

        GlStateManager.disableCull();

        Entity camera = minecraft.getCamera();
        float cameraY = (float) (camera.prevTickY + (camera.y - camera.prevTickY) * tickDelta);
        double time = ticks + tickDelta;
        double cloudX = (camera.prevX + (camera.x - camera.prevX) * tickDelta + time * 0.03F) / 12.0;
        double cloudZ = (camera.prevZ + (camera.z - camera.prevZ) * tickDelta) / 12.0 + 0.33F;
        float height = world.dimension.getCloudHeight() - cameraY + 0.33F;
        cloudX -= MathHelper.floor(cloudX / 2048.0) * 2048;
        cloudZ -= MathHelper.floor(cloudZ / 2048.0) * 2048;

        minecraft.getTextureManager().bind(CLOUDS);
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(770, 771, 1, 0);

        Vec3d color = world.getCloudColor(tickDelta);
        float red = (float) color.x;
        float green = (float) color.y;
        float blue = (float) color.z;
        if (anaglyphPass != 2) {
            float gray = (red * 30.0F + green * 59.0F + blue * 11.0F) / 100.0F;
            float anaglyphGreen = (red * 30.0F + green * 70.0F) / 100.0F;
            float anaglyphBlue = (red * 30.0F + blue * 70.0F) / 100.0F;
            red = gray;
            green = anaglyphGreen;
            blue = anaglyphBlue;
        }

        int cellX = MathHelper.floor(cloudX);
        int cellZ = MathHelper.floor(cloudZ);

        // vanilla adds these offsets to every vertex; the scale stays on the matrix like it does in vanilla
        GlStateManager.scalef(12.0F, 1.0F, 12.0F);
        GlStateManager.translatef(-(float) (cloudX - cellX), height, -(float) (cloudZ - cellZ));
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.translatef(cellX * TEXEL, cellZ * TEXEL, 0.0F);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);

        for (int pass = 0; pass < 2; pass++) {
            if (pass == 0) {
                GlStateManager.colorMask(false, false, false, false);
            } else {
                switch (anaglyphPass) {
                    case 0 -> GlStateManager.colorMask(false, true, true, true);
                    case 1 -> GlStateManager.colorMask(true, false, false, true);
                    case 2 -> GlStateManager.colorMask(true, true, true, true);
                }
            }

            if (height > -5.0F) {
                GlStateManager.color4f(red * 0.7F, green * 0.7F, blue * 0.7F, 0.8F);
                GlStateManager.callList(lists + BOTTOM);
            }
            if (height <= 5.0F) {
                GlStateManager.color4f(red, green, blue, 0.8F);
                GlStateManager.callList(lists + TOP);
            }
            GlStateManager.color4f(red * 0.9F, green * 0.9F, blue * 0.9F, 0.8F);
            GlStateManager.callList(lists + SIDES_X);
            GlStateManager.color4f(red * 0.8F, green * 0.8F, blue * 0.8F, 0.8F);
            GlStateManager.callList(lists + SIDES_Z);
        }

        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);

        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
    }

    /^* Builds the 8x8 cells of 8x8 cloud texels around the camera, with the same faces vanilla emits. ^/
    private static int compile() {
        int base = MemoryTracker.getLists(4);

        GL11.glNewList(base + BOTTOM, GL11.GL_COMPILE);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glNormal3f(0.0F, -1.0F, 0.0F);
        for (int cx = -3; cx <= 4; cx++) {
            for (int cz = -3; cz <= 4; cz++) {
                horizontal(cx * 8, cz * 8, 0.0F);
            }
        }
        GL11.glEnd();
        GL11.glEndList();

        GL11.glNewList(base + TOP, GL11.GL_COMPILE);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        for (int cx = -3; cx <= 4; cx++) {
            for (int cz = -3; cz <= 4; cz++) {
                horizontal(cx * 8, cz * 8, 4.0F - EPSILON);
            }
        }
        GL11.glEnd();
        GL11.glEndList();

        GL11.glNewList(base + SIDES_X, GL11.GL_COMPILE);
        GL11.glBegin(GL11.GL_QUADS);
        for (int cx = -3; cx <= 4; cx++) {
            for (int cz = -3; cz <= 4; cz++) {
                float x = cx * 8;
                float z = cz * 8;
                for (int i = 0; i < 8; i++) {
                    float u = (x + i + 0.5F) * TEXEL;
                    if (cx > -1) {
                        GL11.glNormal3f(-1.0F, 0.0F, 0.0F);
                        sideX(x + i, z, u);
                    }
                    if (cx <= 1) {
                        GL11.glNormal3f(1.0F, 0.0F, 0.0F);
                        sideX(x + i + 1.0F - EPSILON, z, u);
                    }
                }
            }
        }
        GL11.glEnd();
        GL11.glEndList();

        GL11.glNewList(base + SIDES_Z, GL11.GL_COMPILE);
        GL11.glBegin(GL11.GL_QUADS);
        for (int cx = -3; cx <= 4; cx++) {
            for (int cz = -3; cz <= 4; cz++) {
                float x = cx * 8;
                float z = cz * 8;
                for (int i = 0; i < 8; i++) {
                    float v = (z + i + 0.5F) * TEXEL;
                    if (cz > -1) {
                        GL11.glNormal3f(0.0F, 0.0F, -1.0F);
                        sideZ(x, z + i, v);
                    }
                    if (cz <= 1) {
                        GL11.glNormal3f(0.0F, 0.0F, 1.0F);
                        sideZ(x, z + i + 1.0F - EPSILON, v);
                    }
                }
            }
        }
        GL11.glEnd();
        GL11.glEndList();

        return base;
    }

    private static void horizontal(float x, float z, float y) {
        vertex(x, y, z + 8.0F, x * TEXEL, (z + 8.0F) * TEXEL);
        vertex(x + 8.0F, y, z + 8.0F, (x + 8.0F) * TEXEL, (z + 8.0F) * TEXEL);
        vertex(x + 8.0F, y, z, (x + 8.0F) * TEXEL, z * TEXEL);
        vertex(x, y, z, x * TEXEL, z * TEXEL);
    }

    private static void sideX(float x, float z, float u) {
        vertex(x, 0.0F, z + 8.0F, u, (z + 8.0F) * TEXEL);
        vertex(x, 4.0F, z + 8.0F, u, (z + 8.0F) * TEXEL);
        vertex(x, 4.0F, z, u, z * TEXEL);
        vertex(x, 0.0F, z, u, z * TEXEL);
    }

    private static void sideZ(float x, float z, float v) {
        vertex(x, 4.0F, z, x * TEXEL, v);
        vertex(x + 8.0F, 4.0F, z, (x + 8.0F) * TEXEL, v);
        vertex(x + 8.0F, 0.0F, z, (x + 8.0F) * TEXEL, v);
        vertex(x, 0.0F, z, x * TEXEL, v);
    }

    private static void vertex(float x, float y, float z, float u, float v) {
        GL11.glTexCoord2f(u, v);
        GL11.glVertex3f(x, y, z);
    }
}
*///?}
