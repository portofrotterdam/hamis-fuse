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

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 
 * @since 0.3
 * @author Romain Guy
 */
abstract class FieldIterator implements Iterable<Field>, Iterator<Field> {
    private static final List<String> stopSetCacheMatch = Collections.synchronizedList(new LinkedList<String>());
    private static final List<String> stopSetCacheUnmatch = Collections.synchronizedList(new LinkedList<String>());
    
    private static Set<String> stopSet = Collections.synchronizedSet(new HashSet<String>());
    
    private FieldIterator() {
    }
    
    static FieldIterator get(Class<?> klass, boolean populateHierarchy, boolean ignoreStopPackages) {
        if (populateHierarchy) {
            return new HierarchyFieldIterator(klass, ignoreStopPackages);
        }
        return new SimpleFieldIterator(klass);
    }
    
    static void addStopPackages(String[] stopPackages) {
        for (String packagename : stopPackages) {
            stopSet.add(packagename);
        }
    }
    
    public Iterator<Field> iterator() {
        return this;
    }

    public boolean hasNext() {
        return false;
    }

    public Field next() {
        return null;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    private static boolean isStopSet(String packagename) {
        String[] packages = packagename.split("\\.");
        
        if (stopSetCacheMatch.contains(packagename)) {
            return true;
        }
        
        if (stopSetCacheUnmatch.contains(packagename)) {
            return false;
        }
        
        for (String blob : stopSet) {
            String[] tokens = blob.split("\\.");
            boolean match = true;
            
            for (int i = 0; i < tokens.length; i++) {
                if (tokens[i].equals("*")) {
                    stopSetCacheMatch.add(packagename);
                    
                    return true;
                }
                
                if (!tokens[i].equals(packages[i])) {
                    match = false;
                    break;
                }
            }
            
            if (match) {
                stopSetCacheMatch.add(packagename);
                
                return true;
            }
        }
        
        stopSetCacheUnmatch.add(packagename);
        
        return false;
    }
    
    private static String getPackage(String classname) {
        String[] tokens = classname.split("\\.");
        String back = "";
        
        for (String token : tokens) {
            if (!token.equals(tokens[tokens.length - 1])) {
                back += token;
            }
        }
        
        return back;
    }
    
    private final static class SimpleFieldIterator extends FieldIterator {
        private final Field[] fields;
        private int index;

        private SimpleFieldIterator(Class<?> klass) {
            fields = klass.getDeclaredFields();
            index = 0;
        }

        @Override
        public boolean hasNext() {
            return index < fields.length;
        }

        @Override
        public Field next() {
            return fields[index++];
        }
    }

    private final static class HierarchyFieldIterator extends FieldIterator {
        private Field[] fields;
        private int index;
        private Class<?> klass;
        private boolean ignoreStopPackages;
        private boolean hasSuperClass = false;

        private HierarchyFieldIterator(Class<?> klass, boolean ignoreStopPackages) {
            this.klass = klass;
            this.ignoreStopPackages = ignoreStopPackages;

            findFirstClassWithFields();
        }

        private void findFirstClassWithFields() {
            do {
                fields = klass.getDeclaredFields();
                if (fields.length == 0) {
                    if (klass.getSuperclass() != null &&
                        (ignoreStopPackages || !isStopSet(getPackage(klass.getSuperclass().getName())))) {
                        klass = klass.getSuperclass();
                        if (klass != null) {
                            fields = klass.getDeclaredFields();
                        }
                    } else {
                        klass = null;
                    }
                }
            } while (klass != null && fields.length == 0);

            if (klass != null) {
                hasSuperClass = klass.getSuperclass() != null &&
                    (ignoreStopPackages || !isStopSet(getPackage(klass.getSuperclass().getName())));
            }

            index = 0;
        }

        @Override
        public boolean hasNext() {
            return klass != null && hasSuperClass;
        }

        @Override
        public Field next() {
            Field f = fields[index++];
            if (index >= fields.length) {
                klass = klass.getSuperclass();
                if (klass != null) {
                    findFirstClassWithFields();
                }
                index = 0;
            }

            return f;
        }
    }
}
