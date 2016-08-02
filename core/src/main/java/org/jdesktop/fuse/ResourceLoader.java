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

import java.io.File;
import java.io.InputStream;
import java.net.URL;

/**
 * <p>An interface to encapsulate the loading of resource properties from
 * within various formats (such as XML, properties, JDBC, etc...)  The
 * default used by ResourceInjector will always be properties.</p>
 * 
 *  <p>The load methods are to load the resource file into memory.  In
 *  the case of a JDBCLoader these method calls would be ignored.  The
 *  close() is there to close any resources (such as a DB connection or result set)
 *  that the loader may have opened.  close() will be called by the
 *  developer who instantiated the loader, not by ResourceInjector.</p>
 * 
 * @see org.jdesktop.fuse.ResourceInjector#setLoader(ResourceLoader)
 * @see org.jdesktop.fuse.ResourceInjector#getLoader()
 * @since 0.2
 * @author Daniel Spiewak
 */
public interface ResourceLoader extends Iterable<String> {

    /**
     * Loads the resources from the specified URL.
     * 
     * @param url	A URL pointing to the resource properties file.
     */
	public abstract void load(URL... url);
    
    /**
     * Loads the resources from the specified InputStream.
     * 
     * @param is	An InputStream containing the resource properties.
     */
	public abstract void load(InputStream... is);
    
    /**
     * Loads the resources from the specified path (as
     * resolved by {@link Class#getResource(String) }
     * 
     * @param path	A path pointing to the resource properties file.
     */
	public abstract void load(String... path);
    
    /**
     * Loads the resources from the specified path using
     * the specified instance of Class to resolve the path.
     * 
     * @param resolver	The Class instance used to resolve the path.
     * @param path	A path pointing to the resource properties file.
     * @since 0.3
     */
	public abstract void load(Class<?> resolver, String... path);
    
    /**
     * Loads the resources from the specified File instance.
     * 
     * @param file	A File pointing to the resource properties file.
     */
	public abstract void load(File... file);
	
	public abstract void load(ResourceLoader... loader);
	
	/**
	 * Used to dispose or close any resources created by the
	 * ResourceLoader requiring such special treatment (such
	 * as a database connection or a result set).  This method
	 * is <i>not</i> called by ResourceInjector.  It must be
	 * invoked by the developer who created the loader instance.
	 */
	public abstract void close();

    /**
     * Clears the properties specific to this instance of
     * ResourceInjector (common properties are left
     * alone).
     * 
     * @see #get(String)
     */
	public abstract void clear();

	/**
	 * Retrieves the value associated with the specified key.  This value
	 * could have been parsed from any data source (depending on the
	 * ResourceLoader implementor).  The key will always be of the form
	 * <code>parta.partb.key</code> or something of the like.  Global
	 * resources will be indicated by a prefix of <code>*.</code> as in
	 * the case: <code>*.key</code>  The precise syntax implementation
	 * of global resources is dependant on the ResourceLoader implementation.
	 * 
	 * @see org.jdesktop.fuse.ResourceInjector#inject(Object...)
	 * @see org.jdesktop.fuse.ResourceInjector#inject(boolean, Object...)
	 * @param key	The key of the resource to return.
	 * @return The value of the resource indicated by the specified key.
	 */
	public abstract String get(String key);
}
