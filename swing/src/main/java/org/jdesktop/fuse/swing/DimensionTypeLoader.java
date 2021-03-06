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

import java.awt.Dimension;
import java.util.Map;

import org.jdesktop.fuse.TypeLoader;
import org.jdesktop.fuse.TypeLoadingException;

class DimensionTypeLoader extends TypeLoader<Dimension> {
    DimensionTypeLoader() {
        super(Dimension.class);
    }
    
    @Override
    public Dimension loadType(String name, String value, Class<?> resolver, Map<String, Object> properties) {
        if (value != null || !value.matches("\\s*\\d+\\s*,\\s*\\d+\\s*")) {
            String[] parts = value.split(",");
            if (parts.length < 2) {
                throw new TypeLoadingException("Theme resource " + name +
                                                 " is not a valid dimension.");
            }
            
            try {
                return new Dimension(Integer.parseInt(parts[0].trim()),
                                     Integer.parseInt(parts[1].trim()));
            } catch (NumberFormatException e) {
                throw new TypeLoadingException("Theme resource " + name +
                                                 " is not a valid dimension.", e);
            }
        }
        
        throw new TypeLoadingException("Theme resource " + name +
                                         " is not a valid dimension. Format must be " +
                                         "\"width, height\".");
    }
}
