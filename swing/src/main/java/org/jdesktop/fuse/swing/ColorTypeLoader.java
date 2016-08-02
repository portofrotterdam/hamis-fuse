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

import java.awt.Color;
import java.util.Map;

import org.jdesktop.fuse.TypeLoader;
import org.jdesktop.fuse.TypeLoadingException;

class ColorTypeLoader extends TypeLoader<Color> {
    ColorTypeLoader() {
        super(Color.class);
    }
    
    @Override
    public Color loadType(String name, String value, Class<?> resolver, Map<String, Object> properties) {
        try {
            return decode(value);
        } catch (TypeLoadingException e) {
            throw new TypeLoadingException("Theme resource " + name +
                                             " is not a valid color.", e);
        }
    }
    
    static Color decode(String value) {
        Color color = null;
        
        if (value.startsWith("#")) {
            switch (value.length()) {
                // RGB/hex color
                case 7:
                    color = Color.decode(value);
                    break;
                // ARGB/hex color
                case 9:
                    int alpha = Integer.decode(value.substring(0, 3));
                    int rgb = Integer.decode("#" + value.substring(3));
                    color = new Color(alpha << 24 | rgb, true);
                    break;
                default:
                    throw new TypeLoadingException("Color " + value +
                                                     " is not a valid color" +
                                                     " (must be \"#RRGGBB\"," +
                                                     " \"#AARRGGBB\"," +
                                                     " \"R, G, B\" or" +
                                                     " \"R, G, B, A\".");
            }
        } else {
            String[] parts = value.split(",");
            if (parts.length < 3 || parts.length > 4) {
                throw new TypeLoadingException("Color " + value +
                                                 " is not a valid color" +
                                                 " (must be \"#RRGGBB\"," +
                                                 " \"#AARRGGBB\"," +
                                                 " \"R, G, B\" or" +
                                                 " \"R, G, B, A\".");
            }
            
            try {
                // with alpha component
                if (parts.length == 4) {
                    int r = Integer.parseInt(parts[0].trim());
                    int g = Integer.parseInt(parts[1].trim());
                    int b = Integer.parseInt(parts[2].trim());
                    int a = Integer.parseInt(parts[3].trim());
                    
                    color = new Color(r, g, b, a);
                } else {
                    int r = Integer.parseInt(parts[0].trim());
                    int g = Integer.parseInt(parts[1].trim());
                    int b = Integer.parseInt(parts[2].trim());
                    
                    color = new Color(r, g, b);
                }
            } catch (NumberFormatException e) {
                throw new TypeLoadingException("Color " + value +
                                                 " is not a valid color" +
                                                 " (must be \"#RRGGBB\"," +
                                                 " \"#AARRGGBB\"," +
                                                 " \"R, G, B\" or" +
                                                 " \"R, G, B, A\".");
            }
        }
        
        return color;
    }
}