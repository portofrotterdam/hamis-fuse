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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdesktop.fuse.ResourceInjector;
import org.jdesktop.fuse.ResourceLoader;
import org.jdesktop.fuse.TypeLoadingException;

/**
 * <p>A resource loader to load resources from a properties file.  This is the default
 * resource loader used by ResourceInjector.</p> 
 *
 * @see org.jdesktop.fuse.ResourceInjector
 * @since 0.2
 * @author Daniel Spiewak
 */
public final class PropertiesResourceLoader implements ResourceLoader {
	private Resources properties;

    /**
     * Creates a new instance and initializes the properties peer.
     */
	public PropertiesResourceLoader() {
		properties = new Resources();
	}

	public void load(URL... urls) {
		if (urls == null) {
			throw new NullPointerException("Cannot load null urls");
		}
		
        List<InputStream> streams = new ArrayList<InputStream>(urls.length);
        
        for (URL url : urls) {
            if (url == null) {
                throw new TypeLoadingException("Resource was null");
            }
            
            try {
                InputStream stream = url.openStream();
                streams.add(stream);
            } catch (IOException e) {
                throw new TypeLoadingException("Cannot open " + url + ".", e);
            }
        }
        
        loadProperties(streams.toArray(new InputStream[streams.size()]));
        
        for (InputStream is : streams) {
            try {
                is.close();
            } catch (IOException e) {       // if we can't close the stream, just ignore it
            }
        }
	}

	public void load(InputStream... streams) {
		loadProperties(streams);
	}

	public void load(String... paths) {
		load(ResourceInjector.class, paths);
	}

	public void load(Class<?> resolver, String... paths) {
        List<URL> urls = new ArrayList<URL>(paths.length);
        
        for (String path : paths) {
            URL url = resolver.getResource(path);
            if (url == null) {
                throw new TypeLoadingException("Resource \"" + path + "\" was not found");
            }
            
            urls.add(url);
        }
        
		load(urls.toArray(new URL[urls.size()]));
	}

	public void load(File... files) {
        List<URL> urls = new ArrayList<URL>(files.length);
        
        for (File file : files) {
            try {
                urls.add(file.toURI().toURL()); 
            } catch (MalformedURLException e) {
                throw new TypeLoadingException("Cannot open " + files + ".", e);
            }
        }

        load(urls.toArray(new URL[urls.size()]));
	}

    public void load(ResourceLoader... loaders) {
        for (ResourceLoader loader : loaders) {
            for (String key : loader) {
                properties.put(key, loader.get(key));
            }
        }
    }

    public void close() {
    }
	
	public void clear() {
		properties.clear();
	}

	public String get(String key) {
		return properties.get(key);
	}

    public Iterator<String> iterator() {
        return properties.iterator();
    }

    private void loadProperties(InputStream... streams) {
		try {
            for (InputStream is : streams) {
                properties.load(is);
            }
		} catch (IOException e) {
			throw new TypeLoadingException("No properties found.", e);
		}
	}
}
