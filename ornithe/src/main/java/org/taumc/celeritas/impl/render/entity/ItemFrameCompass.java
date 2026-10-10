package org.taumc.celeritas.impl.render.entity;

//? if >=1.8 {
/*import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.texture.AbstractTexture;
import net.minecraft.client.render.texture.CompassSprite;
import net.minecraft.client.render.texture.TextureAtlas;
import net.minecraft.client.render.texture.TextureAtlasSprite;
import net.minecraft.client.resource.model.BakedModel;
import net.minecraft.client.resource.model.BakedQuad;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import org.taumc.celeritas.mixin.core.TextureAtlasSpriteAccessor;

import java.util.List;

/^*
 * Draws compasses in item frames without rewriting the block atlas.
 *
 * Vanilla has one compass sprite in the atlas, so for every item frame holding a compass it uploads the frame that
 * points the right way for that item frame, draws the item, and afterwards ticks the sprite back to the frame of the
 * player, which uploads again. Two texture uploads per compass per frame, into a texture the whole world is drawn
 * from, is a stall on most drivers and was a third of the frame time in the IoniumBench entities scene.
 *
 * Instead the frames of the sprite are kept in a strip texture of their own (one frame below the other, level 0 only:
 * items are drawn with nearest filtering and no mipmaps). While the item model is drawn the strip is bound in place
 * of the atlas and the texture matrix maps the atlas coordinates of the sprite onto the wanted frame, which samples
 * the same texels vanilla would have uploaded. The atlas keeps the frame of the player, so the tick that vanilla
 * makes afterwards only uploads when the player turns, as it does without item frames around.
 *
 * Only item models whose texture coordinates all lie inside the compass sprite are drawn this way; anything else
 * falls back to the vanilla upload.
 ^/
public final class ItemFrameCompass {
    private static final float UV_EPSILON = 1.0E-5F;

    private static int textureId = -1;
    private static int[] uploadedFrameData;
    private static int uploadedFrames, uploadedWidth, uploadedHeight;

    private static BakedModel checkedModel;
    private static boolean checkedModelInsideSprite;

    private static CompassSprite sprite;
    private static int frame;
    private static boolean pending;
    private static boolean bound;

    private ItemFrameCompass() {
    }

    /^*
     * Replaces the sprite tick vanilla makes before drawing the item (angle interpolation skipped). Works out the
     * frame the compass in the item frame shows, like the sprite would, but leaves the atlas alone.
     *
     * @return false if there is nothing to draw this way and the caller should run the vanilla tick
     ^/
    public static boolean prepare(CompassSprite sprite, World world, double x, double z, double yaw) {
        int frames = sprite.getFrameCount();
        if (frames <= 0) {
            return false;
        }

        double angle = 0.0;
        if (world != null) {
            BlockPos spawn = world.getSpawnPoint();
            double dx = spawn.getX() - x;
            double dz = spawn.getZ() - z;
            yaw %= 360.0;
            angle = -((yaw - 90.0) * Math.PI / 180.0 - Math.atan2(dz, dx));
            if (!world.dimension.isOverworld()) {
                angle = Math.random() * (float) Math.PI * 2.0;
            }
        }

        int i = (int) ((angle / (Math.PI * 2) + 1.0) * frames) % frames;
        while (i < 0) {
            i = (i + frames) % frames;
        }

        ItemFrameCompass.sprite = sprite;
        ItemFrameCompass.frame = i;
        pending = true;
        return true;
    }

    /^* Called by the item renderer right before the quads of the model are drawn. ^/
    public static void beginModel(BakedModel model) {
        if (!pending) {
            return;
        }
        pending = false;

        CompassSprite sprite = ItemFrameCompass.sprite;
        if (model == null || model.isCustomRenderer() || !isInsideSprite(model, sprite)) {
            uploadToAtlas(sprite);
            return;
        }

        ensureStrip(sprite);

        float uMin = sprite.getUMin();
        float vMin = sprite.getVMin();
        float uSize = sprite.getUMax() - uMin;
        float vSize = sprite.getVMax() - vMin;
        int frames = uploadedFrames;

        GlStateManager.bindTexture(textureId);
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.scalef(1.0F / uSize, 1.0F / (vSize * frames), 1.0F);
        GlStateManager.translatef(-uMin, -vMin + frame * vSize, 0.0F);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        bound = true;
    }

    /^* Called by the item renderer right after the quads of the model were drawn. ^/
    public static void endModel() {
        if (!bound) {
            return;
        }
        bound = false;

        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        Minecraft.getInstance().getTextureManager().bind(TextureAtlas.BLOCKS_LOCATION);
    }

    /^* Called when the item frame is done, in case the model was never drawn. ^/
    public static void reset() {
        pending = false;
        if (bound) {
            endModel();
        }
    }

    /^*
     * What the vanilla tick would have done: upload the frame into the atlas if it is not there already. Vanilla
     * uploads before the item renderer switches the atlas to nearest filtering for the item; here the upload comes
     * after that switch and would turn mipmaps back on, so the item filter is applied again.
     ^/
    private static void uploadToAtlas(CompassSprite sprite) {
        TextureAtlasSpriteAccessor accessor = (TextureAtlasSpriteAccessor) sprite;
        if (frame != accessor.celeritas$getFrameIndex()) {
            accessor.celeritas$setFrameIndex(frame);
            TextureUtil.upload(sprite.getFrame(frame), sprite.getWidth(), sprite.getHeight(), sprite.getX(), sprite.getY(), false, false);
            if (Minecraft.getInstance().getTextureManager().get(TextureAtlas.BLOCKS_LOCATION) instanceof AbstractTexture atlas) {
                atlas.setFilter(false, false);
            }
        }
    }

    private static void ensureStrip(TextureAtlasSprite sprite) {
        int frames = sprite.getFrameCount();
        int width = sprite.getWidth();
        int height = sprite.getHeight();
        int[] frameData = sprite.getFrame(0)[0];
        if (textureId != -1 && frameData == uploadedFrameData && frames == uploadedFrames
                && width == uploadedWidth && height == uploadedHeight) {
            return;
        }

        if (textureId == -1) {
            textureId = TextureUtil.genTextures();
        }
        TextureUtil.prepareImage(textureId, 0, width, height * frames);
        for (int i = 0; i < frames; i++) {
            TextureUtil.upload(new int[][] { sprite.getFrame(i)[0] }, width, height, 0, i * height, false, false);
        }

        uploadedFrameData = frameData;
        uploadedFrames = frames;
        uploadedWidth = width;
        uploadedHeight = height;
    }

    private static boolean isInsideSprite(BakedModel model, TextureAtlasSprite sprite) {
        if (model == checkedModel) {
            return checkedModelInsideSprite;
        }

        boolean inside = quadsInsideSprite(model.getQuads(), sprite);
        for (Direction direction : Direction.values()) {
            inside &= quadsInsideSprite(model.getQuads(direction), sprite);
        }

        checkedModel = model;
        checkedModelInsideSprite = inside;
        return inside;
    }

    private static boolean quadsInsideSprite(List<BakedQuad> quads, TextureAtlasSprite sprite) {
        float uMin = sprite.getUMin() - UV_EPSILON;
        float uMax = sprite.getUMax() + UV_EPSILON;
        float vMin = sprite.getVMin() - UV_EPSILON;
        float vMax = sprite.getVMax() + UV_EPSILON;

        for (BakedQuad quad : quads) {
            int[] data = quad.getVertices();
            // the item vertex format: position (3), colour (1), texture (2), normal (1) per vertex
            int stride = data.length / 4;
            if (stride < 6) {
                return false;
            }
            for (int vertex = 0; vertex < 4; vertex++) {
                float u = Float.intBitsToFloat(data[vertex * stride + 4]);
                float v = Float.intBitsToFloat(data[vertex * stride + 5]);
                if (u < uMin || u > uMax || v < vMin || v > vMax) {
                    return false;
                }
            }
        }
        return true;
    }
}

*///?}
