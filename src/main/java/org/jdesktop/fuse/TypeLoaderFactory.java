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

import java.util.LinkedList;
import java.util.List;

import org.jdesktop.fuse.core.CoreModule;

/**
 * <p>This class is used by ResourceInjector as a backend for all
 * TypeLoader instances.  There are no instance methods in this
 * class, everything is static.  Basically, this class is just a high-level
 * wrapper around a List&lt;TypeLoader&lt;?&gt;&gt;  This class is
 * also used by module control classes to register custom TypeLoader(s).</p>
 * 
 * @see #addTypeLoader(TypeLoader)
 * @see FuseModule
 * @see ResourceInjector
 * @see TypeLoader
 * @since 0.1
 * @author Romain Guy
 */
public final class TypeLoaderFactory {
    private static List<TypeLoader<?>> loaders = new LinkedList<TypeLoader<?>>();

    private TypeLoaderFactory() {
    }

    // core type loaders are assumed
    static {
        ResourceInjector.addModule(new CoreModule());
    }
    
    /**
     * <p>This method is used by module control classes to register
     * custom TypeLoader(s).  For example, the SwingModule class
     * contains an <code>init()</code> method as all FuseModule
     * subclasses do.  This <code>init()</code> method is what creates
     * and registers the custom TypeLoader(s) in the /Swing module
     * with TypeLoaderFactory.  This method is called to handle the
     * registration of the specified TypeLoader.</p>
     * 
     * @param loader	The TypeLoader instance to be registered.
     * @see FuseModule
     * @see TypeLoader
     */
    public static void addTypeLoader(TypeLoader<?> loader) {
        if (loader != null) {
            loaders.add(loader);
        }
    }
    
    /**
     * This method is called by ResourceInjector to find the appropriate
     * TypeLoader for a specific type.  There should rarely be a need
     * for code outside the core Fuse API to call this method.
     * 
     * @param type	The Class&lt;?&gt; representing the type in question.
     * @return The TypeLoader&lt;?&gt; corresponding to the specified type.
     */
    @SuppressWarnings("unchecked")
    public static <T> TypeLoader<T> getLoaderForType(Class<T> type) {
        for (TypeLoader<?> loader: loaders) {
            if (loader.supportsType(type)) {
                return (TypeLoader<T>) loader;
            }
        }
        
        throw new TypeLoadingException("Theme resource type " + type +
                                         " is not supported.");
    }
}