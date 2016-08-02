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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jdesktop.fuse.rl.PropertiesResourceLoader;

/**
 * <p>This class is used by the developer to manage the resource injection
 * process.  This class loads the properties file which contains the
 * resources to inject.  It also performs the actual resource injections
 * reflectively and calls the appropriate TypeLoader(s) required to load
 * the resource from the properties file and convert it into the corresponding
 * Java object.  This class is completely thread safe and implements the
 * multi-instance Singleton pattern.</p>
 *
 * <p>To use this class, you must first get an instance using the static
 * <code>get()</code> or <code>get(Object)</code> method.  Fuse
 * by default loads the module for the Java core types (double, String, URL, etc...)
 * but it does not by default load support for Swing or SWT.  If you need
 * injected resources requiring a non-core module, you must add it manually
 * to ResourceInjector using the <code>addModule(String)</code> or
 * the <code>addModule(FuseModule)</code> method.  These methods
 * both add the module to ResourceInjector and enable support for injecting
 * the corresponding types.  You can also add third party and custom modules
 * as long as they are subclasses of <code>FuseModule.</code>  With the
 * required module(s) added to ResourceInjector, you must now set any
 * properties you may need.  For instance, with the exception of two types,
 * SWT resource injection requires the <code>swt.display</code> property
 * to be set containing the instance of <code>org.eclipse.swt.widgets.Display</code>
 * in use by the GUI.  This is accomplished through the <code>ResourceInjector.setCommonProperty(String, Object)</code>
 * and the <code>ResourceInjector#setProperty(String, Object)</code> methods.
 * <code>setCommonProperty(String, Object)</code> sets the property on
 * <i>all</i> instances of ResourceInjector and thus on all TypeLoader
 * instances.  <code>setProperty(String, Object)</code> only sets the property
 * on the instance of ResourceInjector on which it is invoked.  With the properties
 * set, you can then load the resource file using on of the overloaded
 * <code>load()</code> methods.  Once the resource file is loaded, you
 * call the <code>inject(Object)</code> or the <code>inject(Object, boolean)</code>
 * method to actually perform the resource injection.  With the SWT and Swing modules,
 * this call must be performed in the UI thread.  At this point, the resource
 * injection process has completed and if you wish to create a new ResourceInjector
 * instance with the specified key (specified in the call to <code>get(Object)</code>)
 * you may invoke the <code>dispose()</code> method which reclaims the resources and
 * allows for the creation of a new Singleton instance. The default ResourceInjector
 * cannot be disposed, only reset.</p>
 *
 * <p>For more information, see the <a href="https://fuse.dev.java.net/">online documentation.</a></p>
 *
 * @see #get()
 * @see #get(Object)
 * @see #inject(Object...)
 * @see #inject(boolean, Object...)
 * @see FuseModule
 * @see TypeLoader
 * @since 0.1
 * @author Romain Guy
 * @author Daniel Spiewak
 */
public final class ResourceInjector {
    private static final ResourceInjector DEFAULT_INSTANCE = new ResourceInjector(null);
    private static final Map<Object, ResourceInjector> instances = new HashMap<Object, ResourceInjector>();
    private static Map<String, Object> commonProperties  = Collections.synchronizedMap(
        new HashMap<String, Object>());
    private static final Map<String, Definition> definitions = Collections.synchronizedMap(
        new HashMap<String, Definition>());
    
    private static final List<FuseModule> modules = Collections.synchronizedList(
        new LinkedList<FuseModule>());
    private static final ReadWriteLock moduleLocker = new ReentrantReadWriteLock();
    
    private final ReadWriteLock locker = new ReentrantReadWriteLock();
    private final Set<String> cycle = new TreeSet<String>();
    private final Object key;
    
    private boolean useBeanInfo = false;
    
    private ResourceLoader loader = new PropertiesResourceLoader();
    
    private final Map<String, Object> typeProperties;
    
    private ResourceInjector(Object key) {
        this.key = key;

        typeProperties = Collections.synchronizedMap(
            new FallbackMap<String, Object>(commonProperties));
    }
    
    /**
     * Clears the properties specific to this instance of
     * ResourceInjector (common properties are left
     * alone).
     *
     * @see #setProperty(String, Object)
     */
    public void reset() {
        locker.writeLock().lock();
        loader.clear();
        locker.writeLock().unlock();
    }
    
    /**
     * Removes the current instance of ResourceInjector from
     * the Singleton cache and clears the instance properties
     * (common properties are left alone). This method does not
     * affect the default ResourceInjector (key = null.)
     */
    public void dispose() {
        if (key != null) {
            locker.writeLock().lock();
            loader.clear();
            instances.remove(key);
            locker.writeLock().unlock();
        }
    }
    
    /**
     * Loads the resources from the specified URL.
     *
     * @param properties	A URL pointing to the resource properties file.
     */
    public void load(URL... properties) {
        loader.load(properties);
    }
    
    /**
     * Loads the resources from the specified InputStream.
     *
     * @param properties	An InputStream containing the resource properties.
     */
    public void load(InputStream... properties) {
        loader.load(properties);
    }
    
    /**
     * Loads the resources from the specified path (as
     * resolved by {@link Class#getResource(String) }
     *
     * @param properties	A path pointing to the resource properties file.
     */
    public void load(String... properties) {
        loader.load(properties);
    }
    
    /**
     * Loads the resources from the specified path using
     * the specified instance of Class to resolve the path.
     *
     * @param klass	The Class instance used to resolve the path.
     * @param properties	A path pointing to the resource properties file.
     */
    public void load(Class<?> klass, String... properties) {
        loader.load(klass, properties);
    }
    
    /**
     * Loads the resources from the specified File instance.
     *
     * @param properties	A File pointing to the resource properties file.
     */
    public void load(File... properties) {
        loader.load(properties);
    }
    
    public void load(ResourceLoader... loaders) {
        loader.load(loaders);
    }
    
    /**
     * Performs the resource injection operation on the specified
     * instance only populating fields in the instance's Class.  This
     * is the same as calling <code>inject(false, components).</code>
     *
     * @param components	The instance(s) on which to perform resource injection.
     * @see #inject(boolean, Object...)
     * @since 0.2
     */
    public void inject(Object... components) {
        inject(false, components);
    }
    
    public void injectFromSuper(Object component) {
//        inject(false, FuseUtilities.getCallingClass(0), component);
    }

    public void injectFromSuper(boolean populateHierarchy, Object component) {
//        inject(populateHierarchy, FuseUtilities.getCallingClass(0), component);
    }
    
    /**
     * <p>Performs the resource injection operation on the specified
     * instance(s).  If the first method parameter is <code>true</code>
     * the resource injection will also be performed on all
     * superclass fields all the way up to the various stop packages (usually just javax.* and java.*) and
     * subpackages.  If the value is <code>false</code> only the
     * specified instance's fields will be injected.</p>
     *
     * <p>The resource injection is performed by getting the fields
     * in the specified instance and then checking for the <code>InjectedResource</code>
     * annotation marking those fields.  The parameters of the
     * annotation are used to determine which property in the resource
     * file should be injected into the field.  Then, the TypeLoader
     * corresponding to the type of the field.  Note: if the <code>loader</code>
     * parameter is specified in the annotation, it overrides the default
     * loader for that field type.  If no TypeLoader is found for the
     * field type, an exception is thrown.</p>
     *
     * <p>If one of the specified instances contains a field marked with
     * <code>@InjectedResource(definition="...")</code> the instance field is
     * treated as an instance to be injected unto itself.  The specified definition
     * key is used to obtain an instance of Definition, which must be added statically
     * to ResourceInjector prior to injection.  This Definition instance is used to
     * obtain the metadata required for injection.  The Definition instance
     * <i>overrides</i> any <code>@InjectedResource</code> annotations which
     * may be found within the declaring class of the field in question.</p>
     *
     * @param populateHierarchy	A boolean flag specifying if the injection should
     * 	continue up the inheritance hierarchy.
     * @param components	The instances on which to perform resource injection.
     * @see TypeLoader
     * @see org.jdesktop.fuse.TypeLoaderFactory
     * @see Definition
     * @since 0.2
     */
    public void inject(boolean populateHierarchy, Object... components) {
        inject(populateHierarchy, null, components);
    }
    
    void inject(boolean populateHierarchy, Class<?> componentClass, Object... components) {
        List<Exception> exceptions = new LinkedList<Exception>();
        locker.readLock().lock();
        
        for (Object component : components) {
            if (componentClass == null) {
                componentClass = component.getClass();
            } else {
                try {
                    component.getClass().asSubclass(componentClass);
                } catch (ClassCastException e) {
                    componentClass = component.getClass();
                }                
            }

            try {
                FieldIterator iterator = FieldIterator.get(componentClass, populateHierarchy, false);
                AnnotatedFieldSelector selector = AnnotatedFieldSelector.get(iterator);
                BeanInfo beanInfo = BeanInfoProvider.get(componentClass, useBeanInfo);
                
                for (AnnotatedField annotatedField : selector) {
                    try {
                        InjectedResource annotation = annotatedField.getAnnotation();
                        InjectionProvider provider = InjectionProvider.get(annotation);
                        
                        provider.inject(this, component, componentClass,
                                        annotatedField, beanInfo, useBeanInfo, populateHierarchy);
                    } catch (TypeLoadingException e) {
                        exceptions.add(e);
                        //e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        exceptions.add(e);
                        //e.printStackTrace();
                    }
                }
            } catch (IntrospectionException e) {
                exceptions.add(e);
                //e.printStackTrace();
            }
        }
        
        locker.readLock().unlock();
        FuseUtilities.buildAndThrowChainedException(exceptions);
    
    }
    
    /**
     * <p>Performs the resource injection operation on the specified
     * instance using a definition to find injected fields. If the
     * method parameter populateHierarchy is <code>true</code> the
     * resource injection will also be performed on all superclass
     * fields all the way up to the various stop packages (usually
     * just javax.* and java.*) and subpackages.  If the value is
     * <code>false</code> only the specified instance's fields will
     * be injected.</p>
     * 
     * @param populateHierarchy A boolean flag specifying if the injection should
     *                          continue up the inheritance hierarchy.
     * @param instance        The instance(s) on which to perform resource injection.
     *
     * @see #inject(boolean, Object...)
     * @since 0.3
     */
    void inject(String definition, boolean populateHierarchy, Object instance) {
        List<Exception> exceptions = new LinkedList<Exception>();
        locker.readLock().lock();
        
        Class<?> klass = instance.getClass();
        try {
            FieldIterator iterator = FieldIterator.get(klass, populateHierarchy, true);
            BeanInfo beanInfo = BeanInfoProvider.get(klass, useBeanInfo);
            
            for (Field field : iterator) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    injectResource(instance, klass, field, beanInfo, null, definition);
                }
            }
        } catch (TypeLoadingException e) {
            exceptions.add(e);
        } catch (IllegalArgumentException e) {
            exceptions.add(e);
        } catch (IntrospectionException e) {
            exceptions.add(e);
        }
        
        locker.readLock().unlock();
        FuseUtilities.buildAndThrowChainedException(exceptions);
    }

    /**
     * Returns the current ResourceLoader instance which will be used
     * by the injection algorithm to load the resource values.
     *
     * @since 0.2
     * @return The ResourceLoader instance currently in use
     */
    public ResourceLoader getLoader() {
        return loader;
    }
    
    /**
     * Sets the ResourceLoader instance to use to load resource values.  This
     * instance will be used by the injection algorithm and all calls to <code>load</code>
     * peer to this instance.  The default is {@link PropertiesResourceLoader}
     *
     * @since 0.2
     * @param loader	The ResourceLoader instance to use to load resource values.
     */
    public void setLoader(ResourceLoader loader) {
        locker.writeLock().lock();
        this.loader = loader;
        locker.writeLock().unlock();
    }
    
    /**
     * Sets an instance property.  This property is only available
     * to TypeLoader(s) in the current instance.
     *
     * @param key	A String identifying the property.
     * @param value	The value of the property.
     * @see #setCommonProperty(String, Object)
     */
    public void setProperty(String key, Object value) {
        locker.writeLock().lock();
        typeProperties.put(key, value);
        locker.writeLock().unlock();
    }
    
    /**
     * <p>Returns whether or not this resource injector uses a
     * <code>BeanInfo</code> to find an appropriate mutator for
     * injected fields.</p>
     * 
     * @see #setUseBeanInfo(boolean)
     * @return True when the use of JavaBeans accessor has been
     *         set, false otherwise.
     * @since 0.3
     */
    public boolean isUseBeanInfo() {
        return useBeanInfo;
    }

    /**
     * <p>Enables or disables the use of JavaBean accessors for injection.
     * When enabled, Fuse will request the <code>BeanInfo</code> of
     * the injected object to find its mutators. If no bean info is
     * defined, Fuse will use the <code>set&lt;&lt;PropertyName&gt;&gt;()</code>
     * idiom. If such a mutator does not exist, Fuse will inject directly
     * into the field.</p>
     * 
     * <p>When disabled Fuse will search for a mutator following the
     * <code>set&lt;&lt;PropertyName&gt;&gt;()</code> idiom. Would it fail,
     * the value is directly injected into the field.</p>
     *
     * @param useAccessors Enables use of JavaBean accessors when true.
     * @see #isUseBeanInfo()
     * @see BeanInfo
     * @since 0.3
     */
    public void setUseBeanInfo(boolean useAccessors) {
        locker.writeLock().lock();
        
        this.useBeanInfo = useAccessors;
        
        locker.writeLock().unlock();
    }

    /**
     * Returns the default Singleton instance of ResourceInjector.  This
     * is equivalent to calling <code>get(null)</code>.
     *
     * @see #get(Object)
     * @return An instance of ResourceInjector.
     */
    public static ResourceInjector get() {
        return get(null);
    }
    
    /**
     * Returns a Singleton instance of ResourceInjector matching
     * the specified key.  This method allows for multiple instances
     * of ResourceInjector while still maintaining the static, Singleton
     * properties.  If <code>null</code> is passed as the key the
     * default instance is returned.
     *
     * @param key	The key used to identify the Singleton instance.
     * @return	An instance of ResourceInjector.
     */
    public static synchronized ResourceInjector get(Object key) {
        if (key == null) {
            return DEFAULT_INSTANCE;
        }
        
        ResourceInjector injector = instances.get(key);
        if (injector == null) {
            injector = new ResourceInjector(key);
            instances.put(key, injector);
        }
        
        return injector;
    }
    
    /**
     * Sets a common property.  This property will be available
     * to all TypeLoader(s) in the current JVM.
     *
     * @param key	A String identifying the property.
     * @param value	The value of the property.
     * @see #setProperty(String, Object)
     */
    public static void setCommonProperty(String key, Object value) {
        commonProperties.put(key, value);
    }
    
    /**
     * Adds a module reflectively obtained using the specified
     * class name.  The module must be a subclass of FuseModule.
     * This method is identical to calling <code>addModule(FuseModule)</code>
     * and passing an instance of the specified class.
     *
     * @param module	A String class name indicating the module to add.
     * @see #addModule(FuseModule)
     * @see FuseModule
     */
    @SuppressWarnings("unchecked")
    public static void addModule(String module) throws ModuleInitException {
        Class<? extends FuseModule> clazz;
        FuseModule newModule;
        try {
            clazz = (Class<? extends FuseModule>) Class.forName(module);
            newModule = clazz.newInstance();
        } catch (ClassNotFoundException e) {
            ModuleInitException rethrow = new ModuleInitException(e.getMessage());
            rethrow.initCause(e);
            
            throw rethrow;
        } catch (InstantiationException e) {
            ModuleInitException rethrow = new ModuleInitException(e.getMessage());
            rethrow.initCause(e);
            
            throw rethrow;
        } catch (IllegalAccessException e) {
            ModuleInitException rethrow = new ModuleInitException(e.getMessage());
            rethrow.initCause(e);
            
            throw rethrow;
        }
        
        if (!FuseUtilities.isVersionValid(newModule.requiredVersion())) {
            throw new ModuleInitException("Module requires an incompatible core version: " +
                newModule.requiredVersion() + ".");
        }
        
        addModule(newModule);
    }
    
    /**
     * Adds the specified FuseModule instance.  This method is identical
     * to calling <code>addModule(String)</code> and passing the
     * class name.
     *
     * @param mod	A FuseModule instance to add to ResourceInjector.
     * @see #addModule(String)
     * @see FuseModule
     */
    public static void addModule(FuseModule mod) throws ModuleInitException {
        moduleLocker.writeLock().lock();
        if (modules.contains(mod)) {
            moduleLocker.writeLock().unlock();
            return;
        }
        modules.add(mod);
        FieldIterator.addStopPackages(mod.getStopPackages());
        moduleLocker.writeLock().unlock();
        
        try {
            mod.init();
        } catch (ModuleInitException e) {
            ModuleInitException rethrow = new ModuleInitException(e.getMessage());
            rethrow.initCause(e);
            
            throw rethrow;
        }
    }
    
    /**
     * Adds the the specified definition to the cache identified by the
     * specified key.  The specified key is what will identify the Definition
     * instance in the <code>@InjectedResource(definition="...")</code>
     * declaration.  The definition (if specified) will be used by the
     * <code>inject</code> method to determine the field metadata
     * required for the injection algorithm.
     *
     * @param key	A String value used to identify the Definition instance.
     * @param def	A Definition instance to be (potentially) used by the <code>inject</code>
     * 	method to obtain field metadata.
     * @since 0.2
     */
    public static synchronized void addDefinition(String key, Definition def) {
        definitions.put(key, def);
    }
    
    void injectResource(Object component, Class<?> componentClass, Field field,
                        BeanInfo beanInfo, InjectedResource annotation, String manualDefinition) {
        if (component == null) {
            throw new TypeLoadingException("You cannot use a definition with " + field.getName() +
                                           ". Enclosing instance is null.");
        }
        
        Definition definition = null;
        if (manualDefinition != null || (annotation != null && annotation.definition().length() > 0)) {
            definition = (manualDefinition == null ?
                          definitions.get(annotation.definition()) : definitions.get(manualDefinition));

            if (definition == null || !definition.isInjectedField(field.getName())) {
                return;
            }
        }

        String[] nameValue = new String[2];
        getNameAndValue(componentClass, field, annotation, definition, nameValue);
        String name = nameValue[0];
        String value = nameValue[1];
        
        Object resource;
        
        if (field.getType().isArray()) {
            TypeLoader<Object> typeloader = new SpecialArrayTypeLoader(field.getType(), this);

            resource = typeloader.loadTypeWithCaching(name, value, componentClass, typeProperties);
        } else {
            TypeLoader<?> typeLoader = TypeLoaderProvider.get(name, field, annotation, definition);

            resource = typeLoader.loadTypeWithCaching(name, value, componentClass, typeProperties);
            configureResource(componentClass, name, typeLoader, resource);
        }

        ValueInjectionProvider provider = ValueInjectionProvider.get(field, beanInfo, useBeanInfo);
        provider.setValue(component, resource);
    }
    
    private void getNameAndValue(Class<?> klass, Field field, InjectedResource annotation,
                                 Definition definition, String[] nameValue) {
        NameAndValueProvider provider = NameAndValueProvider.get(klass, field, annotation, definition);
        String firstAttempt = provider.getNameAndValue(this, nameValue);

        if (nameValue[1] == null) {
            if (!klass.equals(field.getDeclaringClass())) {
                provider = NameAndValueProvider.get(field.getDeclaringClass(), field, annotation, definition);
                String secondAttempt = provider.getNameAndValue(this, nameValue);
            
                if (nameValue[1] == null) {
                    throw new TypeLoadingException("Theme resource " + firstAttempt + " and " +
                                                   secondAttempt + " do not exist.");
                }
            } else {
                throw new TypeLoadingException("Theme resource " + firstAttempt + " do not exist.");
            }
        }
    }

    @SuppressWarnings("unchecked")
    void configureResource(Class<?> klass, String name, TypeLoader typeloader, Object resource) {
        Map<String, String> values = new HashMap<String, String>();
        for (String childKey : typeloader.getChildKeys(resource)) {
            values.put(childKey, getValue(name + '.' + childKey));
        }
        typeloader.configureType(resource, values, klass, typeProperties);
    }

    // Never call without acquiring read lock locker
    String getValue(String name) {
        return getValue(name, false);
    }
    
    // Never call without acquiring read lock locker
    private String getValue(String name, boolean subCall) {
        if (!subCall) {
            cycle.clear();
        }
        
        if (subCall && cycle.contains(name)) {
            throw new TypeLoadingException("Theme resource " + name +
                " cannot be resolved. Dependency cycle detected");
        }
        
        String value = loader.get(name);
        if (value == null) {
            return null;
        }
        
        cycle.add(name);
        value = resolveReferences(name, value);
        cycle.remove(name);

        return value;
    }

    private String resolveReferences(String name, String value) {
        StringBuilder buffer = new StringBuilder(value.length());
        StringBuilder reference = null;
        
        boolean backslash = false;
        boolean inReference = false;

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            
            if (inReference && c == '}') {
                try {
                    buffer.append(getValue(reference.toString(), true));
                } catch (TypeLoadingException e) {
                    throw new TypeLoadingException("Theme resource " + name +
                                                   " cannot be resolved.", e);
                } finally {
                    inReference = false;
                    reference = null;
                }
            } else {
                if (!backslash) {
                    if (c == '\\') {
                        backslash = true;
                    } else if (c == '{') {
                        inReference = true;
                        reference = new StringBuilder();
                    } else if (c != '}') {
                        (inReference ? reference : buffer).append(c);
                    }
                } else {
                    if (c != '{' && c!= '}') {
                        (inReference ? reference : buffer).append('\\');
                    }
                    if (c != '\\') {
                        (inReference ? reference : buffer).append(c);
                    }
                    backslash = false;
                }
            }
        }
        
        if (inReference) {
            buffer.append(reference);
        }
        
        value = buffer.toString();
        return value;
    }
}
