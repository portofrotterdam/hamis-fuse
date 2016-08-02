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
 * Subclasses of AutoInjectionProvider abstract the enabling of auto-injection
 * away from the details of the API specific implementation.  The algorithms
 * used to determine the appropriate auto-injection provider are very similar
 * to those used in determining the required TypeLoader.
 * 
 * @see AutoInjection
 * @since 0.3
 * @author Daniel Spiewak
 */
public abstract class AutoInjectionProvider<T> {
    private Class<T> type;
    
    /**
     * Creates a new auto-injection provider supporting the specified type
     * and all subclasses.
     * 
     * @param type  The inclusive superclass of all types supported by this provider.
     */
    protected AutoInjectionProvider(Class<T> type) {
        this.type = type;
    }
    
    boolean supportsType(Class<?> supports) {
        if (supports.equals(type)) {
            return true;
        }
        
        try {
            supports.asSubclass(type);
        } catch (ClassCastException e) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Called by <code>AutoInjection</code> to enable auto-injection for the
     * specified container.  The hive parameter is optional and may be null.  This
     * method will only be invoked on types which are of the same type (or
     * subclasses) of the type passed to the constructor.  Thus, the implementation
     * of this method does not need to be highly optimized.  The implementation
     * <i>must</i> use an instance of Hive to handle the injection of the child
     * components and return this instance from this method.
     * 
     * @param hive  Specifies the instance of Hive to use in injecting the components.  May be null.
     * @param container The parent container to listen to for the addition of child components to inject.
     * @return The instance of Hive used to inject child components.  If a Hive instance is passed into
     *      the method, that instance must be returned from the method.
     */
    protected abstract <S extends T> Hive<T> enable(Hive<T> hive, S container);
    
    /**
     * Called by <code>AutoInjection</code> to register a binding of the specified
     * component identifier to the specified definition key.  Components that match
     * the component identifier (matching is provider specific) should be injected using
     * the specified definition key.  This method is called for <i>every</i> binding, 
     * reguardless of whether or not this type supports the component in question.  
     * Thus, this should execute very quickly.
     * 
     * @param name  The component identifier to bind to the definition.
     * @param definition    The definition key to use to inject any component that matches the
     *      specified identifier.
     */
    protected abstract void bind(String name, String definition);
}
