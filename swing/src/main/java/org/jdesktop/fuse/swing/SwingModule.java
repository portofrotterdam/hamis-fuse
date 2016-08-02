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

import static org.jdesktop.fuse.AutoInjection.addAutoInjectionProvider;
import static org.jdesktop.fuse.TypeLoaderFactory.addTypeLoader;

import org.jdesktop.fuse.FuseModule;
import org.jdesktop.fuse.ModuleInitException;

/**
 * This class is the FuseModule subclass encapsulating the /Swing
 * module.  To perform resource injection on any Swing types, this
 * class must be used to add the /Swing module to ResourceInjector.
 * For example:
 * <pre>ResourceInjector.get().load("testui.theme");
 *ResourceInjector.get().addModule("org.jdesktop.fuse.swing.SwingModule");</pre>
 * 
 * <p>Alternatively, you can create an instance of SwingModule and add that
 * using the overloaded method.</p>
 * 
 * @see org.jdesktop.fuse.FuseModule
 * @see org.jdesktop.fuse.ResourceInjector#addModule(String)
 * @see org.jdesktop.fuse.ResourceInjector#addModule(FuseModule)
 * @since 0.1
 * @author Romain Guy
 */
public final class SwingModule extends FuseModule {

	@Override
	public String requiredVersion() {
		return ">=0.3";
	}

	@Override
	public void init() throws ModuleInitException {
        addTypeLoader(new AlphaCompositeTypeLoader());        
        addTypeLoader(new ColorTypeLoader());
        addTypeLoader(new CursorTypeLoader());
        addTypeLoader(new DimensionTypeLoader());
        addTypeLoader(new FontTypeLoader());
        addTypeLoader(new GradientTypeLoader());
        addTypeLoader(new ImageTypeLoader());
        addTypeLoader(new ImageIconTypeLoader());
        addTypeLoader(new InsetsTypeLoader());
        addTypeLoader(new PointTypeLoader());
        addTypeLoader(new Point2dTypeLoader());
        addTypeLoader(new RectangleTypeLoader());
        addTypeLoader(new Rectangle2dTypeLoader());
        addTypeLoader(new RenderingHintsTypeLoader());
        
        addAutoInjectionProvider(new SwingAutoInjectionProvider());
	}
}
