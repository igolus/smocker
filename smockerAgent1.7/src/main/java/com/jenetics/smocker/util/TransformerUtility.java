package com.jenetics.smocker.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.jenetics.smocker.util.io.TeeInputStream;
import com.jenetics.smocker.util.io.TeeOutputStream;
import com.jenetics.smocker.util.network.RemoteServerChecker;

public class TransformerUtility {
	private static String callerApp = null;

	private static Hashtable<Object, SmockerContainer> smockerContainerBySocket = new Hashtable<>();
	private TransformerUtility() {
		super();
	}

	public static String getCallerApp() {
		if (callerApp == null) {
			callerApp = "unknown";
			Map<Thread,StackTraceElement[]> stackTraceMap = Thread.getAllStackTraces();
			for (Thread t : stackTraceMap.keySet())
			{
				if ("main".equals(t.getName()))
				{
					StackTraceElement[] mainStackTrace = stackTraceMap.get(t);
					callerApp = mainStackTrace[mainStackTrace.length - 1].getClassName();
				}
			}
		}
		return callerApp;
	}

	public static synchronized int socketChannelWrite (final SocketChannel socketChannel, ByteBuffer bytebuffer) 
			throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if (socketChannel != null && !filterSmockerBehavior() && RemoteServerChecker.isRemoteServerAlive() 
				&& isConnectionWatched(socketChannel)) {
			int write = bytebuffer.limit();			
			int initialPosition = bytebuffer.position();

			byte[] array = null;
			byte[] outBytes = null;
			SmockerContainer smockerContainer = smockerContainerBySocket.get(socketChannel);
			if (smockerContainer == null) {
				InetSocketAddress remoteAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
				smockerContainer = addSmockerContainer(
						socketChannel, remoteAddress.getAddress().getHostAddress(), remoteAddress.getAddress().getHostName(), remoteAddress.getPort());
			}
			if (smockerContainer.isReseNextWrite()) {
				smockerContainer.postCommunication();
				smockerContainer.resetAll();
			}
			if (!smockerContainer.isApplyMock()) {
				write = writeSocketChannel(bytebuffer, socketChannel);
				array = getArrayFromBuffer(bytebuffer);
				outBytes = Arrays.copyOfRange(array, initialPosition, bytebuffer.position());
			}
			else {
				bytebuffer.position(bytebuffer.limit());
				array = getArrayFromBuffer(bytebuffer);
				outBytes = Arrays.copyOfRange(array, initialPosition, bytebuffer.limit());
				bytebuffer.position(0);

				SelectionKey[] keysSelection = getKeys(socketChannel);

				if (keysSelection != null && keysSelection.length > 0) {
					SelectionKey initialSelectionKey = keysSelection[0];

					Method getAttachmentMethod = initialSelectionKey.getClass().getMethod("getAttachment");
					final Object attached = getAttachmentMethod.invoke(initialSelectionKey);

					Method getSelectorMethod = initialSelectionKey.getClass().getMethod("getSelector");
					final Selector selectorNio = (Selector) getSelectorMethod.invoke(initialSelectionKey);

					Method getSelectedKeysMethod = selectorNio.getClass().getMethod("getSelectedKeys");
					Object selectedKeys = getSelectedKeysMethod.invoke(selectorNio);
					AbstractCollection<SelectionKey> collectionSelectedKeys = (AbstractCollection<SelectionKey>) selectedKeys;

					SelectionKey readSelectionKey = new NioCustomSelectionKey(selectorNio, socketChannel);

					Method setAttachmentMethod = readSelectionKey.getClass().getMethod("setAttachment", Object.class);
					setAttachmentMethod.invoke(readSelectionKey, attached);
					collectionSelectedKeys.add(readSelectionKey);
				}

			}

			smockerContainer.getSmockerSocketOutputStream().write(outBytes);
			return write;
		}
		else {
			return writeSocketChannel(bytebuffer, socketChannel);
		}
	}

	private static byte[] getArrayFromBuffer(ByteBuffer bytebuffer) {
		byte[] array;
		ByteBuffer bufferDup = bytebuffer.duplicate();
		array = new byte[bufferDup.position()];
		bufferDup.flip();
		bufferDup.get(array);
		return array;
	}

	public static synchronized int socketChannelRead (SocketChannel socketChannel,  ByteBuffer bytebuffer) 
			throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		SmockerContainer smockerContainer = smockerContainerBySocket.get(socketChannel);
		if (smockerContainer != null && socketChannel != null && !filterSmockerBehavior() && RemoteServerChecker.isRemoteServerAlive() 
				&& isConnectionWatched(socketChannel)) {
			int initialPosition = bytebuffer.position();

			smockerContainer.setReseNextWrite(true);
			if (smockerContainer.isApplyMock()) {
				byte[] matchMock = smockerContainer.getMatchMock();
				if (matchMock != null) {
					int index = smockerContainer.getIndexForArrayCopy();

					int lengthToCopy = Math.min(bytebuffer.limit(), matchMock.length - index);
					if (lengthToCopy == 0) {
						return -1;
					}
					byte[] targetBytes = new byte[lengthToCopy];
					System.arraycopy(matchMock, index, targetBytes, 0, lengthToCopy);
					smockerContainer.setIndexForArrayCopy(index + lengthToCopy);
					bytebuffer.put(targetBytes);
					return lengthToCopy;
				}
				else {
					if (!smockerContainer.isStreamResent()) {
						ByteBuffer buffer = ByteBuffer.wrap(smockerContainer.getSmockerSocketOutputStream().getBytes());
						writeSocketChannel(buffer, socketChannel);
						smockerContainer.setStreamResent(true);
					}
				}
				return readSocketChannel(bytebuffer, socketChannel);
			}
			int readen = readSocketChannel(bytebuffer, socketChannel);

			ByteBuffer bufferDup = bytebuffer.duplicate();
			byte[] array = new byte[bufferDup.position()];
			bufferDup.flip();
			bufferDup.get(array);

			byte[] outBytes = Arrays.copyOfRange(array, initialPosition, bytebuffer.position());
			if (array.length > 0) {
				smockerContainer.getSmockerSocketInputStream().write(outBytes);
			}


			return readen;
		}
		if (bytebuffer != null && socketChannel != null) {
			return readSocketChannel(bytebuffer, socketChannel);
		}
		return -1;

	}

	private static int readSocketChannel(ByteBuffer buffer, SocketChannel socketChannel) 
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Method readNew = socketChannel.getClass().
				getMethod("readNew", java.nio.ByteBuffer.class);
		return (int) readNew.invoke(socketChannel, buffer);

	}


	private static SelectionKey[] getKeys(SocketChannel socketChannel) 
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Method getKeys = socketChannel.getClass().
				getMethod("getKeys", null);
		return (SelectionKey[]) getKeys.invoke(socketChannel, null);

	}


	private static int writeSocketChannel(ByteBuffer buffer, SocketChannel socketChannel) 
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Method writeNew = socketChannel.getClass().
				getMethod("writeNew", java.nio.ByteBuffer.class);
		return (int) writeNew.invoke(socketChannel, buffer);
	}

	public static synchronized void socketChannelClosed (Object socketChannel) throws IOException {
		SmockerContainer smockerContainer = smockerContainerBySocket.get(socketChannel);

		if (smockerContainer != null && smockerContainer.isReseNextWrite()) {
			smockerContainer.postCommunication();
			smockerContainerBySocket.remove(socketChannel); 
		}
	}

	public static synchronized InputStream manageInputStream(InputStream is, Socket source) {
		if (!filterSmockerBehavior() && RemoteServerChecker.isRemoteServerAlive() && isConnectionWatched(source)) {
			if (!smockerContainerBySocket.containsKey(source)) {
				addSmockerContainer(source, source.getInetAddress().getHostAddress(), source.getInetAddress().getHostName() ,source.getPort());
			}
			SmockerContainer smockerContainer = smockerContainerBySocket.get(source);
			if (smockerContainer.getTeeInputStream() == null) {
				smockerContainer.resetSmockerSocketInputStream();
				TeeInputStream teeInputStream = new TeeInputStream(is, smockerContainer.getSmockerSocketInputStream(), true, smockerContainer);
				smockerContainer.setTeeInputStream(teeInputStream);
			}
			return smockerContainer.getTeeInputStream();
		}
		return is;
	}

	public static synchronized OutputStream manageOutputStream(OutputStream os, Socket source) {
		if (!filterSmockerBehavior() && RemoteServerChecker.isRemoteServerAlive() && isConnectionWatched(source)) {
			SmockerContainer container = null;
			if (!smockerContainerBySocket.containsKey(source)) {
				container = addSmockerContainer(source, source.getInetAddress().getHostAddress(), source.getInetAddress().getHostName(), 
						source.getPort());
			}
			else {
				container = smockerContainerBySocket.get(source);
			}
			container.resetSmockerSocketOutputStream();
			TeeOutputStream teeOutputStream = new TeeOutputStream(os, container.getSmockerSocketOutputStream());
			container.setTeeOutputStream(teeOutputStream);

			if (container.isApplyMock()) {
				teeOutputStream.setForwardToRealStream(false);
			}

			return container.getTeeOutputStream();
		}
		return os;
	}

	private static boolean isConnectionWatched(Socket source) {
		return RemoteServerChecker.isConnectionWatched(source.getInetAddress().getHostName(),
				source.getPort());
	}


	private static boolean isConnectionWatched(SocketChannel socketChannel) throws IOException {
		InetSocketAddress remoteAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
		String hostName = remoteAddress.getHostName();
		return RemoteServerChecker.isConnectionWatched(hostName,
				remoteAddress.getPort());
	}

	public static synchronized void socketClosed(Object source) throws UnsupportedEncodingException {
		if (!filterSmockerBehavior()) {
			if (smockerContainerBySocket.get(source) != null) {
				smockerContainerBySocket.get(source).postCommunication();
			}
			smockerContainerBySocket.remove(source);
		}
	}


	private static SmockerContainer addSmockerContainer(Object source, String ip, String host, int port) {
		String stackTrace = getStackTrace();
		SmockerContainer smockerContainer = new SmockerContainer(ip, host, port, stackTrace, source);
		smockerContainerBySocket.put(source, smockerContainer);
		smockerContainer.resetAll();
		return smockerContainer;
	}

	private static String getStackTrace() {
		StringBuilder sb = new StringBuilder();
		String[] stackTraceAsArray = getStackTraceAsArray();
		for (int i = 0; i < stackTraceAsArray.length; i++) {
			sb.append(stackTraceAsArray[i]);
			if (i < stackTraceAsArray.length - 1) {
				sb.append(System.getProperty("line.separator"));
			}
		}
		return sb.toString();
	}

	public static String[] getStackTraceAsArray() {
		List<String> arrRet = new ArrayList<>();
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			arrRet.add(ste.toString());
		}
		String[] ret = new String[arrRet.size()];
		ret = arrRet.toArray(ret);
		return ret;
	}

	public static boolean filterSmockerBehavior() {
		return inSocketFromSSL() 
				|| inStack("com.jenetics.smocker.util.network.RestClientSmocker")
				|| inStack("com.jenetics.smocker.util.network.RestClientAdminChecker")
				|| inStack("com.jenetics.smocker.util.network.SmockerServer$ClientTask")
				|| inStack("com.jenetics.smocker.util.network.RemoteServerChecker");
	}

	protected static boolean inStack(String className) {
		StackTraceElement[] stackTrace = new Throwable().getStackTrace();
		for (int i = 1; i < stackTrace.length; i++) {
			if (stackTrace[i].getClassName().equals(className)) {
				return true;
			}
		}
		return false;
	}

	protected static boolean inSocketFromSSL() {
		StackTraceElement[] stackTrace = new Throwable().getStackTrace();
		// we are in socket coming from SSLSocket socket
		boolean socketTrace = false;
		for (int i = 1; i < stackTrace.length; i++) {
			if (!socketTrace) {
				socketTrace = stackTrace[i].getClassName().equals("java.net.Socket");
			}
			if (!socketTrace && stackTrace[i].getClassName().equals("sun.security.ssl.SSLSocketImpl")) {
				return false;
			} else if (socketTrace && stackTrace[i].getClassName().equals("sun.security.ssl.SSLSocketImpl")) {
				return true;
			}
		}
		return false;
	}
}