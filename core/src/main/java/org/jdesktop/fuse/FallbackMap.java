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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A simple map which creates a HashMap as a peer.  If the peer
 * Map doesn't contain a requested value, the fallback Map
 * is queried.
 * 
 * @author Daniel Spiewak
 */
class FallbackMap<K, V> implements Map<K, V> {
	private final Map<K, V> fallback;
	private final Map<K, V> peer;
	
	FallbackMap(Map<K, V> fallback) {
		this.fallback = fallback;
		
		peer = new HashMap<K, V>();
	}
	
	public int size() {
		return peer.size() + fallback.size();
	}

	public boolean isEmpty() {
		return !(peer.isEmpty() && fallback.isEmpty());
	}

	public boolean containsKey(Object key) {
		return peer.containsKey(key) || fallback.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return peer.containsValue(value) || fallback.containsValue(value);
	}

	public V get(Object key) {
		if (peer.containsKey(key)) {
            return peer.get(key);
        }
		
		return fallback.get(key);
	}

	public V put(K key, V value) {
		return peer.put(key, value);
	}

	public V remove(Object key) {
		return null;
	}

	public void putAll(Map<? extends K, ? extends V> t) {
		peer.putAll(t);
	}

	public void clear() {
		peer.clear();
	}

	public Set<K> keySet() {
		Set<K> keySet = new HashSet<K>();
		
		keySet.addAll(peer.keySet());
		keySet.addAll(fallback.keySet());
		
		return keySet;
	}

	public Collection<V> values() {
		Collection<V> values = new ArrayList<V>();
		
		values.addAll(peer.values());
		values.addAll(fallback.values());
		
		return values;
	}

	public Set<Entry<K, V>> entrySet() {
		Set<Entry<K, V>> entrySet = new HashSet<Entry<K, V>>();
		
		entrySet.addAll(peer.entrySet());
		entrySet.addAll(fallback.entrySet());
		
		return entrySet;
	}
}