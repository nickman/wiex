/**
 * Helios, OpenSource Monitoring
 * Brought to you by the Helios Development Group
 *
 * Copyright 2014, Helios Development Group and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org. 
 *
 */
package com.heliosapm.wiex.server.server.boot;

import java.beans.PropertyEditorSupport;
import javax.management.remote.JMXServiceURL;

/**
 * <p>Title: JMXServiceURLPropertyEditor</p>
 * <p>Description: Property editor for JMXServiceURLs</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.heliosapm.wiex.server.server.boot.JMXServiceURLPropertyEditor</code></p>
 */

public class JMXServiceURLPropertyEditor extends PropertyEditorSupport {

	/**
	 * Creates a new JMXServiceURLPropertyEditor
	 */
	public JMXServiceURLPropertyEditor() {

	}

	/**
	 * Creates a new JMXServiceURLPropertyEditor
	 * @param source
	 */
	public JMXServiceURLPropertyEditor(Object source) {
		super(source);
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.beans.PropertyEditorSupport#getAsText()
	 */
	@Override
	public String getAsText() {		
		Object obj =  super.getValue();
		return obj==null ? null : obj.toString();
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		try {
			setValue(new JMXServiceURL(text.trim()));
		} catch (Exception ex) {
			throw new RuntimeException("Failed to create JMXServiceURL from text [" + text + "]", ex);
		}
		
	}

}
