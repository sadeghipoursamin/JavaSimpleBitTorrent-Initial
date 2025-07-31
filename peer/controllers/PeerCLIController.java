package peer.controllers;

import common.models.Message;
import common.utils.MD5Hash;
import peer.app.PeerApp;

import java.io.File;
import java.util.*;

public class PeerCLIController {
	public static String processCommand(String command) {
		for (PeerCommands cmd : PeerCommands.values()) {
			if (cmd.matches(command)) {
				switch (cmd) {
					case END:
						return endProgram();
					case DOWNLOAD:
						return handleDownload(command);
					case LIST:
						return handleListFiles();
					default:
						break;
				}
			}
		}
		return PeerCommands.invalidCommand;
	}

	private static String handleListFiles() {
		File folder = new File(PeerApp.getSharedFolderPath());

		if (!folder.exists() || !folder.isDirectory()) {
			return "Repository is empty.";
		}

		File[] files = folder.listFiles();
		if (files == null || files.length == 0) {
			return "Repository is empty.";
		}

		// Create map of filename -> hash
		Map<String, String> fileMap = new TreeMap<>();
		for (File file : files) {
			if (file.isFile()) {
				String hash = MD5Hash.HashFile(file.getAbsolutePath());
				if (hash != null) {
					fileMap.put(file.getName(), hash);
				}
			}
		}

		if (fileMap.isEmpty()) {
			return "Repository is empty.";
		}

		// Build output string
		StringBuilder result = new StringBuilder();
		for (Map.Entry<String, String> entry : fileMap.entrySet()) {
			if (result.length() > 0) {
				result.append("\n");
			}
			result.append(entry.getKey()).append(" ").append(entry.getValue());
		}

		return result.toString();
	}

	private static String handleDownload(String command) {
		String fileName = command.substring("download ".length()).trim();

		// Check if file already exists
		File file = new File(PeerApp.getSharedFolderPath(), fileName);
		if (file.exists()) {
			return "You already have the file!";
		}

		try {
			// Send file request to tracker
			Message response = P2TConnectionController.sendFileRequest(
					PeerApp.getP2TConnection(), fileName);

			// Get peer info and file hash
			String peerIP = response.getFromBody("peer_have");
			int peerPort = response.getIntFromBody("peer_port");
			String md5 = response.getFromBody("md5");

			// Request file from peer
			PeerApp.requestDownload(peerIP, peerPort, fileName, md5);

			return "File downloaded successfully: " + fileName;

		} catch (Exception e) {
			String msg = e.getMessage();
			if (msg.contains("No peer has the file!")) {
				return "No peer has the file!";
			} else if (msg.contains("Multiple hashes found!")) {
				return "Multiple hashes found!";
			} else if (msg.contains("File corruption detected")) {
				return "The file has been downloaded from peer but is corrupted!";
			} else {
				return "Error downloading file: " + msg;
			}
		}
	}

	public static String endProgram() {
		PeerApp.endAll();
		return "";
	}
}