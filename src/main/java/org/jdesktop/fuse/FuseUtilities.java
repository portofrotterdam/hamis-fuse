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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

/**
 * 
 * @since 0.3
 * @author Romain Guy
 */
final class FuseUtilities {
    private static double VERSION;

    private FuseUtilities() {
    }

    static {
        Properties bp = new Properties();
        InputStream is = FuseUtilities.class.getResourceAsStream("/build.properties");

        if (is == null) {
            try {
                is = new FileInputStream("build.properties");
            } catch (FileNotFoundException e) {
                VERSION = -1.0d;
            }
        }

        try {
            bp.load(is);

            VERSION = Double.parseDouble((String) bp.get("fuse.version"));

            is.close();
        } catch (IOException e) {
            VERSION = -1.0d;
        } catch (NullPointerException e) {
            VERSION = -1.0d;
        }
    }

    static boolean isVersionValid(String version) {
        String[] tokens = version.split(",");
        boolean back = false;
        
        for (String token : tokens) {
            if (token.charAt(1) == '=') {	// with operator token
                String op = token.substring(0, 2);
                
                if (op.equals(">=")) {
                    back = (back || Double.parseDouble(token.substring(2)) <= FuseUtilities.getVersion());
                } else if (op.equals("<=")) {
                    back = (back || Double.parseDouble(token.substring(2)) >= FuseUtilities.getVersion());
                } else {
                    back = (back || Double.parseDouble(token.substring(2)) == FuseUtilities.getVersion());
                }
            } else { // without operator token
                back = (back || Double.parseDouble(token) == FuseUtilities.getVersion());
            }
        }
        
        return back;
    }

    static String getSimpleName(String name) {
        int i = name.lastIndexOf('.');
        if (i == -1) {
            return name;
        } else {
            return name.substring(i + 1);
        }
    }

    static double getVersion() {
        return VERSION;
    }

    static void buildAndThrowChainedException(List<Exception> exceptions) {
        if (exceptions == null || exceptions.size() == 0) {
            return;
        }
        
        StringBuilder message = new StringBuilder();
        message.append(MessageFormat.format("{0, number, integer} {0, choice, 1#exception was|" +
            "1<exceptions were} encountered:\n", exceptions.size()));
        
        for (Exception e : exceptions) {
            message.append("\t\t");
            message.append(e.getMessage());
            message.append('\n');
        }
        
        throw new TypeLoadingException(message.toString());
    }
    
    static Class<?> getCallingClass(int depth) {
        StackTraceElement[] stack = new Exception().getStackTrace();
        try {
            return Class.forName(stack[depth + 2].getClassName());
        } catch (ClassNotFoundException e) {}
        
        return null;
    }
}
