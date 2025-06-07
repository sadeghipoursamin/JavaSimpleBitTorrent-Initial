package tracker.app;

import java.util.ArrayList;
import java.util.List;

public class TrackerApp {
	public static final int TIMEOUT_MILLIS = 500;
	private static final ArrayList<PeerConnectionThread> connections = new ArrayList<>();
	private static boolean exitFlag = false;
	private static ListenerThread listenerThread;

	public static PeerConnectionThread getConnectionByIpPort(String ip, int port) {
		// TODO: Implement peer connection lookup
		return null;
	}

	public static boolean isEnded() {
		return exitFlag;
	}

	public static void setListenerThread(ListenerThread listenerThread) {
		TrackerApp.listenerThread = listenerThread;
	}

	public static List<PeerConnectionThread> getConnections() {
		return List.copyOf(TrackerApp.connections);
	}

	public static void startListening() {
		if (listenerThread != null && !listenerThread.isAlive()) {
			listenerThread.start();
		} else {
			throw new IllegalStateException("Listener thread is already running or not set.");
		}
	}

	public static void endAll() {
		exitFlag = true;
		for (PeerConnectionThread connection : connections)
			connection.end();
		connections.clear();
	}

	public static void removePeerConnection(PeerConnectionThread peerConnectionThread) {
		if (peerConnectionThread != null) {
			connections.remove(peerConnectionThread);
			peerConnectionThread.end();
		}
	}

	public static void addPeerConnection(PeerConnectionThread peerConnectionThread) {
		if (peerConnectionThread != null && !connections.contains(peerConnectionThread))
			connections.add(peerConnectionThread);
	}
}
