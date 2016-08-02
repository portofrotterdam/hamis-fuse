/**
 * Copyright (c) 2006, Sun Microsystems, Inc
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following 
 *     disclaimer in the documentation and/or other materials provided 
 *     with the distribution.
 *   * Neither the name of the Fuse project nor the names of its
 *     contributors may be used to endorse or promote products derived 
 *     from this software without specific prior written permission.
 * 
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
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.jdesktop.fuse.TypeLoader;
import org.jdesktop.fuse.TypeLoadingException;

class FontTypeLoader extends TypeLoader<Font> {
    private static Map<String, Font> fonts = new HashMap<String, Font>();
    
    FontTypeLoader() {
        super(Font.class);
    }
    
    @Override
    public Font loadType(String name, String value, Class<?> resolver, Map<String, Object> properties) {
        String[] parts = value.split("-");
        if (parts.length < 3) {
            throw new TypeLoadingException("Theme resource " + name +
                                             " is not a valid font. Must be " +
                                             "defined as follows: " +
                                             "Font face-STYLE-size " +
                                             "(eg Arial-PLAIN-12).");
        }
        
        if (parts[0].endsWith(".ttf")) {
            Font font = fonts.get(parts[0]);
            
            if (font == null) {
                InputStream fontStream = resolver.getResourceAsStream(parts[0]);
                if (fontStream == null) {
                    throw new TypeLoadingException("Unable to load font from " +
                                                     name);
                }
                
                try {
                    font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                    fonts.put(parts[0], font);
                } catch (FontFormatException e) {
                    throw new TypeLoadingException("Unable to load font from " +
                                                     name, e);
                } catch (IOException e) {
                    throw new TypeLoadingException("Unable to load font from " +
                                                     name, e);
                } finally {
                    try {
                        fontStream.close();
                    } catch (IOException e) {
                        //noinspection ThrowFromFinallyBlock
                        throw new TypeLoadingException("Unable to load font from " +
                                                         name, e);
                    }
                }
            }
            
            int style = Font.PLAIN;
            if (parts[1].equals("BOLD")) {
                style = Font.BOLD;
            } else if (parts[1].equals("ITALIC")) {
                style = Font.ITALIC;
            } else if (parts[1].equals("BOLDITALIC")) {
                style = Font.BOLD | Font.ITALIC;
            }
            
            try {
                return font.deriveFont(style, Float.parseFloat(parts[2]));
            } catch (TypeLoadingException e) {
                throw new TypeLoadingException("Theme resource " + name +
                                                 " has invalid point size.", e);
            }
        } else {        
            return Font.decode(value);
        }
    }
}