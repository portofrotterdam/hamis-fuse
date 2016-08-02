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

/**
 * A hive injection provider abstracts the Hive class away from the details of the
 * injection process.  This allows modules to supply custom injection algorithms
 * for different APIs completely transparently.  Custom providers are primarily
 * used for auto-injection binding.  All implementors of this class must be thread-safe.
 * 
 * @see Hive
 * @see AutoInjection
 * @since 0.3
 * @author Daniel Spiewak
 */
public abstract class HiveInjectionProvider<T> {
    
    /**
     * Creates a new instance of HiveInjectionProvider.  This class
     * has no internal state (and so is thread-safe) thus this constructor
     * does nothing but change the permissions of the default.
     */
    protected HiveInjectionProvider() {
    }
    
    /**
     * This method injects the specified instance using the specified definition key
     * using the ResourceInjector associated with the specified key and optionally
     * recursing into the instance superclasses.  This method is required for auto-injection
     * binding but should not be used for any other purpose (which is why the method
     * it delegates to isn't exposed in ResourceInjector).
     * 
     * @param resourceInjector  The ResourceInjector instance key to use in the injection.
     * @param populateHierarchy Boolean flag indicating whether or not to recurse up the class hierarchy.
     * @param definition    The definition key to use for injection.
     * @param instance  The instance to inject.
     */
    protected final void inject(Object resourceInjector, boolean populateHierarchy, 
                                String definition, T instance) {
        ResourceInjector.get(resourceInjector).inject(definition, populateHierarchy, instance);
    }
    
    /**
     * This method is called by Hive to inject every component.
     * 
     * @param key   The ResourceInjector instance key to use in the injection.
     * @param populateHierarchy Boolean flag indicating whether or not to recurse up the class hierarchy.
     * @param instance  The instance to inject.
     */
    public abstract void inject(Object key, boolean populateHierarchy, T instance);
}
