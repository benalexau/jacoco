/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.report.internal.html;

import javax.xml.parsers.ParserConfigurationException;

import org.jacoco.report.internal.xml.XMLSupport;

/**
 * Support for verifying XHTML documents.
 */
public class HTMLSupport extends XMLSupport {

	public HTMLSupport() throws ParserConfigurationException {
		super(HTMLSupport.class);
	}

}
