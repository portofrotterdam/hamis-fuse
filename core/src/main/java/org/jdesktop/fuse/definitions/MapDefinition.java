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

import java.util.HashMap;
import java.util.Map;

import org.jdesktop.fuse.Definition;
import org.jdesktop.fuse.TypeLoader;
import org.jdesktop.fuse.TypeLoadingException;

import static java.util.Collections.synchronizedMap;

/**
 * <p>A definition format implementation which loads the definition and field
 * metadata from a specified <code>Map&lt;String, String&gt;</code>
 * instance.  The value String in the (key, value) pair is parsed into an instance
 * of <code>Map&lt;String, String&gt;</code>.  The parsed map format is as 
 * follows:</p>
 * 
 * <table border="1">
 *      <tbody>
 *          <tr>
 *              <td valign="top"><i>unqualified field name</i></td>
 *              <td>
 *                  <table>
 *                      <tbody>
 *                          <tr>
 *                              <td><code>name</code></td>
 *                              
 *                              <td><i>the value of the name metadata attribute</i></td>
 *                          </tr>
 *                          <tr>
 *                              <td><code>key</code></td>
 *                              
 *                              <td><i>the value of the key metadata attribute</i></td>
 *                          </tr>
 *                          <tr>
 *                              <td><code>loader</code></td>
 *                              
 *                              <td><i>the value of the loader metadata attribute</i></td>
 *                          </tr>
 *                      </tbody>
 *                  </table>
 *              </td>
 *          </tr>
 *      </tbody>
 * </table>
 * 
 * <p>The format of the value String in the (key, value) pair of the specified Map
 * is as follows:</p>
 * 
 * <p>&nbsp;&nbsp;&nbsp;&nbsp;<code>name=&lt;&lt;name attribute value&gt;&gt;, 
 * key=&lt;&lt;key attribute value&gt;&gt;, loader=&lt;&lt;loader attribute value&gt;&gt;</code></p>
 * 
 * <p>All metadata attributes are optional.  If none are to be specified, an empty String
 * is expected.</p>
 * 
 * @since 0.2
 * @author Daniel Spiewak
 */
public final class MapDefinition implements Definition {
	private final Map<String, Map<String, String>> parameters;
	
	private MapDefinition(Map<String, String> parameters) {
		this.parameters = synchronizedMap(new HashMap<String, Map<String, String>>());
		
		for (String key : parameters.keySet()) {
			this.parameters.put(key, parseString(parameters.get(key)));
		}
	}
	
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
	
    /**
     * Creates a new instance of MapDefinition using the specified instance
     * of <code>Map&lt;String, String&gt;</code> as a data peer to obtain
     * metadata and parsed fields.
     * 
     * @param parameters    The data peer to use in the definition implementation.
     * @return An instance of MapDefinition corresponding to the Map peer passed into the method.
     */
	public static MapDefinition load(Map<String, String> parameters) {
		return new MapDefinition(parameters);
	}
	
	private Map<String, String> parseString(String string) {
		Map<String, String> back = new HashMap<String, String>();
		
		if (string.trim().length() == 0) {
			return back;
		}
		
		String[] tokens = string.split(",");
		
		if (tokens.length == 0) {
			return back;
		}
		
		for (String token : tokens) {
            token = token.trim();
			if (token.startsWith("name")) {
				token = token.substring(5);
				token = token.substring(1, token.length() - 1);
				
				back.put("name", token);
			} else if (token.startsWith("key")) {
				token = token.substring(4);
				token = token.substring(1, token.length() - 1);
				
				back.put("key", token);
			} else if (token.startsWith("loader")) {
				token = token.substring(7);
				token = token.substring(1, token.length() - 1);
				
				back.put("loader", token);
			}
		}
		
		return back;
	}
}
