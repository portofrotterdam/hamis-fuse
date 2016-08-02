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
 * <p>This exception may be thrown during either the load or 
 * parse process of a TypeLoader.  This exception will be thrown
 * by ResourceInjector if instantiation or lookup of a TypeLoader fails.  
 * It could also be thrown by a TypeLoader if a problem arrises
 * in parsing a value.</p>
 * 
 * @see ResourceInjector
 * @see TypeLoader
 * @since 0.1
 * @author Romain Guy
 */
public class TypeLoadingException extends RuntimeException {
	
	/**
	 * Create a new exception with the specified message.
	 * 
	 * @param message	The message to be displayed by the exception.
	 */
    public TypeLoadingException(String message) {
        super(message);
    }

	/**
	 * Create a new exception with the specified message and 
	 * initCause.  Invoking this constructor is identical to invoking
	 * <pre>TypeLoadingException e = new TypeLoadingException(message);
	 * e.initCause(cause);</pre>
	 * 
	 * @param message	The message to be displayed by the exception.
	 * @param cause	The initCause for this exception.
	 */
    public TypeLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create a new exception with an empty message and 
     * initCause.  Invoking this constructor is identical to invoking
     * <pre>TypeLoadingException e = new TypeLoadingException("");
     * e.initCause(cause);</pre>
     * 
     * @param cause The initCause for this exception.
     */
    public TypeLoadingException(Throwable cause) {
        this("", cause);
    }
}