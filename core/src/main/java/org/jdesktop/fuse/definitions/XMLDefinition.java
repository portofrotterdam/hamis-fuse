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

import static java.util.Collections.synchronizedList;
import static java.util.Collections.synchronizedMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jdesktop.fuse.Definition;
import org.jdesktop.fuse.DefinitionLoadingException;
import org.jdesktop.fuse.TypeLoader;
import org.jdesktop.fuse.TypeLoadingException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A definition format implementation which allows injection definitions to be loaded from
 * an XML file.  The XML format is documented in the web documentation
 * and in examples in the /demo directory.
 * 
 * @since 0.2
 * @author Daniel Spiewak
 */
public final class XMLDefinition extends DefaultHandler implements Definition {
	
	/*
	 * Example (for dev documentation purposes): 

<?xml version="1.0" encoding="UTF-8"?>

<definition>
    <field name="name">
		<name>nme</name>
		<key>Strange.nme</key>
		<loader>org.jdesktop.fuse.TypeLoader</loader>
	</field>
</definition>

	 */
	
	private static enum ParameterType {
		NAME("name"),
		KEY("key"),
		LOADER("loader");
		
		private final String key;
		
		ParameterType(String key) {
			this.key = key;
		}
		
		public String getKey() {
			return key;
		}
	}
	
	private List<String> injectedFields = synchronizedList(new LinkedList<String>());
	private Map<String, Map<String, String>> parameters = synchronizedMap(
			new HashMap<String, Map<String, String>>());
	
	private ParameterType currentParameter = null;
	
	private XMLDefinition() {}

	public boolean isInjectedField(String field) {
		return parameters.containsKey(field);
	}

	public String name(String field) {
		if (!parameters.containsKey(field)) {
			return null;
		}
		
		if (!parameters.get(field).containsKey("name")) {
			return "";
		}
		
		return parameters.get(field).get("name");
	}

	public String key(String field) {
		if (!parameters.containsKey(field)) {
			return null;
		}
		
		if (!parameters.get(field).containsKey("key")) {
			return "";
		}
		
		return parameters.get(field).get("key");
	}

	@SuppressWarnings("unchecked")
	public Class<? extends TypeLoader> loader(String field) {
		if (!parameters.containsKey(field)) {
            return TypeLoader.class;
		}
		
		if (!parameters.get(field).containsKey("loader")) {
			return TypeLoader.class;
		}
		
		Class<? extends TypeLoader> back = null;
		
		try {
			back = (Class<? extends TypeLoader>) Class.forName(parameters.get(field).get("loader"));
		} catch (ClassNotFoundException e) {
			throw new TypeLoadingException(e.getMessage(), e);
		}
		
		return back;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		startElement(qName, attributes);
	}
	
	public void startElement(String name, Attributes attributes) {
		if (name.equals("field")) {
			injectedFields.add(attributes.getValue("name"));
			parameters.put(attributes.getValue("name"), synchronizedMap(new HashMap<String, String>()));
		} else if (name.equals("name")) {
			if (currentParameter == null) {
				currentParameter = ParameterType.NAME;
			}
		} else if (name.equals("key")) {
			if (currentParameter == null) {
				currentParameter = ParameterType.KEY;
			}
		} else if (name.equals("loader")) {
			if (currentParameter == null) {
				currentParameter = ParameterType.LOADER;
			}
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (currentParameter == null) return;
		
		String value = new String(ch, start, length);
		
		parameters.get(injectedFields.get(injectedFields.size() - 1)).put(
				currentParameter.getKey(), value);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		endElement(qName);
	}
	
	public void endElement(String name) {
		if (name.equals("name") || name.equals("key") || name.equals("loader")) {
			currentParameter = null;
		}
	}
	
    /**
     * Loads the XML definition from the specified InputStream and
     * returns an instance of XMLDefinition which corresponds.
     * 
     * @param is    The stream to load the XML from.
     * @return A valid instance of XMLDefinition containing the parsed XML data.
     */
	public static XMLDefinition load(InputStream is) {
		XMLDefinition back = new XMLDefinition();
		
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(is, back);
		} catch (ParserConfigurationException e) {
			throw new DefinitionLoadingException(e.getMessage(), e);
		} catch (SAXException e) {
			throw new DefinitionLoadingException(e.getMessage(), e);
		} catch (IOException e) {
			throw new DefinitionLoadingException(e.getMessage(), e);
		}
		
		return back;
	}

    /**
     * Loads the XML definition from the specified File and
     * returns an instance of XMLDefinition which corresponds.  This
     * is the same as calling <code>XMLDefinition.load(new FileInputStream(file))</code>
     * 
     * @param file  The File instance to load the XML from.
     * @return A valid instance of XMLDefinition containing the parsed XML data.
     */
	public static XMLDefinition load(File file) {
		try {
			return load(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new DefinitionLoadingException(e.getMessage(), e);
		}
	}
	
    /**
     * Loads the XML definition from the specified resource on the
     * classpath and returns an instance of XMLDefinition which
     * corresponds.  This is the same as calling
     * <code>XMLDefinition.load(getClass().getResourceAsStream(resource))</code>
     * 
     * @param resource  The path of the XML definition resource on the classpath as
     *      resolved by the current classloader.
     * @return A valid instance of XMLDefinition containing the parsed XML data.
     */
	public static XMLDefinition load(String resource) {
		return load(XMLDefinition.class.getResourceAsStream(resource));
	}
	
    /**
     * Loads the XML definition from the specified resource on the
     * classpath using the specified Class instance to resolve the
     * resource and returns an instance of XMLDefinition which
     * corresponds.  This is the same as calling
     * <code>XMLDefinition.load(resolver.getResourceAsStream(resource))</code>
     * @param resolver  The Class instance from to use to resolve the specified resource path.
     * @param resource  The path of the XML definition resource on the classpath as
     *      resolved by the specified Class classloader.
     * 
     * @return A valid instance of XMLDefinition containing the parsed XML data.
     */
	public static XMLDefinition load(Class<?> resolver, String resource) {
		return load(resolver.getResourceAsStream(resource));
	}
}
