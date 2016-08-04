/**
 * Copyright (c) 2006, Sun Microsystems, Inc
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * <p>
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * * Neither the name of the Fuse project nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jdesktop.fuse.swing;

import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.jdesktop.fuse.TypeLoader;
import org.jdesktop.fuse.TypeLoadingException;

class FontTypeLoader extends TypeLoader<Font> {

    private static Map<String, Font> fonts = new HashMap<>();

    FontTypeLoader() {
        super(Font.class);
    }

    @Override
    public Font loadType(String name, String value, Class<?> resolver, Map<String, Object> properties) {
        String[] parts = value.split("-");

        if (parts.length < 3) {
            throw new TypeLoadingException(
                    String.format("Theme resource %s is not a valid font. Must be defined as follows: Font face-STYLE-size (eg Arial-PLAIN-12).", name));
        }

        if (parts[0].endsWith(".ttf")) {
            Font font = fonts.get(parts[0]);

            if (font == null) {
                InputStream fontStream = resolver.getResourceAsStream(parts[0]);

                if (fontStream == null) {
                    throw new TypeLoadingException(String.format("Unable to load font from '%s:%s'", name, value));
                }

                try {
                    font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                    fonts.put(parts[0], font);
                } catch (final Exception e) {
                    throw new TypeLoadingException(String.format("Unable to load font from '%s:%s'", name, value), e);
                } finally {
                    try {
                        fontStream.close();
                    } catch (final IOException e) {
                        //noinspection ThrowFromFinallyBlock
                        throw new TypeLoadingException(String.format("Unable to load font '%s:%s'", name, value), e);
                    }
                }
            }

            int style = Font.PLAIN;
            switch (parts[1]) {
                case "BOLD":
                    style = Font.BOLD;
                    break;
                case "ITALIC":
                    style = Font.ITALIC;
                    break;
                case "BOLDITALIC":
                    style = Font.BOLD | Font.ITALIC;
                    break;
            }

            try {
                return font.deriveFont(style, Float.parseFloat(parts[2]));
            } catch (final Exception e) {
                throw new TypeLoadingException(String.format("Theme resource '%s:%s' has invalid point size.", name, value), e);
            }
        } else {
            return Font.decode(value);
        }
    }
}