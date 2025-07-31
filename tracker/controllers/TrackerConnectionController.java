package tracker.controllers;

import common.models.Message;
import tracker.app.PeerConnectionThread;
import tracker.app.TrackerApp;

import java.util.*;

public class TrackerConnectionController {
	public static Message handleCommand(Message message) {
		String fileName = message.getFromBody("name");

		Map<PeerConnectionThread, String> peersWithFile = new HashMap<>();
		List<PeerConnectionThread> connections = TrackerApp.getConnections();

		for (PeerConnectionThread connection : connections) {
			Map<String, String> files = connection.getFileAndHashes();
			if (files.containsKey(fileName)) {
				peersWithFile.put(connection, files.get(fileName));
			}
		}

		HashMap<String, Object> body = new HashMap<>();

		if (peersWithFile.isEmpty()) {
			body.put("response", "error");
			body.put("error", "not_found");
			return new Message(body, Message.Type.response);
		}

		Set<String> uniqueHashes = new HashSet<>(peersWithFile.values());
		if (uniqueHashes.size() > 1) {
			body.put("response", "error");
			body.put("error", "multiple_hash");
			return new Message(body, Message.Type.response);
		}

		List<PeerConnectionThread> peerList = new ArrayList<>(peersWithFile.keySet());
		PeerConnectionThread selectedPeer = peerList.get(new Random().nextInt(peerList.size()));

		body.put("response", "peer_found");
		body.put("md5", peersWithFile.get(selectedPeer));
		body.put("peer_have", selectedPeer.getOtherSideIP());
		body.put("peer_port", selectedPeer.getOtherSidePort());

		return new Message(body, Message.Type.response);
	}

	public static Map<String, List<String>> getSends(PeerConnectionThread connection) {
		HashMap<String, Object> body = new HashMap<>();
		body.put("command", "get_sends");
		Message command = new Message(body, Message.Type.command);

		Message response = connection.sendAndWaitForResponse(command, TrackerApp.TIMEOUT_MILLIS);

		if (response == null) {
			return new HashMap<>();
		}

		Map<String, List<String>> sentFiles = response.getFromBody("sent_files");
		return sentFiles != null ? sentFiles : new HashMap<>();
	}

	public static Map<String, List<String>> getReceives(PeerConnectionThread connection) {
		HashMap<String, Object> body = new HashMap<>();
		body.put("command", "get_receives");
		Message command = new Message(body, Message.Type.command);

		Message response = connection.sendAndWaitForResponse(command, TrackerApp.TIMEOUT_MILLIS);

		if (response == null) {
			return new HashMap<>();
		}

		Map<String, List<String>> receivedFiles = response.getFromBody("received_files");
		return receivedFiles != null ? receivedFiles : new HashMap<>();
	}
}