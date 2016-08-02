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

package org.jdesktop.fuse.core;

import static org.jdesktop.fuse.TypeLoaderFactory.addTypeLoader;

import org.jdesktop.fuse.FuseModule;
import org.jdesktop.fuse.ModuleInitException;

/**
 * This class is the FuseModule subclass encapsulating the /Core
 * module.  The /Core module is automatically added to
 * ResourceInjector and does not need to be manually managed.
 * 
 * @see org.jdesktop.fuse.FuseModule
 * @see org.jdesktop.fuse.ResourceInjector#addModule(String)
 * @see org.jdesktop.fuse.ResourceInjector#addModule(FuseModule)
 * @since 0.1
 * @author Romain Guy
 */
public final class CoreModule extends FuseModule {
    @Override
    public String requiredVersion() {
        return ">=0.1";
    }

    @Override
    public void init() throws ModuleInitException {
        addTypeLoader(new BooleanTypeLoader());
        addTypeLoader(new ByteTypeLoader());
        addTypeLoader(new CalendarTypeLoader());
        addTypeLoader(new CharacterTypeLoader());
        addTypeLoader(new DateTypeLoader());
        addTypeLoader(new DoubleTypeLoader());
        addTypeLoader(new FileTypeLoader());
        addTypeLoader(new FloatTypeLoader());
        addTypeLoader(new IntTypeLoader());
        addTypeLoader(new LongTypeLoader());
        addTypeLoader(new ShortTypeLoader());
        addTypeLoader(new StringTypeLoader());
        addTypeLoader(new StringBufferTypeLoader());
        addTypeLoader(new StringBuilderTypeLoader());
        addTypeLoader(new URITypeLoader());
        addTypeLoader(new URLTypeLoader());
        addTypeLoader(new UUIDTypeLoader());
        addTypeLoader(new XmlDocumentTypeLoader());
    }
}
