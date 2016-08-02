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

package org.jdesktop.fuse.core;

import org.jdesktop.fuse.TypeLoader;
import org.jdesktop.fuse.TypeLoaderFactory;

import java.util.Map;
import java.util.UUID;

/**
 * @author Daniel Spiewak
 */
class UUIDTypeLoader extends TypeLoader<UUID> {
	
	UUIDTypeLoader() {
		super(UUID.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public UUID loadType(String name, String value, Class<?> resolver, Map<String, Object> properties) {
		String[] tokens = value.split(",");
		
		for (int i = 0; i < tokens.length; i++) tokens[i] = tokens[i].trim();
		
		TypeLoader<Long> tl = (TypeLoader<Long>) TypeLoaderFactory.getLoaderForType(long.class);
		
		return new UUID(tl.loadTypeWithCaching(name, tokens[0], resolver, properties), 
				tl.loadTypeWithCaching(name, tokens[1], resolver, properties));
	}

}
