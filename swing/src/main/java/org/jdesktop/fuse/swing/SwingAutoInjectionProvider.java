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

package org.jdesktop.fuse.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

import org.jdesktop.fuse.AutoInjected;
import org.jdesktop.fuse.AutoInjectionProvider;
import org.jdesktop.fuse.Hive;
import org.jdesktop.fuse.SwingHive;

/**
 * @author Romain Guy
 * @author Daniel Spiewak
 */
class SwingAutoInjectionProvider extends AutoInjectionProvider<Component> {
    private Map<String, String> bindings =
        Collections.synchronizedMap(new HashMap<String, String>());
    
    private final Object LOCK = new Object();
    
    SwingAutoInjectionProvider() {
        super(Component.class);
    }
    
    @SuppressWarnings({"unchecked"})
    @Override
    protected <S extends Component> Hive<Component> enable(Hive<Component> hive, S container) {
        if (!(container instanceof Container)) {
            throw new IllegalArgumentException("Only subclasses of java.awt.Container can be enabled.");
        }
        
        if (hive == null) {
            hive = new SwingHive();
            
            synchronized (LOCK) {
                copyMap(bindings, hive.getBindings());
                
                bindings = hive.getBindings();
            }
        }

        if (!isRegistered((Container) container)) {
            register(new AutoInjector(hive), (Container) container);
        }

        return hive;
    }
    
    @Override
    protected void bind(String name, String definition) {
        synchronized (LOCK) {
            bindings.put(name, definition);
        }
    }

    private static boolean isRegistered(Container container) {
        ContainerListener[] listeners = container.getContainerListeners();
        for (ContainerListener listener : listeners) {
            if (listener instanceof AutoInjector) {
                return true;
            }
        }
        return false;
    }
    
    private static void register(AutoInjector listener, Container container) {
        if (!isRegistered(container)) {
            container.addContainerListener(listener);
        }

        Component[] components = container.getComponents();
        if (components.length == 0) {
            return;
        }
        
        for (Component component : components) {
            if (component != null && component instanceof Container) {
                register(listener, (Container) component);
            }
        }
    }
    
    private static <K, V> void copyMap(Map<K, V> from, Map<K, V> to) {
        for (K key : from.keySet()) {
            if (!to.containsKey(key)) {
                to.put(key, from.get(key));
            }
        }
    }

    private class AutoInjector implements ContainerListener {
        private final Hive<Component> hive;
        
        private AutoInjector(Hive<Component> hive) {
            this.hive = hive;
        }

        public void componentAdded(ContainerEvent e) {
            Component component = e.getChild();

            if (component != null) {
                AutoInjected annotation = component.getClass().getAnnotation(AutoInjected.class);
                if (annotation == null || !annotation.disabled()) {
                    String name = component.getName();
                    if (component instanceof JComponent) {
                        Object property = ((JComponent) component).getClientProperty(SwingHive.AUTO_INJECTION_KEY);
                        if (property != null) {
                            name = property.toString();
                        }
                    }
                    String definition = bindings.get(name);

                    if (definition != null) {
                        hive.addBoundInstance(component);
                        hive.inject(component);
                    } else {
                        hive.inject(component);
                    }
                }
            }
            
            if (component instanceof Container) {
                register(this, (Container) component);
            }
        }

        public void componentRemoved(ContainerEvent e) {
            Component component = e.getChild();
            if (component != null && component instanceof Container) {
                ((Container) component).removeContainerListener(this);
            }
        }
    }
}
