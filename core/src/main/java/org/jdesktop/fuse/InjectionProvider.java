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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 
 * @since 0.3
 * @author Romain Guy
 */
abstract class InjectionProvider {
    static InjectionProvider get(InjectedResource annotation) {
        if (annotation != null && annotation.definition().length() > 0) {
            return new InjectionFromDefinitionProvider();
        } else {
            return new SimpleInjectionProvider();
        }
    }
    
    abstract void inject(ResourceInjector injector, Object component, Class<?> componentClass,
                         AnnotatedField field, BeanInfo info, boolean useBeanInfo, boolean populateHierarchy);
    
    private final static class SimpleInjectionProvider extends InjectionProvider {
        @Override
        void inject(ResourceInjector injector, Object component, Class<?> componentClass,
                    AnnotatedField field, BeanInfo info, boolean useBeanInfo, boolean populateHierarchy) {
            injector.injectResource(component, componentClass, field.getField(), info, field.getAnnotation(), null);
        }
    }
    
    private final static class InjectionFromDefinitionProvider extends InjectionProvider {
        @Override
        void inject(ResourceInjector injector, Object component, Class<?> componentClass,
                    AnnotatedField field, BeanInfo info, boolean useBeanInfo, boolean populateHierarchy) {
            Field componentField = field.getField();
            componentField.setAccessible(true);

            try {
                component = componentField.get(component);
            } catch (IllegalArgumentException e) {
                throw new TypeLoadingException(e);
            } catch (IllegalAccessException e) {
                throw new TypeLoadingException(e);
            }
            
            Class<?> klass = componentField.getType();
            FieldIterator iterator = FieldIterator.get(klass, populateHierarchy, true);

            BeanInfo beanInfo;
            try {
                beanInfo = BeanInfoProvider.get(klass, useBeanInfo);
            } catch (IntrospectionException e) {
                throw new TypeLoadingException(e);
            }

            for (Field definedField : iterator) {
                if (!Modifier.isStatic(definedField.getModifiers())) {
                    injector.injectResource(component, componentClass, definedField, beanInfo, field.getAnnotation(), null);
                }
            }
        }
    }
}
