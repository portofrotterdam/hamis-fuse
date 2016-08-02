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

/**
 * 
 * @since 0.3
 * @author Romain Guy
 */
abstract class NameAndValueProvider {
    static NameAndValueProvider get(Class<?> klass, Field field,
                                    InjectedResource annotation, Definition definition) {
        if (definition != null) {
            if (definition.key(field.getName()).length() > 0) {
                return new DefinitionKeyProvider(field, definition);
            } else if (definition.name(field.getName()).length() > 0) {
                return new DefinitionNameProvider(field, definition, klass);
            } else {
                return new DefinitionProvider(field, klass);
            }
        } else if (annotation.key().length() > 0) {
            return new AnnotationKeyProvider(annotation);
        } else if (annotation.name().length() > 0) {
            return new AnnotationNameProvider(annotation, klass);
        }
        
        return new FieldNameProvider(field, klass);
    }
    
    abstract String getNameAndValue(ResourceInjector injector, String[] nameValue);
    
    private static final class DefinitionKeyProvider extends NameAndValueProvider {
        private final Field field;
        private final Definition definition;

        private DefinitionKeyProvider(Field field, Definition definition) {
            this.field = field;
            this.definition = definition;
        }

        @Override
        String getNameAndValue(ResourceInjector injector, String[] nameValue) {
            nameValue[0] = definition.key(field.getName());
            nameValue[1] = injector.getValue(nameValue[0]);
            
            return nameValue[0];
        }
    }

    private static final class DefinitionNameProvider extends NameAndValueProvider {
        private final Field field;
        private final Definition definition;
        private final Class<?> klass;

        private DefinitionNameProvider(Field field, Definition definition, Class<?> klass) {
            this.field = field;
            this.definition = definition;
            this.klass = klass;
        }

        @Override
        String getNameAndValue(ResourceInjector injector, String[] nameValue) {
            String name = FuseUtilities.getSimpleName(klass.getName()) + '.' +
                definition.name(field.getName());
            String value = injector.getValue(name);
            
            StringBuilder attempted = new StringBuilder();
            if (value == null) {
                attempted.append(name).append(" and ");
                name = "*." + definition.name(field.getName());
                attempted.append(name);
                value = injector.getValue(name);
            }
            nameValue[0] = name;
            nameValue[1] = value;
            
            return attempted.toString();
        }
    }
    
    private static final class DefinitionProvider extends NameAndValueProvider {
        private final Field field;
        private final Class<?> klass;

        private DefinitionProvider(Field field, Class<?> klass) {
            this.field = field;
            this.klass = klass;
        }

        @Override
        String getNameAndValue(ResourceInjector injector, String[] nameValue) {
            String name = FuseUtilities.getSimpleName(klass.getName()) + '.' + field.getName();
            String value = injector.getValue(name);

            StringBuilder attempted = new StringBuilder();
            if (value == null) {
                attempted.append(name).append(" and ");
                name = "*." + field.getName();
                attempted.append(name);
                value = injector.getValue(name);
            }
            nameValue[0] = name;
            nameValue[1] = value;

            return attempted.toString();
        }
    }

    private static final class AnnotationKeyProvider extends NameAndValueProvider {
        private final InjectedResource annotation;

        private AnnotationKeyProvider(InjectedResource annotation) {
            this.annotation = annotation;
        }

        @Override
        String getNameAndValue(ResourceInjector injector, String[] nameValue) {
            nameValue[0] = annotation.key();
            nameValue[1] = injector.getValue(nameValue[0]);
            
            return nameValue[0];
        }
    }
    
    private static final class AnnotationNameProvider extends NameAndValueProvider {
        private final InjectedResource annotation;
        private final Class<?> klass;

        private AnnotationNameProvider(InjectedResource annotation, Class<?> klass) {
            this.annotation = annotation;
            this.klass = klass;
        }

        @Override
        String getNameAndValue(ResourceInjector injector, String[] nameValue) {
            String name = FuseUtilities.getSimpleName(klass.getName()) + '.' + annotation.name();
            String value = injector.getValue(name);
            
            StringBuilder attempted = new StringBuilder();
            if (value == null) {
                attempted.append(name).append(" and ");
                name = "*." + annotation.name();
                attempted.append(name);
                value = injector.getValue(name);
            }
            
            nameValue[0] = name;
            nameValue[1] = value;

            return attempted.toString();
        }
    }
    
    private static final class FieldNameProvider extends NameAndValueProvider {
        private final Class<?> klass;
        private Field field;

        private FieldNameProvider(Field field, Class<?> klass) {
            this.field = field;
            this.klass = klass;
        }

        @Override
        String getNameAndValue(ResourceInjector injector, String[] nameValue) {
            String name = FuseUtilities.getSimpleName(klass.getName()) + '.' + field.getName();
            String value = injector.getValue(name);
            
            StringBuilder attempted = new StringBuilder();
            if (value == null) {
                attempted.append(name).append(" and ");
                name = "*." + field.getName();
                attempted.append(name);
                value = injector.getValue(name);
            }
            
            nameValue[0] = name;
            nameValue[1] = value;

            return attempted.toString();
        }
    }
}
