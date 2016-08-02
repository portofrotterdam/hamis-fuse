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

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>This class is the superclass of all resource type loaders.  Instances of
 * subclasses of this class are used by ResourceInjector to convert the values
 * from the resource properties into Java objects which can then be injected
 * into instance fields.  The generic parameter <code>T</code> defines what
 * instance type (i.e. Double, Object, Color, etc...) will be returned by the
 * <code>loadType(String, String, Class&lt;?&gt;, Map&lt;String, Object&gt;)</code>
 * method.  Any custom TypeLoader must extend this class <i>and</i> be 
 * registered with TypeLoaderFactory.</p>
 * 
 * <p>Any class extending this one must implement a constructor Class&lt;?&gt;
 * values to the <code>super()</code> constructor.  The arguments passed
 * to the <code>super()</code> constructor should be a list of the Class&lt;?&gt;
 * representing all the types supported by this TypeLoader.  These values will
 * be used by TypeLoader to determine if this TypeLoader is the appropriate
 * one to load a specific value.</p>
 * 
 * @see #TypeLoader(Class...)
 * @see #loadType(String, String, Class, Map)
 * @see org.jdesktop.fuse.TypeLoaderFactory
 * @see org.jdesktop.fuse.ResourceInjector
 * @see Class
 * @since 0.1
 * @author Romain Guy
 * @author Daniel Spiewak
 */
public abstract class TypeLoader<T> {
    private final Map<String, WeakReference<T>> cache = new HashMap<String, WeakReference<T>>();
    
    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();
    
    private final Class<?>[] types;

    /**
     * Every subclass must implement a constructor which calls
     * this one passing a list of supported <code>Class&lt;?gt;</code>
     * types.
     * 
     * @param types	A varargs Classlt;?&gt;[] containing a list of all types
     * 	supported by this TypeLoader.
     */
    protected TypeLoader(Class<? extends T>... types) {
        this.types = types;
    }
    
    /**
     * This method is called by ResourceInjector to parse the resource
     * property value into a usable Java object.  The return result of this method is then
     * injected into the appropriate instance field in the injected class.  If
     * an error is encountered in parsing the value, a TypeLoaderException
     * should be thrown.
     * 
     * @param name	A String representing the key of the resource property.
     * @param value	A String containing the value of the resource property.
     * @param resolver	The Class&lt;?&gt; of the instance being injected.
     * @param properties	A Map&lt;String, Object&gt; containing the properties relevant to
     * 	this TypeLoader.  These include both ResourceInjector common and instance properties.
     * @return A generic T containing the result of parsing the value parameter into an 
     * 	instance of T. 
     * @see org.jdesktop.fuse.ResourceInjector
     * @see org.jdesktop.fuse.TypeLoadingException
     */
    public abstract T loadType(String name, String value, Class<?> resolver, Map<String, Object> properties);
    
    /**
     * TODO write javadoc
     */
    public T loadTypeWithCaching(String name, String value, 
                         Class<?> resolver, Map<String, Object> properties) {
        T back = null;
        
        value = value.trim();
        
        cacheLock.readLock().lock();
        if (cache.containsKey(value)) {
            back = cache.get(value).get();
        }
        cacheLock.readLock().unlock();
        
        if (back == null) {
            back = loadType(name, value, resolver, properties);
            
            cacheLock.writeLock().lock();
            cache.remove(value);
            cache.put(value, new WeakReference<T>(back));
            cacheLock.writeLock().unlock();
        }
        
        return back;
    }
    
    /**
     * Called by ResourceInjector to notify the TypeLoader of sub-properties.  This
     * method was not made abstract to allow for signature compatibility with 0.1
     * and to allow TypeLoader(s) to optionally implement the sub-properties
     * feature.  This method will only be called if there are sub-properties to
     * report.  As this method is only intended for configuration of an existing instance,
     * nothing is returned.  Instead, the instance to mutate is passed into the method.
     * 
     * @param type	The instance returned from <code>loadType</code> to reconfigure with the sub-properties.
     * @param values	The sub-properties organized in the form {"non-fully-qualified key" => "value"}.
     * @param resolver	The Class&lt;?&gt; of the instance being injected.
     * @param properties	A Map&lt;String, Object&gt; containing the properties relevant to
     * 	this TypeLoader.  These include both ResourceInjector common and instance properties.
     * @since 0.2
     */
    public void configureType(T type, Map<String, String> values, Class<?> resolver, Map<String, Object> properties) { }
    
    /**
     * <p>Retrieves the <i>keys</i> that are hierarchically below the parent
     * key.  For instance:</p>
     * 
     * <pre>Component.value=parent value
     *Component.value.child=child value 1
     *Component.value.width=123, 987</pre>
     * 
     * <p>In this case, the parent key would be
     * <code>Component.value</code> and the child keys should be
     * returned as <code>child</code> and
     * <code>width</code></p>
     * 
     * @param type  The value of the resource being injected.
     * @return An array of all unqualified child keys supported by this TypeLoader.
     * @since 0.3
     */
    public String[] getChildKeys(T type) {
        return new String[0];
    }
    
    boolean supportsType(Class<?> type) {
        for (Class<?> supportedType: types) {
            if (isOfType(supportedType, type)) {
                return true;
            }
        }
        
        return false;
    }
    
    private static boolean isOfType(Class<?> type,
                                    Class<?> inter) {
        return type.equals(inter);
//        if (type.equals(inter)) {
//            return true;
//        }
//        
//        try {
//            type.asSubclass(inter);
//        } catch (ClassCastException e) {
//            return false;
//        }
//
//        return true;
    }
}
