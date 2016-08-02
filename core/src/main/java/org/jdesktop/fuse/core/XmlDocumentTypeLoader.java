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

import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jdesktop.fuse.TypeLoader;
import org.jdesktop.fuse.TypeLoadingException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.util.URI;
import com.sun.org.apache.xerces.internal.util.URI.MalformedURIException;

class XmlDocumentTypeLoader extends TypeLoader<Document> {
    XmlDocumentTypeLoader() {
        super(Document.class);
    }

    @Override
    public Document loadType(String name, String value, Class<?> resolver, Map<String, Object> properties) {
        DocumentBuilder builder = null;
        
        if (properties == null || properties.get("xml.builder") == null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new TypeLoadingException("Theme resource " + name + " cannot be loaded: " +
                                               "no XML document can be created." , e);
            }
        } else {
            builder = (DocumentBuilder) properties.get("xml.builder");
        }

        if (value.indexOf(':') == -1) {
            try {
                return builder.parse(resolver.getResourceAsStream(value));
            } catch (SAXException e) {
                throw new TypeLoadingException("Theme resource " + name + " cannot be loaded. " +
                                               "XML document " + value + " is malformed." , e);
            } catch (IOException e) {
                throw new TypeLoadingException("Theme resource " + name + " cannot be loaded. " +
                                               "XML document " + value + " cannot be found." , e);
            }
        }
        
        try {
            new URI(value);
        } catch (MalformedURIException e1) {
            try {
                return builder.parse(resolver.getResourceAsStream(value));
            } catch (SAXException e) {
                throw new TypeLoadingException("Theme resource " + name + " cannot be loaded. " +
                                               "XML document " + value + " is malformed." , e);
            } catch (IOException e) {
                throw new TypeLoadingException("Theme resource " + name + " cannot be loaded. " +
                                               "XML document " + value + " cannot be found." , e);
            }
        }

        try {
            return builder.parse(value);
        } catch (SAXException e) {
            throw new TypeLoadingException("Theme resource " + name + " cannot be loaded. " +
                                           "XML document " + value + " is malformed." , e);
        } catch (IOException e) {
            throw new TypeLoadingException("Theme resource " + name + " cannot be loaded. " +
                                           "XML document URI " + value + " cannot be found." , e);
        }
    }
}
