package org.taumc.celeritas.impl.render.text;

//? if >=1.8 {
/*import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.TextRenderUtils;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;

/^*
 * Remembers, per sign, what the sign renderer works out from the text components every frame: the wrapped
 * components, the formatted string of the first line and its width. Vanilla recomputes all of it for every visible
 * sign every frame, which for a line of text means copying styles, walking component trees and measuring the string
 * two times, and was most of the sign cost left after the glyph batching.
 *
 * Entries are keyed on the identity of the text component of the line; the client replaces the component when a sign
 * changes, so a stale entry would need a mod that mutates a component in place. Everything is dropped when the font
 * reloads or switches between the default and the unicode glyphs, which changes the widths the wrapping depends on.
 ^/
public final class SignTextCache {
    private static final int LINES = 4;

    private static int fontGeneration;

    private final Text[] sources = new Text[LINES];
    @SuppressWarnings("unchecked")
    private final List<Text>[] wrapped = new List[LINES];
    private final String[] formatted = new String[LINES];
    private final int[] widths = new int[LINES];
    private int generation = -1;
    private boolean unicode;
    private int next;

    /^* Called when a font reloads its glyph data. ^/
    public static void invalidateFonts() {
        fontGeneration++;
    }

    public List<Text> wrap(Text text, int width, TextRenderer font, boolean stripLeadingSpaces, boolean allowFormatting) {
        if (this.generation != fontGeneration || this.unicode != font.getUnicode()) {
            Arrays.fill(this.sources, null);
            Arrays.fill(this.wrapped, null);
            Arrays.fill(this.formatted, null);
            this.generation = fontGeneration;
            this.unicode = font.getUnicode();
        }

        for (int i = 0; i < LINES; i++) {
            if (this.sources[i] == text && this.wrapped[i] != null) {
                return this.wrapped[i];
            }
        }

        List<Text> list = TextRenderUtils.wrapText(text, width, font, stripLeadingSpaces, allowFormatting);
        String string = list != null && !list.isEmpty() ? list.get(0).getFormattedString() : null;

        int slot = this.next;
        this.next = (slot + 1) % LINES;
        this.sources[slot] = text;
        this.wrapped[slot] = list;
        this.formatted[slot] = string;
        this.widths[slot] = string != null ? font.getWidth(string) : 0;
        return list;
    }

    /^* The formatted string of the first wrapped component, if it is one this cache produced. ^/
    public String formatted(Text first) {
        for (int i = 0; i < LINES; i++) {
            List<Text> list = this.wrapped[i];
            if (list != null && !list.isEmpty() && list.get(0) == first && this.formatted[i] != null) {
                return this.formatted[i];
            }
        }
        return first.getFormattedString();
    }

    /^* The width of a string this cache produced, measured with the font it was produced for. ^/
    public int width(String string, TextRenderer font) {
        for (int i = 0; i < LINES; i++) {
            if (this.formatted[i] == string) {
                return this.widths[i];
            }
        }
        return font.getWidth(string);
    }
}

*///?}
