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

import java.awt.RenderingHints;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.jdesktop.fuse.TypeLoader;
import org.jdesktop.fuse.TypeLoadingException;

class RenderingHintsTypeLoader extends TypeLoader<RenderingHints> {
    RenderingHintsTypeLoader() {
        super(RenderingHints.class);
    }
    
    @Override
    public RenderingHints loadType(String name, String value, Class<?> resolver, Map<String, Object> properties) {
        Map<RenderingHints.Key, Object> hints = new HashMap<RenderingHints.Key, Object>();
        
        String[] pairs = value.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length < 2) {
                throw new TypeLoadingException("Theme resource " + name +
                                                 " is not a valid rendering hints collection. " +
                                                 "The pair " + keyValue + " is invalid. Format " +
                                                 "should be key=value");
            }
            
            String hintKey = keyValue[0].trim().replace(' ', '_').toUpperCase();
            String hintValue = keyValue[1].trim().replace(' ', '_').toUpperCase();
            
            if (!hintKey.startsWith("KEY_")) {
                hintKey = "KEY_" + hintKey;
            }
            
            Field keyField = null;
            try {
                keyField = RenderingHints.class.getField(hintKey);
            } catch (SecurityException e) {
            } catch (NoSuchFieldException e) {
                throw new TypeLoadingException("Theme resource " + name +
                                                 " is not a valid rendering hints collection. " +
                                                 "The key " + hintKey + " does not exist.");
            }
            
            Field valueField = null;
            try {
                valueField = RenderingHints.class.getField(hintValue);
            } catch (SecurityException e) {
            } catch (NoSuchFieldException e) {
                try {
                    valueField = RenderingHints.class.getField("VALUE_" + hintValue);
                } catch (SecurityException e1) {
                } catch (NoSuchFieldException e1) {
                    String prefix = hintKey;
                    if (hintKey.startsWith("KEY_")) {
                        prefix = prefix.substring("KEY_".length());
                    }
                    
                    try {
                        valueField = RenderingHints.class.getField("VALUE_" + prefix + "_" + hintValue);
                    } catch (SecurityException e2) {
                    } catch (NoSuchFieldException e2) {
                        throw new TypeLoadingException("Theme resource " + name +
                                                         " is not a valid rendering hints collection. " +
                                                         "The value " + hintValue + " does not exist.");
                    }
                }
            }
            
            try {
                hints.put((RenderingHints.Key) keyField.get(null),
                          valueField.get(null));
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
                throw new TypeLoadingException("Theme resource " + name +
                                                 " is not a valid rendering hints collection. " +
                                                 "The key " + hintKey + " does not exist.");
            }
        }
        
        return new RenderingHints(hints);
    }
}