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

package org.jdesktop.fuse.definitions;

import org.jdesktop.fuse.Definition;
import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.TypeLoader;

/**
 * A definition format implementation which allows injection definitions
 * to be defined as actual classes.  Each field name in the definition class
 * will be injected in the instance in question by ResourceInjector.  Any
 * metadata (such as <code>name</code>, <code>key</code>, or
 * <code>loader</code> attributes) are defined as attribute tags <i>in
 * the definition class.</i>  A good example of a class definition is
 * available in the /demo/definitions directory.  As per convention, the
 * constructor of this class is declared private and a static factory method
 * is provided.
 * 
 * @since 0.2
 * @author Daniel Spiewak
 */
public final class ClassDefinition implements Definition {
	private final Class<?> clazz;
	
	private ClassDefinition(Class<?> clazz) {
		this.clazz = clazz;
	}

	public boolean isInjectedField(String field) {
		try {
			return clazz.getDeclaredField(field) != null;
		} catch (SecurityException e) {
		} catch (NoSuchFieldException e) {
		}
		
		return false;
	}

	public String name(String field) {
		try {
			InjectedResource annotation = clazz.getDeclaredField(field).getAnnotation(InjectedResource.class);
			
			if (annotation == null) return "";
			
			return annotation.name();
		} catch (SecurityException e) {
		} catch (NoSuchFieldException e) {
		}
		
		return "";
	}

	public String key(String field) {
		try {
			InjectedResource annotation = clazz.getDeclaredField(field).getAnnotation(InjectedResource.class);
			
			if (annotation == null) return "";
			
			return annotation.key();
		} catch (SecurityException e) {
		} catch (NoSuchFieldException e) {
		}
		
		return "";
	}

	@SuppressWarnings("unchecked")
    public Class<? extends TypeLoader> loader(String field) {
		try {
			InjectedResource annotation = clazz.getDeclaredField(field).getAnnotation(InjectedResource.class);
			
			if (annotation == null) return TypeLoader.class;
			
			return annotation.loader();
		} catch (SecurityException e) {
		} catch (NoSuchFieldException e) {
		}
		
		return TypeLoader.class;
	}

    /**
     * Returns a new instance of ClassDefinition which will load
     * the definition from the specified Class instance.
     */
	public static ClassDefinition load(Class<?> clazz) {
		return new ClassDefinition(clazz);
	}
}
