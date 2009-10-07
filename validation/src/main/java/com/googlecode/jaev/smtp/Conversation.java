/*
 * Copyright 2009 - Niclas Meier
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.googlecode.jaev.smtp;

import java.io.IOException;
import java.net.InetAddress;

/**
 * <p>
 * The <code>Conversation</code> class encapsules a conversation to a SMTP MTA. It provides methods to transmit (
 * <code>say(..)</code>) commands to the MTA.
 * </p>
 * <p>
 * <i>Note:</i> The conversation is designed for reuse. The conversation state is changed within the
 * <code>init(..)</code> method and must be reset within the <code>end()</code> method.
 * </p>
 * 
 * @author Niclas Meier
 */
public interface Conversation {

	public void init(InetAddress mxAddress, int port);

	public void start() throws IOException;

	public void say(Command command) throws IOException;

	public Reply listen() throws IOException;

	public void end();

	public InetAddress getMxAddress();

	public boolean isActive();
}
