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

import static com.googlecode.jaev.Check.notBlank;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The socket channel conversation implements a conversation to a SMTP server
 * via a socket channel.
 * 
 * @author Niclas Meier
 */
public class SocketChannelConversation implements Conversation {

	/** Logger */
	private static final Logger LOG = LoggerFactory.getLogger(SocketChannelConversation.class);

	/** Character set for US-ASCII */
	private static final Charset CHARSET = Charset.forName("US-ASCII");

	/** Character set decoder */
	private static final CharsetDecoder DECODER = CHARSET.newDecoder();

	/** Character set encoder */
	private static final CharsetEncoder ENCODER = CHARSET.newEncoder();

	/** Byte buffer for network transmission */
	private final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

	/** The timeout to use for socket channel communication */
	private final int timeout;

	/** The socket address of the mail server */
	private InetSocketAddress socketAddress;

	/** The IRnternet address of the mail server */
	private InetAddress mxAddress;

	/** The socket channel for network communication */
	private SocketChannel channel = null;

	private boolean active = false;

	/**
	 * Constructor for the conversation
	 * 
	 * @param timeout
	 *            Timeout in milliseconds
	 */
	public SocketChannelConversation(long timeout) {
		this.timeout = (int) timeout;

		if (LOG.isDebugEnabled()) {
			LOG.debug("Initialized conversation with {} ms socket time out.", timeout);
		}
	}

	public void init(InetAddress mxAddress, int port) {
		this.mxAddress = mxAddress;
		this.socketAddress = new InetSocketAddress(this.mxAddress, port);
	}

	public void start() throws IOException {
		try {
			this.channel = createAndInitialiseChannel(this.timeout);
			this.channel.connect(this.socketAddress);
			this.active = true;

			if (LOG.isDebugEnabled()) {
				LOG.debug("Starting conversation with '" + this.socketAddress.getAddress().getCanonicalHostName() + ":"
						+ this.socketAddress.getPort() + "'.");
			}
		}
		catch (IOException e) {
			this.channel = null;
			this.active = false;
			throw e;
		}

	}

	public SocketChannel createAndInitialiseChannel(int timeout) throws IOException {
		SocketChannel channel = SocketChannel.open();
		Socket socket = channel.socket();
		socket.setSoTimeout(timeout);
		return channel;
	}

	public void say(Command command) throws IOException {
		sayToServer(command.brief() + "\r\n");
	}

	protected void sayToServer(String message) throws IOException {
		notBlank(message, "message");
		ByteBuffer messageBuffer = ENCODER.encode(CharBuffer.wrap(message));

		this.channel.write(messageBuffer);
		if (LOG.isTraceEnabled()) {
			LOG.trace(">>> " + message.replace("\r", "\\r").replace("\n", "\\n"));
		}
	}

	public Reply listen() throws IOException {
		String reply = listenToServer();

		if (reply.isEmpty()) {
			return Reply.EMPTY;
		}

		try {
			return Reply.create(reply.substring(0, 3), reply.substring(4));
		}
		catch (StringIndexOutOfBoundsException sioobe) {
			throw new IOException("Unable to parse server response: " + reply, sioobe);
		}
	}

	protected String listenToServer() throws IOException {
		StringBuilder result = new StringBuilder();
		this.buffer.clear();
		while (this.channel.read(this.buffer) > 0) {
			this.buffer.flip();
			CharBuffer charBuffer = DECODER.decode(this.buffer);
			result.append(charBuffer.toString());
		}

		if (LOG.isTraceEnabled()) {
			LOG.trace("<<< " + result.toString().replace("\r", "\\r").replace("\n", "\\n"));
		}

		return result.toString();
	}

	public void end() {
		try {
			if (this.channel != null) {
				if (LOG.isDebugEnabled()) {
					// a little trick: log only if a channel exists because
					// the connection failed if no channel exists.
					LOG.debug("Conversation to '" + this.socketAddress.getAddress().getCanonicalHostName()
							+ "' has ended.");
				}
				this.channel.close();
			}
		}
		catch (IOException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("An error occured while closing the socket channel.", e);
			}
		}
		finally {
			this.channel = null;
			this.socketAddress = null;
			this.mxAddress = null;
			this.active = false;
		}
	}

	public InetAddress getMxAddress() {
		return this.mxAddress;
	}

	public static class Factory implements ConversationFactory {

		private final long socketTimeout;

		public Factory(long socketTimeout) {
			this.socketTimeout = socketTimeout;
			if (LOG.isDebugEnabled()) {
				LOG.debug("Initialized socket channel conversation factory " + "with " + this.socketTimeout
						+ " ms socket time out.");
			}
		}

		@Override
		public Conversation createConversation() {
			return new SocketChannelConversation(this.socketTimeout);
		}

	}

	public boolean isActive() {
		return this.active;
	}
}
