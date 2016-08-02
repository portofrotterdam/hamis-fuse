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

/**
 * <p>This is the superinterface of any definition provider classes.  This
 * interface is used primarily as a contract assurance interface and not
 * in any fancy polymorphic magic.  Unless one is defining a new definition
 * <i>provider</i> (not the definition itself), there should never be a need 
 * to implement this interface.</p>
 * 
 * <p>It is expected that all implementations of this interface will also provide
 * a static <code>load</code> method overloaded for the various instances
 * which may or may not be necessary to create the definition.  This is not
 * required (obviously) but it is conventional with the definitions provided
 * by Fuse.</p>
 * 
 * @see org.jdesktop.fuse.ResourceInjector#addDefinition(String, Definition)
 * @since 0.2
 * @author Daniel Spiewak
 */
public interface Definition {
	
	/**
	 * This method is called to determine if the field name in question
	 * is a field which should be injected.  If this method returns true,
	 * the result is roughly analogous to tagging a field with the 
	 * <code>@InjectedResource</code> annotation.
	 * 
	 * @param field	The name of the declared field to mark for injection.
	 * @return <code>true</code> if the field should be injected,
	 * 	<code>false</code> if otherwise.
	 */
	public boolean isInjectedField(String field);
	
	/**
	 * This method returns the value of the (optional) name
	 * attribute of the field in question.  This is roughly analogous
	 * to the <code>@InjectedResource(name="...")</code>
	 * attribute.
	 * 
	 * @param field	The name of the declared field for which to return the name attribute.
	 * @return The value of the name attribute for the specified field.
	 */
	public String name(String field);
	
	/**
	 * This method returns the value of the (optional) key
	 * attribute of the field in question.  This is roughly analogous
	 * to the <code>@InjectedResource(key="...")</code>
	 * attribute.
	 * 
	 * @param field	The name of the declared field for which to return the key attribute.
	 * @return The value of the key attribute for the specified field.
	 */
	public String key(String field);
	
	/**
	 * This method returns the value of the (optional) loader
	 * attribute of the field in question.  This is roughly analogous
	 * to the <code>@InjectedResource(loader="...")</code>
	 * attribute.
	 * 
	 * @param field	The name of the declared field for which to return the loader attribute.
	 * @return The value of the loader attribute for the specified field.
	 */
	@SuppressWarnings("unchecked")
    public Class<? extends TypeLoader> loader(String field);
}
