package peer.app;

import common.models.Message;
import common.utils.JSONUtils;
import common.utils.MD5Hash;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PeerApp {
	public static final int TIMEOUT_MILLIS = 500;

	private static String peerIP;
	private static int peerPort;
	private static String sharedFolderPath;
	private static P2TConnectionThread p2tConnectionThread;
	private static P2PListenerThread p2pListenerThread;

	private static final Map<String, List<String>> sentFiles = new ConcurrentHashMap<>();
	private static final Map<String, List<String>> receivedFiles = new ConcurrentHashMap<>();
	private static final List<TorrentP2PThread> torrentThreads = Collections.synchronizedList(new ArrayList<>());

	private static boolean exitFlag = false;

	public static boolean isEnded() {
		return exitFlag;
	}

	public static void initFromArgs(String[] args) throws Exception {
		String[] selfAddr = args[0].split(":");
		peerIP = selfAddr[0];
		peerPort = Integer.parseInt(selfAddr[1]);

		String[] trackerAddr = args[1].split(":");
		String trackerIP = trackerAddr[0];
		int trackerPort = Integer.parseInt(trackerAddr[1]);

		sharedFolderPath = args[2];

		Socket trackerSocket = new Socket(trackerIP, trackerPort);
		p2tConnectionThread = new P2TConnectionThread(trackerSocket);

		p2pListenerThread = new P2PListenerThread(peerPort);
	}

	public static void endAll() {
		exitFlag = true;

		if (p2tConnectionThread != null) {
			p2tConnectionThread.end();
		}

		synchronized (torrentThreads) {
			for (TorrentP2PThread thread : torrentThreads) {
				thread.end();
			}
			torrentThreads.clear();
		}

		sentFiles.clear();
		receivedFiles.clear();
	}

	public static void connectTracker() {
		if (p2tConnectionThread != null && !p2tConnectionThread.isAlive()) {
			p2tConnectionThread.start();
		}
	}

	public static void startListening() {
		if (p2pListenerThread != null && !p2pListenerThread.isAlive()) {
			p2pListenerThread.start();
		}
	}

	public static void removeTorrentP2PThread(TorrentP2PThread torrentP2PThread) {
		if (torrentP2PThread != null) {
			torrentThreads.remove(torrentP2PThread);
		}
	}

	public static void addTorrentP2PThread(TorrentP2PThread torrentP2PThread) {
		if (torrentP2PThread != null && !torrentThreads.contains(torrentP2PThread)) {
			torrentThreads.add(torrentP2PThread);
		}
	}

	public static String getSharedFolderPath() {
		return sharedFolderPath;
	}

	public static void addSentFile(String receiver, String fileNameAndHash) {
		sentFiles.computeIfAbsent(receiver, k -> Collections.synchronizedList(new ArrayList<>()))
				.add(fileNameAndHash);
	}

	public static void addReceivedFile(String sender, String fileNameAndHash) {
		receivedFiles.computeIfAbsent(sender, k -> Collections.synchronizedList(new ArrayList<>()))
				.add(fileNameAndHash);
	}

	public static String getPeerIP() {
		return peerIP;
	}

	public static int getPeerPort() {
		return peerPort;
	}

	public static Map<String, List<String>> getSentFiles() {
		Map<String, List<String>> copy = new HashMap<>();
		for (Map.Entry<String, List<String>> entry : sentFiles.entrySet()) {
			copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
		}
		return copy;
	}

	public static Map<String, List<String>> getReceivedFiles() {
		Map<String, List<String>> copy = new HashMap<>();
		for (Map.Entry<String, List<String>> entry : receivedFiles.entrySet()) {
			copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
		}
		return copy;
	}

	public static P2TConnectionThread getP2TConnection() {
		return p2tConnectionThread;
	}

	public static void requestDownload(String ip, int port, String filename, String md5) throws Exception {
		File file = new File(sharedFolderPath, filename);
		if (file.exists()) {
			throw new Exception("File already exists");
		}

		HashMap<String, Object> body = new HashMap<>();
		body.put("name", filename);
		body.put("md5", md5);
		body.put("receiver_ip", peerIP);
		body.put("receiver_port", peerPort);

		Message request = new Message(body, Message.Type.download_request);

		Socket socket = new Socket(ip, port);
		socket.setSoTimeout(TIMEOUT_MILLIS);

		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		DataInputStream dis = new DataInputStream(socket.getInputStream());

		dos.writeUTF(JSONUtils.toJson(request));

		socket.setSoTimeout(0);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[8192];
		int bytesRead;

		try {
			while ((bytesRead = dis.read(buffer)) != -1) {
				baos.write(buffer, 0, bytesRead);
			}
		} catch (Exception e) {
		}

		byte[] fileData = baos.toByteArray();
		socket.close();

		File tempFile = new File(sharedFolderPath, filename + ".tmp");
		try (FileOutputStream fos = new FileOutputStream(tempFile)) {
			fos.write(fileData);
		}

		String receivedHash = MD5Hash.HashFile(tempFile.getAbsolutePath());
		if (receivedHash == null || !receivedHash.equals(md5)) {
			tempFile.delete();
			throw new Exception("File corruption detected");
		}

		tempFile.renameTo(file);

		addReceivedFile(ip + ":" + port, filename + " " + md5);
	}
}