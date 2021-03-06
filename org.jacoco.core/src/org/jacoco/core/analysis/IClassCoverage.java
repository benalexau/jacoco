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
package org.jacoco.core.analysis;

import java.util.Collection;

/**
 * Coverage data of a single class containing methods. The name of this node is
 * the fully qualified class name in VM notation (slash separated).
 * 
 * @see IMethodCoverage
 */
public interface IClassCoverage extends ISourceNode {

	/**
	 * Returns the identifier for this class which is the CRC64 signature of the
	 * class definition.
	 * 
	 * @return class identifier
	 */
	public long getId();

	/**
	 * Returns the VM signature of the class.
	 * 
	 * @return VM signature of the class (may be <code>null</code>)
	 */
	public String getSignature();

	/**
	 * Returns the VM name of the superclass.
	 * 
	 * @return VM name of the super class (may be <code>null</code>, i.e.
	 *         <code>java/lang/Object</code>)
	 */
	public String getSuperName();

	/**
	 * Returns the VM names of implemented/extended interfaces
	 * 
	 * @return VM names of implemented/extended interfaces
	 */
	public String[] getInterfaceNames();

	/**
	 * Returns the VM name of the package this class belongs to.
	 * 
	 * @return VM name of the package
	 */
	public String getPackageName();

	/**
	 * Returns the optional name of the corresponding source file.
	 * 
	 * @return name of the corresponding source file
	 */
	public String getSourceFileName();

	/**
	 * Returns the methods included in this class.
	 * 
	 * @return methods of this class
	 */
	public Collection<IMethodCoverage> getMethods();

}