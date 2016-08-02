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
import java.awt.GradientPaint;
import java.util.Map;

import org.jdesktop.fuse.TypeLoader;
import org.jdesktop.fuse.TypeLoadingException;

class GradientTypeLoader extends TypeLoader<GradientPaint> {
    GradientTypeLoader() {
        super(GradientPaint.class);
    }
    
    @Override
    public GradientPaint loadType(String name, String value, Class<?> resolver, Map<String, Object> properties) {
        String[] parts = value.split("\\s?\\|\\s?");
        if (parts.length < 4) {
            throw new TypeLoadingException("Theme resource " + name +
                                             " is not a valid gradient. Format " +
                                             "must be: x1,y1 | x2,y2 | color1 | color2" +
                                             " where color is a Color.");
        }

        String[] startPoint = parts[0].split(",");
        if (startPoint.length != 2) {
            throw new TypeLoadingException("Start point " + parts[0] +
                                             " is not valid in " + name);
        }
        
        float startX;
        try {
            startX = Float.parseFloat(startPoint[0]);
        } catch (NumberFormatException e) {
            throw new TypeLoadingException("Gradient start point is invalid in " + name + ".", e);
        }
        float startY;
        try {
            startY = Float.parseFloat(startPoint[1]);
        } catch (NumberFormatException e) {
            throw new TypeLoadingException("Gradient start point is invalid in " + name + ".", e);
        }
        
        String[] endPoint = parts[1].split(",");
        if (endPoint.length != 2) {
            throw new TypeLoadingException("End point " + parts[1] +
                                             " is not valid in " + name);
        }
        
        float endX;
        try {
            endX = Float.parseFloat(endPoint[0]);
        } catch (NumberFormatException e) {
            throw new TypeLoadingException("Gradient end point is invalid in " + name + ".", e);
        }
        float endY;
        try {
            endY = Float.parseFloat(endPoint[1]);
        } catch (NumberFormatException e) {
            throw new TypeLoadingException("Gradient end point is invalid in " + name + ".", e);
        }

        Color color1 = null;
        try {
            color1 = ColorTypeLoader.decode(parts[2]);
        } catch (TypeLoadingException e) {
            throw new TypeLoadingException("Gradient start color is invalid in " + name + ".", e);
        }
        
        Color color2 = null;
        try {
            color2 = ColorTypeLoader.decode(parts[3]);
        } catch (TypeLoadingException e) {
            throw new TypeLoadingException("Gradient end color is invalid in " + name + ".", e);
        }
        
        return new GradientPaint(startX, startY, color1, endX, endY, color2);
    }
}