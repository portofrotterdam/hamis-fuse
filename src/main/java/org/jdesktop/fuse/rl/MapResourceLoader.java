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

package org.jdesktop.fuse.rl;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.jdesktop.fuse.ResourceLoader;

/**
 * <p>A resource loader which peers directly to a <code>Map&lt;String, String&gt;</code>
 * instance specified by the developer.  Since a Map instance is created by the developer 
 * and passed into the constructor, none of the <code>load</code> methods are 
 * implemented.  Nor is the <code>close</code> method for that matter.  Note: this class
 * does not synchronize the Map instance.  If multiple threads are to be accessing the specified
 * Map instance, the developer must synchronize the calls by using <code>Collections.synchronizedMap</code></p>
 *
 * @since 0.2
 * @author Daniel Spiewak
 */
public final class MapResourceLoader implements ResourceLoader {
	private final Map<String, String> map;
	
	/**
	 * Creates a new instance of MapResourceLoader using the specified Map
	 * instance to peer the resources.
	 * 
	 * @param map	The Map&lt;String, String&gt; instance to use as a peered backend for the resource values.
	 */
	public MapResourceLoader(Map<String, String> map) {
		this.map = map;
	}

	public void load(URL... url) {}

	public void load(InputStream... is) {}

	public void load(String... path) {}

	public void load(Class<?> resolver, String... path) {}

	public void load(File... file) {}

    public void load(ResourceLoader... loaders) {
        for (ResourceLoader loader : loaders) {
            for (String key : loader) {
                map.put(key, loader.get(key));
            }
        }
    }

    public void close() {}

	public void clear() {
		map.clear();
	}

	public String get(String key) {
		return map.get(key);
	}

	/**
	 * Retrieves the Map instance currently in use as the peered data backend.
	 *
	 * @return The Map instance currently in use.
	 */
	public Map<String, String> getMap() {
		return map;
	}

    public Iterator<String> iterator() {
        return map.keySet().iterator();
    }
}
