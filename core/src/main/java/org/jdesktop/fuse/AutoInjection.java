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
import java.util.Collections;
import java.util.List;

/**
 * <p>This class is used to enable auto-injection for any supported container.  Out
 * of the box, Fuse supports only Swing <code>JContainer</code> and SWT
 * <code>Composite</code> as containers.  However, the API is extensible and
 * allows the addition of third party auto-injection providers at runtime.</p>
 * 
 * <p>This class is used in the following manner:</p>
 * 
 * <pre>...
 * JPanel container = new JPanel();
 * AutoInjection.enable(container);
 * ...</pre>
 * 
 * <p>By contract, when <code>AutoInjection.enable</code> is invoked, the
 * container will be registered with the provider.  The provider must then handle
 * the listening for the addition of child components and by contract must invoke
 * <code>Hive#inject</code> for each component <i>when it is added.</i>  Any
 * providers which violate this contract in any way will explain in its documentation.
 * If no suitable provider can be located, an exception will be thrown by 
 * <code>enable</code>.</p>
 * 
 * <p>Auto-injection is well integrated with hives.  In fact, all the injection is done
 * using an instance of <code>Hive</code>.  By default, an instance of Hive will
 * be created by the auto-injection provider and returned from the <code>enable</code>
 * method.  Optionally, an instance of Hive can be passed to the overloaded
 * <code>enable</code> which is to be used by the provider to handle the
 * injection.  Either Hive instance can be treated like any other Hive with the
 * appropriate events and re-injection handling.</p>
 * 
 * @see Hive
 * @see org.jdesktop.fuse.AutoInjectionProvider
 * @since 0.3
 * @author Daniel Spiewak
 */
public final class AutoInjection {
    private final static List<AutoInjectionProvider<?>> providers =
        Collections.synchronizedList(new ArrayList<AutoInjectionProvider<?>>());
    
    private AutoInjection() {}
    
    /**
     * Adds an AutoInjectionProvider to the internal registry.  Any providers to be
     * used by the <code>enable</code> method must be first registered with
     * this method.
     * 
     * @see #enable(Object)
     * @see #enable(Hive, Object)
     * @param provider the injection provider
     */
    public static <T> void addAutoInjectionProvider(AutoInjectionProvider<T> provider) {
        providers.add(provider);
    }
    
    /**
     * Enables auto-injection for the specified container.  This is identical to calling
     * <code>AutoInjection.enable(null, component)</code>
     * 
     * @return The Hive instance used by the auto-injection provider.
     * @param container The container for which to enable auto-injection.
     * @see #enable(Hive, Object)
     */
    public static <T, S extends T> Hive<T> enable(S container) {
        return enable((Hive<T>) null, container);
    }
    
    /**
     * <p>Enables auto-injection for the specified container by calling the <code>enable</code>
     * method in the appropriate AutoInjectionProvider previously registered using
     * the <code>addAutoInjectionProvider</code> method.  If no provider is found,
     * an <code>AutoInjectionException</code> will be thrown.</p>
     * 
     * <p>Auto-injection uses hives to handle the injection of the specific child components
     * when they are added to the parent container.  An instance of Hive can optionally be
     * specified for use by the auto-injection provider.  Otherwise, the auto-injection provider
     * will create a new instance of the appropriate hive and return that instance.  This hive
     * instance can be used just like any other hive.  If you reload the resources managed by
     * the hive instance, all the components under the hive will be re-injected automatically 
     * (including those injected using auto-injection).</p>
     * 
     * @return The Hive instance used by the auto-injection provider.
     * @param hive  The Hive instance used by the auto-injection provider.
     * @param container The container for which to enable auto-injection.
     */
    @SuppressWarnings("unchecked")
    public static <T, S extends T> Hive<T> enable(Hive<T> hive, S container) {
        AutoInjectionProvider[] providersArray =
            providers.toArray(new AutoInjectionProvider[0]);
        
        for (AutoInjectionProvider<?> provider : providersArray) {
            if (provider.supportsType(container.getClass())) {
                // thanks to the test supportsType() we know the provider can be cast to Provider<T>
                if (hive == null) {
                    hive = ((AutoInjectionProvider<T>) provider).enable(hive, container);
                } else {
                    ((AutoInjectionProvider<T>) provider).enable(hive, container);
                }
                
                break;      // we only want to have one autoinjector enabled for a specific component
            }
        }
        
        if (hive == null) {
            throw new AutoInjectionException("No compatible auto-injection provider can be found for " + container);
        }
        
        return hive;
    }
    
    /**
     * <p>Binds the specified component identifier to a specific definition.  This
     * definition will be used by the auto-injection providers to inject any component
     * matching the identifier when necessary.  The identifier is provider-specific.  For
     * instance, /Swing uses <code>Component#getName()</code> as one of the
     * possible identifier values.  The default (if the provider doesn't explictly define
     * an identifier) is <code>Object#toString()</code></p>
     * 
     * @param name  The identifier of the component to inject using the specified definition key.
     * @param definition    The definition identifier (as registered with ResourceInjector) to use to
     *      inject the component matching the specified identifier.
     */
    public static void bind(String name, String definition) {
        for (AutoInjectionProvider<?> provider : providers) {
            provider.bind(name, definition);
        }
    }
}
