package tracker.controllers;

import tracker.app.PeerConnectionThread;
import tracker.app.TrackerApp;

import java.util.*;

public class TrackerCLIController {
	public static String processCommand(String command) {
		for (TrackerCommands cmd : TrackerCommands.values()) {
			if (cmd.matches(command)) {
				switch (cmd) {
					case END:
						return endProgram();
					case REFRESH_FILES:
						return refreshFiles();
					case RESET_CONNECTIONS:
						return resetConnections();
					case LIST_PEERS:
						return listPeers();
					case LIST_FILES:
						return listFiles(command);
					case GET_RECEIVES:
						return getReceives(command);
					case GET_SENDS:
						return getSends(command);
					default:
						break;
				}
			}
		}
		return TrackerCommands.invalidCommand;
	}

	private static String getReceives(String command) {
		String ipPort = command.substring("get_receives ".length()).trim();
		String[] parts = ipPort.split(":");
		if (parts.length != 2) {
			return "Invalid format";
		}

		String ip = parts[0];
		int port;
		try {
			port = Integer.parseInt(parts[1]);
		} catch (NumberFormatException e) {
			return "Invalid port";
		}

		PeerConnectionThread connection = TrackerApp.getConnectionByIpPort(ip, port);
		if (connection == null) {
			return "Peer not found.";
		}

		Map<String, List<String>> receivedFiles = TrackerConnectionController.getReceives(connection);

		if (receivedFiles.isEmpty()) {
			return "No files received by " + ip + ":" + port;
		}

		List<String> output = new ArrayList<>();
		for (Map.Entry<String, List<String>> entry : receivedFiles.entrySet()) {
			String sender = entry.getKey();
			List<String> files = new ArrayList<>(entry.getValue());
			Collections.sort(files);

			for (String fileInfo : files) {
				output.add(fileInfo + " - " + sender);
			}
		}

		Collections.sort(output);
		return String.join("\n", output);
	}

	private static String getSends(String command) {
		String ipPort = command.substring("get_sends ".length()).trim();
		String[] parts = ipPort.split(":");
		if (parts.length != 2) {
			return "Invalid format";
		}

		String ip = parts[0];
		int port;
		try {
			port = Integer.parseInt(parts[1]);
		} catch (NumberFormatException e) {
			return "Invalid port";
		}

		PeerConnectionThread connection = TrackerApp.getConnectionByIpPort(ip, port);
		if (connection == null) {
			return "Peer not found.";
		}

		Map<String, List<String>> sentFiles = TrackerConnectionController.getSends(connection);

		if (sentFiles.isEmpty()) {
			return "No files sent by " + ip + ":" + port;
		}

		// Sort and format output
		List<String> output = new ArrayList<>();
		for (Map.Entry<String, List<String>> entry : sentFiles.entrySet()) {
			String receiver = entry.getKey();
			List<String> files = new ArrayList<>(entry.getValue());
			Collections.sort(files);

			for (String fileInfo : files) {
				output.add(fileInfo + " - " + receiver);
			}
		}

		Collections.sort(output);
		return String.join("\n", output);
	}

	private static String listFiles(String command) {
		String ipPort = command.substring("list_files ".length()).trim();
		String[] parts = ipPort.split(":");
		if (parts.length != 2) {
			return "Invalid format";
		}

		String ip = parts[0];
		int port;
		try {
			port = Integer.parseInt(parts[1]);
		} catch (NumberFormatException e) {
			return "Invalid port";
		}

		PeerConnectionThread connection = TrackerApp.getConnectionByIpPort(ip, port);
		if (connection == null) {
			return "Peer not found.";
		}

		Map<String, String> files = connection.getFileAndHashes();
		if (files.isEmpty()) {
			return "Repository is empty.";
		}

		// Sort by filename
		TreeMap<String, String> sortedFiles = new TreeMap<>(files);
		StringBuilder result = new StringBuilder();
		for (Map.Entry<String, String> entry : sortedFiles.entrySet()) {
			if (!result.isEmpty()) {
				result.append("\n");
			}
			result.append(entry.getKey()).append(" ").append(entry.getValue());
		}

		return result.toString();
	}

	private static String listPeers() {
		List<PeerConnectionThread> connections = TrackerApp.getConnections();

		if (connections.isEmpty()) {
			return "No peers connected.";
		}

		StringBuilder result = new StringBuilder();
		for (PeerConnectionThread connection : connections) {
			if (!result.isEmpty()) {
				result.append("\n");
			}
			result.append(connection.getOtherSideIP())
					.append(":")
					.append(connection.getOtherSidePort());
		}

		return result.toString();
	}

	private static String resetConnections() {
		try {
			List<PeerConnectionThread> connections = TrackerApp.getConnections();
			for (PeerConnectionThread connection : connections) {
				try {
					if (connection.isSocketConnected()) {
						connection.refreshStatus();
						connection.refreshFileList();
					} else {
						TrackerApp.removePeerConnection(connection);
					}
				}catch (Exception e) {
					TrackerApp.removePeerConnection(connection);
				}
			}
			return "";
		} catch (Exception e) {
			return "Error resetting connections: "+ e.getMessage();
		}
	}

	private static String refreshFiles() {
		List<PeerConnectionThread> connections = TrackerApp.getConnections();

		for (PeerConnectionThread connection : connections) {
			try {
				connection.refreshFileList();
			} catch (Exception e) {
			}
		}

		return "";
	}

	private static String endProgram() {
		TrackerApp.endAll();
		return "";
	}
}