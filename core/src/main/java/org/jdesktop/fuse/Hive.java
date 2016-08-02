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

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.WeakHashMap;

/**
 * <p>This class is the superclass of all classes implementing hive support.  In and
 * of itself, this class just acts as a simple injection manager, grouping instances for
 * bulk injection, and an event dispatcher, firing events for various injeciton state
 * changes.</p>
 * 
 * <p>Any subclasses must set the generic type parameter in the extension
 * declaration.  This generic type parameter defines what type(s) are allowed in the
 * <code>inject</code> methods.  Subclasses should also avail themselves of the
 * events fired by this class.  These events are ResourceLoadEvent and
 * ResourceInjectionEvent.  The load event is fired when one of the <code>load</code>
 * methods is called.  The injection evnet is fired when one of the <code>inject</code>
 * methods is called.  Both the events themselves and the corresponding listeners
 * are defined as static inner classes and inner interfaces of this class.</p>
 * 
 * <p>This class can also be used without subclassing.  Such usage would be 
 * appropriate for a situation where listening for injection and loading events is
 * desirable or where it may be necessary to group instances together for automatic
 * injection when the resources are reloaded.  However, it is more common to just 
 * use the subclasses of Hive.</p>
 * 
 * <p>Hive uses WeakReference(s) in its cache of injected instances to ensure that
 * no lock is perceived by the GC.  This ensures less memory usage and better
 * performance in some circumstances.  Hive is thread-safe.</p>
 * 
 * @see ResourceLoadEvent
 * @see ResourceInjectionEvent
 * @see ResourceLoadListener
 * @see ResourceInjectionListener
 * @see WeakReference
 * @since 0.2
 * @author Daniel Spiewak
 * @author Romain Guy
 */
public class Hive<T> {
	private Map<Object, Set<WeakReference<T>>> objects;
	
	private List<ResourceLoadListener> loadListeners;
	private List<ResourceInjectionListener> injectionListeners;
	
	private volatile Stack<WeakReference<T>> markedRefs;
    
    private final Object key;
    
    private final HiveInjectionProvider<T> standardProvider;
    private final HiveInjectionProvider<T> bindProvider;
    
    private final Map<String, String> bindings;
    private final Set<WeakReference<T>> boundInstances;
	
	private final Object LOCK = new Object();

	/**
	 * Creates a new instance of Hive and initializes the injected
	 * instances cache.  This constructor uses the default instance
     * of ResourceInjector and is equivalent to calling <code>new Hive(null)</code>
	 */
	public Hive() {
		this(null);
	}

    /**
     * Creates a new instance of Hive and initializes the injected
     * instances cache.  Uses the specified key instance of
     * ResourceInjector. 
     * 
     * @param key   The key of the ResourceInjector instance to use.
     * @since 0.3
     */
    public Hive(Object key) {
        this.key = key;
        
        objects = Collections.synchronizedMap(new WeakHashMap<Object, Set<WeakReference<T>>>());
        bindings = Collections.synchronizedMap(new HashMap<String, String>());
        boundInstances = Collections.synchronizedSet(new HashSet<WeakReference<T>>());
        
        standardProvider = new HiveStandardInjectionProvider<T>();
        bindProvider = new HiveBindInjectionProvider<T>(bindings);
        
        loadListeners = new ArrayList<ResourceLoadListener>();
        injectionListeners = new ArrayList<ResourceInjectionListener>();
        markedRefs = new Stack<WeakReference<T>>();
    }

    /**
     * Loads the resources from the specified URL.  Uses the current
     * ResourceInjector instance.  Fires a ResourceLoadEvent and 
     * re-injects every instance in the cache.
     * 
     * @param properties	A URL pointing to the resource properties file.
     */
	public void load(URL properties) {
        ResourceInjector.get(key).load(properties);
        
        fireResourceLoadEvent(ResourceInjector.get(key).getLoader());
        
        performInjection();
	}

    /**
     * Loads the resources from the specified URL.  Uses the specified
     * ResourceInjector instance.  Fires a ResourceLoadEvent and 
     * re-injects every instance in the cache.
     * 
     * @param key	The key to use in obtaining the ResourceInjector instance.
     * @param properties	A URL pointing to the resource properties file.
     * @deprecated Use {@link #load(URL)}
     */
    @Deprecated
	public void load(String key, URL properties) {
        load(properties);
    }

    /**
     * Loads the resources from the specified InputStream.  Uses the current
     * ResourceInjector instance.  Fires a ResourceLoadEvent and 
     * re-injects every instance in the cache.
     * 
     * @param properties	An InputStream containing the resource properties.
     */
	public void load(InputStream properties) {
        ResourceInjector.get(key).load(properties);
        
        fireResourceLoadEvent(ResourceInjector.get(key).getLoader());
        
        performInjection();
	}

    /**
     * Loads the resources from the specified InputStream.  Uses the specified
     * ResourceInjector instance.  Fires a ResourceLoadEvent and 
     * re-injects every instance in the cache.
     * 
     * @param key	The key to use in obtaining the ResourceInjector instance.
     * @param properties	An InputStream containing the resource properties.
     * @deprecated Use {@link #load(InputStream)}
     */
    @Deprecated
	public void load(String key, InputStream properties) {
        load(properties);
    }

    /**
     * Loads the resources from the specified path (as
     * resolved by {@link Class#getResource(String) }  Uses the current
     * ResourceInjector instance.  Fires a ResourceLoadEvent and 
     * re-injects every instance in the cache.
     * 
     * @param properties	A path pointing to the resource properties file.
     */
	public void load(String properties) {
        ResourceInjector.get(key).load(properties);
        
        fireResourceLoadEvent(ResourceInjector.get(key).getLoader());
        
        performInjection();
	}

    /**
     * Loads the resources from the specified path (as
     * resolved by {@link Class#getResource(String) }  Uses the specified
     * ResourceInjector instance.  Fires a ResourceLoadEvent and 
     * re-injects every instance in the cache.
     * 
     * @param key	The key to use in obtaining the ResourceInjector instance.
     * @param properties	A path pointing to the resource properties file.
     * @deprecated Use {@link #load(String)}
     */
    @Deprecated
	public void load(String key, String properties) {
        load(properties);
    }

    /**
     * Loads the resources from the specified path using
     * the specified instance of Class to resolve the path.  Uses the current
     * ResourceInjector instance.  Fires a ResourceLoadEvent and 
     * re-injects every instance in the cache.
     * 
     * @param klass	The Class instance used to resolve the path.
     * @param properties	A path pointing to the resource properties file.
     */
	public void load(Class<?> klass, String properties) {
        ResourceInjector.get(key).load(klass, properties);
        
        fireResourceLoadEvent(ResourceInjector.get(key).getLoader());
        
        performInjection();
	}

    /**
     * Loads the resources from the specified path using
     * the specified instance of Class to resolve the path.  Uses the specified
     * ResourceInjector instance.  Fires a ResourceLoadEvent and 
     * re-injects every instance in the cache.
     * 
     * @param key	The key to use in obtaining the ResourceInjector instance.
     * @param klass	The Class instance used to resolve the path.
     * @param properties	A path pointing to the resource properties file.
     * @deprecated Use {@link #load(Class, String)}
     */
    @Deprecated
	public void load(String key, Class<?> klass, String properties) {
        load(klass, properties);
    }

    /**
     * Loads the resources from the specified File instance.  Uses the current
     * ResourceInjector instance.  Fires a ResourceLoadEvent and 
     * re-injects every instance in the cache.
     * 
     * @param properties	A File pointing to the resource properties file.
     */
	public void load(File properties) {
        ResourceInjector.get(key).load(properties);
        
        fireResourceLoadEvent(ResourceInjector.get(key).getLoader());
        
        performInjection();
	}

    /**
     * Loads the resources from the specified File instance.  Uses the specified
     * ResourceInjector instance.  Fires a ResourceLoadEvent and 
     * re-injects every instance in the cache.
     * 
     * @param key	The key to use in obtaining the ResourceInjector instance.
     * @param properties	A File pointing to the resource properties file.
     * @deprecated Use {@link #load(File)}
     */
    @Deprecated
	public void load(String key, File properties) {
        load(properties);
    }

	/**
	 * Injects the specified instance(s) using the default ResourceInjector
	 * instance without populating the superclass hierarchy of the instances.  
	 * Fires a ResourceInjectionEvent.  This is the same as
	 * calling <code>inject(false, true, object)</code> for one instance
	 * and looping the same call for multiple.
	 * 
	 * @param objects	A varargs array of T representing the instance(s) to inject.
	 */
	public void inject(T... objects) {
		for (T object : objects) {
			inject(false, true, object);
		}
	}

	/**
	 * Injects the specified instance(s) using the default ResourceInjector
	 * instance optionally populating the superclass hierarchy of all instances.  
	 * Fires a ResourceInjectionEvent.  This is the same as
	 * calling <code>inject(populateHierarchy, true, object)</code> for one instance
	 * and looping the same call for multiple.
	 * 
	 * @param populateHierarchy	A boolean flag indicating whether or not to inject the superclass hierarchy.
	 * @param objects	A varargs array of T representing the instance(s) to inject.
	 */
	public void inject(boolean populateHierarchy, T... objects) {
		for (T object : objects) {
			inject(populateHierarchy, true, object);
		}
	}

	/**
	 * Injects the specified instance(s) using the specified ResourceInjector
	 * instance without populating the superclass hierarchy of the instances.  
	 * Fires a ResourceInjectionEvent.  This is the same as
	 * calling <code>inject(key, false, true, object)</code> for one instance
	 * and looping the same call for multiple.
	 * 
	 * @param key	A String value representing the key of the ResourceInjector instance to use.
	 * @param objects	A varargs array of T representing the instance(s) to inject.
     * @deprecated Use {@link #inject(Object...)}
	 */
    @Deprecated
	public void inject(String key, T... objects) {
		for (T object : objects) {
			inject(false, true, object);
		}
	}

	/**
	 * Injects the specified instance(s) using the specified ResourceInjector
	 * instance optionally populating the superclass hierarchy of all instances.  
	 * Fires a ResourceInjectionEvent.  This is the same as
	 * calling <code>inject(key, populateHierarchy, true, object)</code> for one instance
	 * and looping the same call for multiple.
	 * 
	 * @param key	A String value representing the key of the ResourceInjector instance to use.
	 * @param populateHierarchy	A boolean flag indicating whether or not to inject the superclass hierarchy.
	 * @param components	A varargs array of T representing the instance(s) to inject.
     * @deprecated Use {@link #inject(boolean, Object...)}
	 */
    @Deprecated
	public void inject(String key, boolean populateHierarchy, T... components) {
		for (T component : components) {
			inject(populateHierarchy, true, component);
		}
	}

	/**
	 * Injects the specified instance(s) using the specified ResourceInjector
	 * instance optionally populating the superclass hierarchy of all instances.
	 * Optionally fires a ResourceInjectionEvent.
	 * 
	 * @param key	A String value representing the key of the ResourceInjector instance to use.
	 * @param populateHierarchy	A boolean flag indicating whether or not to inject the superclass hierarchy.
	 * @param fireEvent	A boolean flag indicating whether or not to fire a ResourceInjectionEvent.
	 * @param instances	The instances of T on which to perform the resource injection.
     * @deprecated Use {@link #inject(boolean, boolean, Object...)}
	 */
    @Deprecated
	public void inject(String key, boolean populateHierarchy, boolean fireEvent, T... instances) {
        inject(populateHierarchy, fireEvent, instances);
	}
    
    /**
     * Injects the specified instance(s) using the current ResourceInjector
     * instance optionally populating the superclass hierarchy of all instances.
     * Optionally fires a ResourceInjectionEvent.  Once injected, all instance
     * will be fired along with any events (such as injection, resource loading, etc).
     * 
     * @since 0.3
     * @param populateHierarchy A boolean flag indicating whether or not to inject the superclass hierarchy.
     * @param fireEvent A boolean flag indicating whether or not to fire a ResourceInjectionEvent.
     * @param instances The instances of T on which to perform the resource injection.
     */
    public void inject(boolean populateHierarchy, boolean fireEvent, T... instances) {
        Set<WeakReference<T>> refs = objects.get(key);

        if (refs == null) {
            refs = new HashSet<WeakReference<T>>();
            objects.put(key, refs);
        }

        for (T object : instances) {
            injectInstance(key, populateHierarchy, object);
            refs.add(new WeakReference<T>(object));
            
            if (fireEvent) {
                fireResourceInjectionEvent(object);
            }
        }
    }
    
	/**
	 * Adds the specified ResourceLoadListener to the listeners List which
	 * will be notified when a ResourceLoadEvent is fired.
	 * 
	 * @param listener	The ResourceLoadListener to add.
	 */
	public void addResourceLoadListener(ResourceLoadListener listener) {
		loadListeners.add(listener);
	}
	
	/**
	 * Removes the specified ResourceLoadListener from the listeners List which
	 * will be notified when a ResourceLoadEvent is fired.
	 * 
	 * @param listener	The ResourceLoadListener to remove.
	 */
	public void removeResourceLoadListener(ResourceLoadListener listener) {
		loadListeners.remove(listener);
	}
	
	/**
	 * Adds the specified ResourceInjectionListener to the listeners List which
	 * will be notified when a ResourceInjectionEvent is fired.
	 * 
	 * @param listener	The ResourceInjectionListener to add.
	 */
	public void addResourceInjectionListener(ResourceInjectionListener listener) {
		injectionListeners.add(listener);
	}
	
	/**
	 * Removes the specified ResourceInjectionListener from the listeners List which
	 * will be notified when a ResourceInjectionEvent is fired.
	 * 
	 * @param listener	The ResourceInjectionListener to remove.
	 */
	public void removeResourceInjectionListener(ResourceInjectionListener listener) {
		injectionListeners.remove(listener);
	}

    /**
     * Accessor for the ResourceInjector key.
     * 
     * @since 0.3
     * @return The ResourceInjector key currently in use by all injection methods.
     */
	public final Object getKey() {
        return key;
    }
    
    /**
     * Accessor for the internal bindings cache.
     * 
     * @since 0.3
     * @return The internal bindings cache.
     */
    public final Map<String, String> getBindings() {
        return bindings;
    }

    /**
     * Any instances being auto-injected using instance binding (see documentation
     * on auto-injection) must be registered with this method.  This is done automatically
     * in auto-injection.  This adds the specified instance to the internal cache of
     * bound fields.
     * 
     * @since 0.3
     * @param instance  The component instance to add to the internal bindings cache.
     */
    public final void addBoundInstance(T instance) {
        boundInstances.add(new WeakReference<T>(instance));
    }

    /**
	 * Returns an Iterable instance suitable for use in the enhanced for loop.  The
	 * Iterable contains a number of WeakReference(s) encapsulating the instances
	 * of T in the cache.
	 * 
	 * @return An Iterable of WeakReference(s) encapsulating all instances of T in the cache.
	 */
	protected Iterable<WeakReference<T>> getIterable() {
		if (!objects.containsKey(key)) return null;
		
		return objects.get(key);
	}
    
    /**
     * Used by Hive to get the HiveInjectionProvider instance used for non-binding
     * injection.
     * 
     * @since 0.3
     * @return The HiveInjectionProvider to use in non-binding injection.
     */
    protected HiveInjectionProvider<T> getStandardInjectionProvider() {
        return standardProvider;
    }
    
    /**
     * Used by Hive to get the HiveInjectionProvider instance used for binding
     * injection.
     * 
     * @since 0.3
     * @return The HiveInjectionProvider to use in binding injection.
     */
    protected HiveInjectionProvider<T> getBindInjectionProvider() {
        return bindProvider;
    }
    
    void fireResourceLoadEvent(ResourceLoader loader) {
    	ResourceLoadEvent e = new ResourceLoadEvent(loader, key);
    	
    	for (ResourceLoadListener l : loadListeners) {
    		l.resourceLoaded(e);
    	}
    }

    void fireResourceInjectionEvent(Object... objects) {
    	ResourceInjectionEvent e = new ResourceInjectionEvent(objects);
    	
    	for (ResourceInjectionListener l : injectionListeners) {
    		l.resourceInjected(e);
    	}
    }

    private void injectInstance(Object key, boolean populateHierarchy, T instance) {
        if (isContainedIn(boundInstances, instance)) {
            getBindInjectionProvider().inject(key, true, instance);
        } else {
            getStandardInjectionProvider().inject(key, populateHierarchy, instance);
        }
    }
	
	@SuppressWarnings("unchecked")
	private void performInjection() {
        if (objects.size() > 0) {
        	List<T> list = new ArrayList<T>();
        	Object[] array = objects.get(key).toArray();
        	
        	for (Object ref : array) {
        		T obj = ((WeakReference<T>) ref).get();
        		
        		if (obj == null) {
        			markForRemoval((WeakReference<T>) ref);
        			continue;
        		}
        		
                injectInstance(key, false, obj);
        		
        		list.add(obj);
        	}
        	
        	removeMarkedReferences();
        	
        	fireResourceInjectionEvent(list.toArray());
        }
	}
	
	private void markForRemoval(WeakReference<T> ref) {
		synchronized (LOCK) {
			if (markedRefs.contains(ref)) {
				return;
			}
			
			markedRefs.push(ref);
		}
	}
	
	private void removeMarkedReferences() {
		synchronized (LOCK) {
			for (WeakReference<T> ref : markedRefs) {
				markedRefs.remove(ref);
				
				objects.get(key).remove(ref);
                boundInstances.remove(ref);
			}
		}
	}
    
    // TODO find a more efficient algorithm
    private static <I> boolean isContainedIn(Set<WeakReference<I>> set, I item) {
        for (WeakReference<I> value : set) {
            I ref = value.get();
            
            if (ref != null && ref.equals(item)) {
                return true;
            }
        }
        
        return false;
    }
	
	/**
	 * Represents an event which is fired when the <code>load</code>
	 * method is called.
	 * 
	 * @since 0.2
	 * @author Daniel Spiewak
	 */
	public static class ResourceLoadEvent {
		
		/**
		 * The ResourceLoader instance used to load the resources in the
		 * invokation from which this event was fired.
		 */
		private ResourceLoader loader;
		
		/**
		 * The key of the ResourceInjector instance used in the resource
		 * loading.  May be <code>null</code>
		 */
		private Object key;
		
		private ResourceLoadEvent(ResourceLoader loader, Object key) {
			this.loader = loader;
			this.key = key;
		}
		
		/**
		 * This is the same as calling <code>e.loader</code> assuming
		 * that <code>e</code> is an instance of ResourceLoadEvent.
		 * 
		 * @return The value of {@link #loader}
		 */
		public ResourceLoader getLoader() {
			return loader;
		}
		
		/**
		 * This is the same as calling <code>e.key</code> assuming
		 * that <code>e</code> is an instance of ResourceLoadEvent.
		 * 
		 * @return The value of {@link #key}
		 */
		public Object getKey() {
			return key;
		}
	}
	
	/**
	 * Represents an event which is fired when the <code>inject</code>
	 * method is called.
	 * 
	 * @since 0.2
	 * @author Daniel Spiewak
	 */
	public static class ResourceInjectionEvent {
		
		/**
		 * An array of instances which were injected.
		 */
		private Object[] objects;
		
		private ResourceInjectionEvent(Object... obj) {
			objects = obj;
		}
		
		/**
		 * This is the same as calling <code>e.objects</code> assuming
		 * that <code>e</code> is an instance of ResourceInjectionEvent.
		 * 
		 * @return The value of {@link #objects}
		 */
		public Object[] getObjects() {
			return objects;
		}
	}
	
	/**
	 * Any classes implementing this interface will be able to act
	 * as listeners for a ResourceLoadEvent as fired by the Hive
	 * class.
	 * 
	 * @since 0.2
	 * @author Daniel Spiewak
	 */
	public interface ResourceLoadListener {
		
		/**
		 * Called when <code>Hive#load</code> is invoked.
		 */
		public void resourceLoaded(ResourceLoadEvent e);
	}
	
	/**
	 * Any classes implementing this interface will be able to act
	 * as listeners for a ResourceInjectionEvent as fired by the Hive
	 * class.
	 * 
	 * @since 0.2
	 * @author Daniel Spiewak
	 */
	public interface ResourceInjectionListener {
		
		/**
		 * Called when <code>Hive#inject</code> is invoked.
		 */
		public void resourceInjected(ResourceInjectionEvent e);
	}
}
