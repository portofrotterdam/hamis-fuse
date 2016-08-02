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
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 
 * @since 0.3
 * @author Romain Guy
 */
abstract class ValueInjectionProvider {
    static ValueInjectionProvider get(Field field, BeanInfo beanInfo, boolean useBeanInfo) {
        if (useBeanInfo && beanInfo != null) {
            PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
            PropertyDescriptor propertyDescriptor = null;
            for (PropertyDescriptor d : descriptors) {
                if (d.getName().equals(field.getName())) {
                    propertyDescriptor = d;
                    break;
                }
            }
            
            if (propertyDescriptor != null) {
                return new BeanValueInjectionProvider(field, propertyDescriptor);
            }
        }
        return new DirectValueInjectionProvider(field);
    }
    
    abstract void setValue(Object component, Object resource);
    
    private static class DirectValueInjectionProvider extends ValueInjectionProvider {
        private final Field field;

        private DirectValueInjectionProvider(Field field) {
            this.field = field;
        }

        @Override
        void setValue(Object component, Object resource) {
            Class<?> klass = field.getDeclaringClass();
            
            String name = field.getName();

            if (!field.isAccessible()) {
                field.setAccessible(true);
            }

            try {
                field.set(component, resource);
            } catch (IllegalArgumentException e) {
                throw new TypeLoadingException("Cannot set the value of field " +
                                               name + " in class " + klass.getName());
            } catch (IllegalAccessException e) {
                throw new TypeLoadingException("Cannot set the value of field " +
                                               name + " in class " + klass.getName());
            }
        }
    }
    
    private static final class BeanValueInjectionProvider extends DirectValueInjectionProvider {
        private final Method setter;

        private BeanValueInjectionProvider(Field field,
                                           PropertyDescriptor propertyDescriptor) {
            super(field);
            this.setter = propertyDescriptor.getWriteMethod();
        }
        
        @Override
        void setValue(Object component, Object resource) {
            if (setter != null) {
                try {
                    setter.invoke(component, resource);
                } catch (IllegalArgumentException e) {
                    throw new TypeLoadingException(e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    throw new TypeLoadingException(e.getMessage(), e);
                } catch (InvocationTargetException e) {
                    throw new TypeLoadingException(e.getMessage(), e);
                }
            } else {
                super.setValue(component, resource);
            }
        }
    }
}
