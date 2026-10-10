package org.taumc.celeritas.impl.render.text;

//? if >=1.8 {
/*import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.render.texture.Texture;
import net.minecraft.client.render.texture.TextureManager;
import net.minecraft.resource.Identifier;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

/^*
 * Collects the glyph quads of one text layer and draws them with a single vertex array instead of one
 * glBegin/glEnd block per glyph.
 *
 * Vanilla draws every glyph as a textured triangle strip of four vertices in immediate mode, which costs about ten
 * GL calls per character. A line of sign text is around a hundred calls, the F3 overlay several thousand. Here the
 * strips are stored as GL_QUADS with the current text colour on each vertex (the fixed-function pipeline multiplies
 * the texel with the vertex colour, which is what glColor did for the immediate-mode glyphs) and drawn in one call per
 * layer, or per font page if a line mixes the default font with unicode pages. The underline and strikethrough quads
 * flush the batch first, so everything is still drawn in the order vanilla draws it.
 *
 * The vertices go straight into native memory. The vanilla vertex builder looks up the vertex format for every
 * attribute of every vertex, which with tens of thousands of glyphs per frame cost more than the draw calls it saved.
 ^/
public final class GlyphBatch {
    /^* Marks the default font texture; unicode pages use their page index (0..255). ^/
    public static final int DEFAULT_FONT = -1;

    // position (3 floats), texture coordinates (2 floats), colour (4 bytes)
    private static final int VERTEX_SIZE = 24;
    private static final int TEXTURE_OFFSET = 12;
    private static final int COLOR_OFFSET = 20;
    // 96 KiB, about 1,000 glyphs; grows when a layer needs more
    private static final int INITIAL_CAPACITY = 4096;

    private long pBuffer; // byte*
    private int capacity;
    private int vertexCount;

    private int texture = DEFAULT_FONT;

    private float r = 1.0F, g = 1.0F, b = 1.0F, a = 1.0F;
    private byte red = -1, green = -1, blue = -1, alpha = -1;

    private final float[] xs = new float[4];
    private final float[] ys = new float[4];
    private final float[] zs = new float[4];
    private final float[] us = new float[4];
    private final float[] vs = new float[4];
    private float u, v;
    private int count;

    private Identifier boundIdentifier;
    private int boundGlId;

    /^* Starts a text layer. Glyphs left over from a layer that threw are thrown away. ^/
    public void begin() {
        this.vertexCount = 0;
        this.count = 0;
        this.boundIdentifier = null;
    }

    /^* Mirrors the glColor calls the text renderer makes; the colour applies to the glyphs that follow. ^/
    public void setColor(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.red = toByte(r);
        this.green = toByte(g);
        this.blue = toByte(b);
        this.alpha = toByte(a);
    }

    private static byte toByte(float component) {
        int value = Math.round(component * 255.0F);
        return (byte) (value < 0 ? 0 : Math.min(value, 255));
    }

    /^* Called before the text renderer binds a font texture. Glyphs of another texture are drawn first. ^/
    public void selectTexture(int texture) {
        if (texture != this.texture) {
            this.flush();
            this.texture = texture;
        }
    }

    /^*
     * Binds a font texture like {@link TextureManager#bind} does, but looks the texture up only once per layer.
     * Vanilla binds the texture again for every glyph, which is a map lookup each time; the GL bind itself is
     * cached by GlStateManager either way.
     ^/
    public void bindTexture(TextureManager textureManager, Identifier identifier) {
        if (identifier != this.boundIdentifier) {
            Texture texture = textureManager.get(identifier);
            if (texture == null) {
                // not loaded yet: let the texture manager load and bind it as it would in vanilla
                textureManager.bind(identifier);
                return;
            }
            this.boundIdentifier = identifier;
            this.boundGlId = texture.getGlId();
        }
        GlStateManager.bindTexture(this.boundGlId);
    }

    public void beginGlyph() {
        this.count = 0;
    }

    public void texCoord(float u, float v) {
        this.u = u;
        this.v = v;
    }

    public void vertex(float x, float y, float z) {
        int i = this.count;
        if (i >= 4) {
            return;
        }
        this.xs[i] = x;
        this.ys[i] = y;
        this.zs[i] = z;
        this.us[i] = this.u;
        this.vs[i] = this.v;
        this.count = i + 1;
    }

    /^*
     * Stores the finished glyph. Vanilla emits the strip as top left, bottom left, top right, bottom right; as a quad
     * that is top left, bottom left, bottom right, top right. Both cover the same parallelogram.
     ^/
    public void endGlyph() {
        if (this.count != 4) {
            this.count = 0;
            return;
        }
        this.count = 0;

        this.reserve(4);
        this.put(0);
        this.put(1);
        this.put(3);
        this.put(2);
    }

    private void reserve(int vertices) {
        int needed = this.vertexCount + vertices;
        if (this.pBuffer == 0L) {
            this.capacity = INITIAL_CAPACITY;
            this.pBuffer = MemoryUtil.nmemAllocChecked((long) this.capacity * VERTEX_SIZE);
        } else if (needed > this.capacity) {
            while (needed > this.capacity) {
                this.capacity *= 2;
            }
            this.pBuffer = MemoryUtil.nmemReallocChecked(this.pBuffer, (long) this.capacity * VERTEX_SIZE);
        }
    }

    private void put(int i) {
        long pVertex = this.pBuffer + (long) this.vertexCount * VERTEX_SIZE; // byte*
        MemoryUtil.memPutFloat(pVertex, this.xs[i]);
        MemoryUtil.memPutFloat(pVertex + 4, this.ys[i]);
        MemoryUtil.memPutFloat(pVertex + 8, this.zs[i]);
        MemoryUtil.memPutFloat(pVertex + TEXTURE_OFFSET, this.us[i]);
        MemoryUtil.memPutFloat(pVertex + TEXTURE_OFFSET + 4, this.vs[i]);
        MemoryUtil.memPutByte(pVertex + COLOR_OFFSET, this.red);
        MemoryUtil.memPutByte(pVertex + COLOR_OFFSET + 1, this.green);
        MemoryUtil.memPutByte(pVertex + COLOR_OFFSET + 2, this.blue);
        MemoryUtil.memPutByte(pVertex + COLOR_OFFSET + 3, this.alpha);
        this.vertexCount++;
    }

    /^*
     * Draws the collected glyphs, if any, the way the vanilla buffer uploader draws a POSITION_TEX_COLOR buffer.
     * Drawing from a colour array leaves the current GL colour undefined, so it is set again to what the last
     * glColor call made it: the underline quads and whatever vanilla draws next without setting a colour of its
     * own depend on it, as they did after the immediate-mode glyphs.
     ^/
    public void flush() {
        int vertices = this.vertexCount;
        if (vertices == 0) {
            return;
        }
        this.vertexCount = 0;

        long pBuffer = this.pBuffer; // byte*
        GL11.glVertexPointer(3, GL11.GL_FLOAT, VERTEX_SIZE, pBuffer);
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GLX.clientActiveTexture(GLX.GL_TEXTURE0);
        GL11.glTexCoordPointer(2, GL11.GL_FLOAT, VERTEX_SIZE, pBuffer + TEXTURE_OFFSET);
        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, VERTEX_SIZE, pBuffer + COLOR_OFFSET);
        GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);

        GL11.glDrawArrays(GL11.GL_QUADS, 0, vertices);

        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
        GlStateManager.clearColor();
        GL11.glColor4f(this.r, this.g, this.b, this.a);
    }
}

*///?}
