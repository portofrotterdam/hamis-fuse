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
import java.lang.reflect.Modifier;
import java.util.Iterator;

/**
 * 
 * @since 0.3
 * @author Romain Guy
 */
abstract class AnnotatedFieldSelector implements Iterable<AnnotatedField>, Iterator<AnnotatedField> {
    private AnnotatedFieldSelector() {
    }
    
    static AnnotatedFieldSelector get(FieldIterator iterator) {
        return new InjectedResourceFieldSelector(iterator);
    }

    public Iterator<AnnotatedField> iterator() {
        return this;
    }

    public boolean hasNext() {
        return false;
    }

    public AnnotatedField next() {
        return null;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    private final static class InjectedResourceFieldSelector extends AnnotatedFieldSelector {
        private final FieldIterator fieldIterator;
        private AnnotatedField nextField;

        private InjectedResourceFieldSelector(FieldIterator fieldIterator) {
            this.fieldIterator = fieldIterator;
            this.nextField = findNextAnnotatedField();
        }

        private AnnotatedField findNextAnnotatedField() {
            while (fieldIterator.hasNext()) {
                Field field = fieldIterator.next();
                InjectedResource annotation = field.getAnnotation(InjectedResource.class);
                if (annotation != null && !Modifier.isStatic(field.getModifiers())) {
                    return new AnnotatedField(field, annotation);
                }
            }
            return null;
        }

        @Override
        public boolean hasNext() {
            return nextField != null;
        }

        @Override
        public AnnotatedField next() {
            AnnotatedField field = nextField;
            nextField = findNextAnnotatedField();
            return field;
        }
    }

}
