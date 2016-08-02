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

/**
 * <p>This class is the superclass of any module initializers to be used
 * by the ResourceInjector.  Either instances of this class or a String
 * representing the fully qualified name of a subclass of this class
 * must be passed to the <code>ResourceInjector.addModule(String)</code>
 * or <code>ResourceInjector.addModule(FuseModule)</code> method.</p>
 * 
 * <p>FuseModule instances are used by ResourceInjector to initialize and
 * register the TypeLoader(s) available in a module.  For example, the /Swing
 * module packaged with Fuse contains a SwingModule class which creates
 * all of the Swing TypeLoader instances and then registers them with
 * TypeLoaderFactory.  Without this registration, ResourceInjector will be
 * unable to use those TypeLoader(s) to inject resources of the corresponding
 * types.</p>
 * 
 * <p><b>Important:</b> If the module is to be added to ResourceInjector reflectively
 * (i.e. using <code>ResourceInjector.addModule(String)</code>) the FuseModule
 * subclass must implement a public zero-argument constructor to allow
 * ResourceInjector to reflectively instantiate it.</p>
 * 
 * @see org.jdesktop.fuse.ResourceInjector#addModule(FuseModule)
 * @see org.jdesktop.fuse.ResourceInjector#addModule(String)
 * @see org.jdesktop.fuse.ModuleInitException
 * @since 0.1
 * @author Daniel Spiewak
 */
public abstract class FuseModule {
	
	/**
	 * <p>This method is to allow future changes in the Fuse API without
	 * breaking compatibility with previously developed modules.  Whenever
	 * a module is added to ResourceInjector it first checks to ensure that
	 * the module required version is compatible with the current version of
	 * the API.  If the version is incompatible, a <code>ModuleInitException</code>
	 * is thrown which can be caught and recovered from by the developer.</p>
	 * 
	 * <p>The version string takes the following format:<br>
	 * <table>
	 * 	<tbody>
	 * 		<tr>
	 * 			<td><b><u>Format</u></b></td>
	 * 
	 * 			<td><b><u>Meaning</u></b></td>
	 * 		</tr>
	 * 
	 * 		<tr>
	 * 			<td>&gt;=<i>version</i></td>
	 * 			<td>Any version equal to or greater than the one specified</td>
	 * 		</tr>
	 * 
	 * 		<tr>
	 * 			<td>&lt;=<i>version</i></td>
	 * 			<td>Any version less than or equal to the one specified</td>
	 * 		</tr>
	 * 
	 * 		<tr>
	 * 			<td>==<i>version</i></td>
	 * 			<td>Any version exactly equal to the one specified</td>
	 * 		</tr>
	 * 
	 * 		<tr>
	 * 			<td><i>version</i></td>
	 * 			<td>Any version exactly equal to the one specified</td>
	 * 		</tr>
	 * 
	 * 		<tr>
	 * 			<td>>=<i>version1</i>,==<i>version2</i>, ...</td>
	 * 			<td>A comma separated list of acceptable versions in the described notation</td>
	 * 		</tr>
	 * 	</tbody>
	 * </table><br>
	 * 
	 * The version number itself will always be a valid <code>Double</code> and <i>not</i> a string of this form:
	 * <code>1.123.381b</code></p>
	 * 
	 * @see org.jdesktop.fuse.ResourceInjector#addModule(FuseModule)
	 * @see org.jdesktop.fuse.ResourceInjector#addModule(String)
	 */
	public abstract String requiredVersion();
    
    /**
     * <p>This method allows individual modules to specify packages which
     * needn't be scanned by ResourceInjector for fields tagged for injection.
     * This is only required when the <code>populateHierarchy</code> flag
     * is set to <code>true</code> in the call to <code>ResourceInjector#inject(boolean, Object...)</code>.
     * Note: The java.* and javax.* packages are assumed in ResourceInjector
     * and don't need to be specified by every module.</p>
     * 
     * <p>Stop packages are specified either in one of the two following forms:</p>
     * 
     * <table>
     *  <tbody>
     *      <tr>
     *          <td><b><u>Form</u></b></td>
     * 
     *          <td><b><u>Signifies</u></b></td>
     *      </tr>
     * 
     *      <tr>
     *          <td><code>org.eclipse.swt.*</code>&nbsp;&nbsp;&nbsp;</td>
     * 
     *          <td>All classes within the <code>org.eclipse.swt</code> package <i>and subpackages</i></td>
     *      </tr>
     * 
     *      <tr>
     *          <td><code>org.xml.sax</code></td>
     * 
     *          <td>All classes within the <code>org.xml.sax</code> package alone</td>
     *      </tr>
     *  </tbody>
     * </table>
     * 
     * <p>The injection algorithm will halt when reaching the specified stop
     * packages.  This is important in considering that perhaps a class within a
     * stop package extends a class outside the stop packages.  Injection does
     * <i>not</i> continue to inject the classes beyond the stop packages.  In
     * other words, stop packages are not blacklisted packages, they are flags
     * to ResourceInjector indicating when to halt injection.</p>
     * 
     * @return An array of stop package 'blobs'
     * @since 0.2
     */
    public String[] getStopPackages() {
        return new String[0];
    }

	/**
	 * <p>This method is called by ResourceInjector when the <code>addModule</code>
	 * method is invoked.  This method should be used to instantiate and register the
	 * module TypeLoader(s) with TypeLoaderFactory.  Such instantiation and registration
	 * should <i>not</i> be done in the constructor or in the <code>requiredVersion()</code>
	 * method.  This method is invoked only once per module addition and always after
	 * the constructor and <code>requiredVersion()</code> have been called.  If the
	 * notation returned by <code>requiredVersion()</code> is incompatible with the
	 * current API version, then this method is never called and the <code>addModule</code>
	 * method throws an exception.</p>
	 * 
	 * @see org.jdesktop.fuse.ResourceInjector#addModule(String)
	 * @see org.jdesktop.fuse.ResourceInjector#addModule(FuseModule)
	 * @see #requiredVersion()
	 */
	public abstract void init() throws ModuleInitException;
}