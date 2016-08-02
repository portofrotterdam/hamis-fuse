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

import java.awt.Component;

import org.jdesktop.fuse.Hive.ResourceInjectionListener;

/**
 * <p>A hive implementation designed specifically for use in injecting instances of
 * JComponent.  This hive listenens for an injection event fired from the
 * <code>Hive</code> class and repaints all injected JComponent(s) whenever the
 * event is handled.  This class would be appropriate for handling GUI theming in
 * a Swing based form.  The /Swing module is auto-loaded by this class.</p>
 * 
 * @since 0.2
 * @author Daniel Spiewak
 * @see javax.swing.Component
 */
public final class SwingHive extends Hive<Component> implements ResourceInjectionListener {
    public static final String AUTO_INJECTION_KEY = "fuse_auto_injection_binding";

    static {
        ResourceInjector.addModule("org.jdesktop.fuse.swing.SwingModule");
    }

    /**
     * Creates a new SwingHive adding itself as an injection listener.
     */
	public SwingHive() {
		this(null);
	}
    
    public SwingHive(Object key) {
        super(key);

        addResourceInjectionListener(this);
    }

	public void resourceInjected(ResourceInjectionEvent e) {
		for (Object obj : e.getObjects()) {
			((Component) obj).repaint();
		}
	}
    
    @Override
    protected HiveInjectionProvider<Component> getBindInjectionProvider() {
        return new SwingHiveBindInjectionProvider(getBindings());
    }
}
