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

package org.jdesktop.fuse;

import java.lang.reflect.Field;

final class TypeLoaderProvider {
    private TypeLoaderProvider() {
    }

    static TypeLoader<?> get(String name, Field field, InjectedResource annotation, Definition definition) {
        Class<?> type = field.getType();
        if (definition != null && !definition.loader(name).equals(TypeLoader.class)) {
            return getTypeLoaderFromDefinition(field, type, definition);
        } else if (annotation == null || annotation.loader().equals(TypeLoader.class)) {
            return TypeLoaderFactory.getLoaderForType(type);
        } else {
            return getTypeLoaderFromAnnotation(annotation, type);
        }
    }
    
    private static TypeLoader<?> getTypeLoaderFromAnnotation(InjectedResource annotation, Class<?> type) {
        TypeLoader<?> typeloader;
        try {
            //noinspection unchecked
            typeloader = (TypeLoader<?>) annotation.loader().newInstance();
            if (!typeloader.supportsType(type)) {
                throw new TypeLoadingException("Specified loader " +
                    annotation.loader() + " does not support the type " + type);
            }
        } catch (InstantiationException e) {
            throw new TypeLoadingException("Specified loader " +
                annotation.loader() + " cannot be instantiated.", e);
        } catch (IllegalAccessException e) {
            throw new TypeLoadingException("Specified loader " +
                annotation.loader() + " cannot be accessed.", e);
        }
        return typeloader;
    }

    private static TypeLoader<?> getTypeLoaderFromDefinition(Field field, Class<?> type, Definition definition) {
        TypeLoader<?> typeloader;
        try {
            //noinspection unchecked
            typeloader = (TypeLoader<?>) definition.loader(field.getName()).newInstance();
            if (!typeloader.supportsType(type)) {
                throw new TypeLoadingException("Specified loader " +
                    definition.loader(field.getName()) + " does not support the type " + type);
            }
        } catch (InstantiationException e) {
            throw new TypeLoadingException("Specified loader " +
                definition.loader(field.getName()) + " cannot be instantiated.", e);
        } catch (IllegalAccessException e) {
            throw new TypeLoadingException("Specified loader " +
                definition.loader(field.getName()) + " cannot be accessed.", e);
        }
        return typeloader;
    }

}
