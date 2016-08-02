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
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jdesktop.fuse.ResourceLoader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A resource loader implementation which loads the resource values from
 * an XML file using the SAX parser obtained using JAXP.  The XML format
 * itself is well defined in the Fuse web documentation.
 * 
 * @since 0.2
 * @author Daniel Spiewak
 */
public final class XMLResourceLoader extends DefaultHandler implements ResourceLoader {
	private SAXParser parser;
	private Map<String, String> properties;
    
    private Stack<String> currentKey;
	
    /**
     * Initializes the in-memory resource cache and creates the SAX
     * parser using JAXP.
     */
	public XMLResourceLoader() {
		properties = new HashMap<String, String>();
		
		try {
			parser = SAXParserFactory.newInstance().newSAXParser();
		} catch (ParserConfigurationException e) {
			properties = null;
		} catch (SAXException e) {
			properties = null;
		}
	}

	public void load(URL... urls) {
        currentKey = new Stack<String>();
        
        for (URL url : urls) {
    		try {
    			parser.parse(url.openStream(), this);
    		} catch (SAXException e) {
    			properties = null;
    		} catch (IOException e) {
    			properties = null;
    		}
        }
	}

	public void load(InputStream... streams) {
        currentKey = new Stack<String>();
        
        for (InputStream is : streams) {
    		try {
    			parser.parse(is, this);
    		} catch (SAXException e) {
    			properties = null;
    		} catch (IOException e) {
    			properties = null;
    		}
        }
	}

	public void load(String... paths) {
		load(getClass(), paths);
	}

	public void load(Class<?> resolver, String... paths) {
        currentKey = new Stack<String>();
        
        for (String path : paths) {
    		try {
    			parser.parse(resolver.getResourceAsStream(path), this);
    		} catch (SAXException e) {
    			properties = null;
    		} catch (IOException e) {
    			properties = null;
    		}
        }
	}

    public void load(ResourceLoader... loaders) {
        for (ResourceLoader loader : loaders) {
            for (String key : loader) {
                properties.put(key, loader.get(key));
            }
        }
    }

    public void load(File... files) {
        currentKey = new Stack<String>();
        
        for (File file : files) {
    		try {
    			parser.parse(file, this);
    		} catch (SAXException e) {
    			properties = null;
    		} catch (IOException e) {
    			properties = null;
    		}
        }
	}

	public void close() {}

	public void clear() {
		properties.clear();
	}

	public String get(String key) {
		return properties.get(key);
	}

    public Iterator<String> iterator() {
        return properties.keySet().iterator();
    }

    @Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		startElement(qName, attributes);
	}
	
	/**
	 * Compatibility with SAX 1.0
	 */
	public void startElement(String name, Attributes attributes) throws SAXException {
        if (name.equals("resources")) {
            return;
        }
        
        if (name.equals("object")) {
            if (currentKey.size() == 0) {
                currentKey.push(attributes.getValue("class"));
            } else {
                String cur = currentKey.pop();
                
                currentKey.push(cur + "$" + attributes.getValue("class"));
            }
        } else if (name.equals("resource")) {
            StringBuilder key = new StringBuilder();
            
            for (String curKey : currentKey) {
                key.append(curKey);
                key.append('.');
            }
            key.append(attributes.getValue("key"));
            
            properties.put(key.toString(), attributes.getValue("value"));
            currentKey.push(attributes.getValue("key"));
        } else if (name.equals("fuse:global")) {
            if (currentKey.size() != 0) {
                return;
            }
            
            currentKey.push("*");
        }
	}

    @Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		endElement(qName);
	}
	
	/**
	 * Compatibility with SAX 1.0
	 */
	public void endElement(String name) throws SAXException {
        if (name.equals("resources")) {
            return;
        } else if (name.equals("object") || name.equals("resource")) {
            currentKey.pop();
        } else if (name.equals("fuse:global")) {
            if (currentKey.peek().equals("*")) {
                currentKey.pop();
            }
        }
	}
}
