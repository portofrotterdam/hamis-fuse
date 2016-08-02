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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>This is used to mark instance fields which will have their values assigned
 * to injected resources.  This annotation <i>must</i> be utilized otherwise
 * ResourceInjector will be unable to determine into which fields to inject 
 * resources.  The properties defined in the annotation declaration are used to
 * locate the resource property to inject.  If no properties are specified, then
 * the resource is loaded from <code>ClassName.fieldName.</code></p>
 * 
 * @see org.jdesktop.fuse.ResourceInjector
 * @since 0.1
 * @author Romain Guy
 * @author Daniel Spiewak
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectedResource {
	
    /**
     * A fully qualified resource key which will override <code>name</code>
     * if used.  This property can be used to define a specific key to use
     * to load the resource.
     */
    String key() default "";
    
    /**
     * The right-hand portion of the property key to load the resource from.
     * If this property is specified, the resource will be loaded from
     * <code>ClassName.specifiedNameValue.</code>
     */
    String name() default "";
    
    /**
     * Used to specify the key for a definition provider instance which
     * will be used by ResourceInjector to load the metadata for the
     * tagged instance.  This attribute relates more to the fields within
     * the tagged field and less to the tagged field itself.
     * 
     * @since 0.2
     */
    String definition() default "";
    
    /**
     * This can be used to specify a custom TypeLoader to use to load
     * the resource from the properties file.  The specified type loader
     * class must be a subclass of <code>TypeLoader</code> and must
     * contain a zero argument constructor to allow ResourceInjector to
     * reflectively instantiate it.
     * 
     * @see TypeLoader
     */
    @SuppressWarnings("unchecked")
    Class<? extends TypeLoader> loader() default TypeLoader.class;
}
