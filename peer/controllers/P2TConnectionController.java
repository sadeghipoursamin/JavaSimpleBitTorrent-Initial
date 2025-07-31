package peer.controllers;

import common.models.Message;
import common.utils.FileUtils;
import common.utils.MD5Hash;
import peer.app.P2TConnectionThread;
import peer.app.PeerApp;

import java.io.File;
import java.util.*;

public class P2TConnectionController {
	public static Message handleCommand(Message message) {
		String command = message.getFromBody("command");

		switch (command) {
			case "status":
				return status();
			case "get_files_list":
				return getFilesList();
			case "get_sends":
				return getSends();
			case "get_receives":
				return getReceives();
			default:
				return null;
		}
	}

	private static Message getReceives() {
		HashMap<String, Object> body = new HashMap<>();
		body.put("response", "ok");
		body.put("command", "get_receives");
		body.put("received_files", PeerApp.getReceivedFiles());

		return new Message(body, Message.Type.response);
	}

	private static Message getSends() {
		HashMap<String, Object> body = new HashMap<>();
		body.put("command", "get_sends");
		body.put("response", "ok");
		body.put("sent_files", PeerApp.getSentFiles());

		return new Message(body, Message.Type.response);
	}

	public static Message getFilesList() {
		HashMap<String, Object> body = new HashMap<>();
		body.put("command", "get_files_list");
		body.put("response", "ok");

		Map<String, String> files = new HashMap<>();
		File folder = new File(PeerApp.getSharedFolderPath());

		if (folder.exists() && folder.isDirectory()) {
			File[] fileList = folder.listFiles();
			if (fileList != null) {
				for (File file : fileList) {
					if (file.isFile()) {
						String hash = MD5Hash.HashFile(file.getAbsolutePath());
						if (hash != null) {
							files.put(file.getName(), hash);
						}
					}
				}
			}
		}

		body.put("files", files);
		return new Message(body, Message.Type.response);
	}

	public static Message status() {
		HashMap<String, Object> body = new HashMap<>();
		body.put("command", "status");
		body.put("response", "ok");
		body.put("peer", PeerApp.getPeerIP());
		body.put("listen_port", PeerApp.getPeerPort());

		return new Message(body, Message.Type.response);
	}

	public static Message sendFileRequest(P2TConnectionThread tracker, String fileName) throws Exception {
		HashMap<String, Object> body = new HashMap<>();
		body.put("name", fileName);

		Message request = new Message(body, Message.Type.file_request);

		Message response = tracker.sendAndWaitForResponse(request, PeerApp.TIMEOUT_MILLIS);

		if (response == null) {
			throw new Exception("Request timed out");
		}

		String responseType = response.getFromBody("response");
		if ("error".equals(responseType)) {
			String error = response.getFromBody("error");
			if ("not_found".equals(error)) {
				throw new Exception("No peer has the file!");
			} else if ("multiple_hash".equals(error)) {
				throw new Exception("Multiple hashes found!");
			}
		}

		return response;
	}
}