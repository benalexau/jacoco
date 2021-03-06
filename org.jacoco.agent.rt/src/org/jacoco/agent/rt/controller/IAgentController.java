/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.agent.rt.controller;

import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.IRuntime;

/**
 * Common interface for different implementations that control execution data
 * dumps.
 */
public interface IAgentController {

	/**
	 * Configure the agent controller with the supplied options and connect it
	 * to the coverage runtime
	 * 
	 * @param options
	 *            Options used to configure the agent controller
	 * @param runtime
	 *            Coverage runtime this agent controller will be connected to
	 */
	public void startup(final AgentOptions options, final IRuntime runtime)
			throws Exception;

	/**
	 * Shutdown the agent controller and clean up any resources it has created.
	 */
	public void shutdown() throws Exception;

	/**
	 * Write all execution data in the runtime to a location determined by the
	 * agent controller. This method should only be called by the Agent
	 */
	public void writeExecutionData() throws Exception;

}
