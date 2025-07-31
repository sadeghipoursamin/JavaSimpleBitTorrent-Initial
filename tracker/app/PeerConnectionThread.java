package tracker.app;

import common.models.ConnectionThread;
import common.models.Message;
import tracker.controllers.TrackerConnectionController;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static tracker.app.TrackerApp.TIMEOUT_MILLIS;

public class PeerConnectionThread extends ConnectionThread {
	private HashMap<String, String> fileAndHashes;

	public PeerConnectionThread(Socket socket) throws IOException {
		super(socket);
		this.fileAndHashes = new HashMap<>();
	}

	@Override
	public boolean initialHandshake() {
		try {
			if (!refreshStatus()) {
				return false;
			}

			refreshFileList();

			TrackerApp.addPeerConnection(this);

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean refreshStatus() {
		HashMap<String, Object> body = new HashMap<>();
		body.put("command", "status");
		Message statusCmd = new Message(body, Message.Type.command);

		Message response = sendAndWaitForResponse(statusCmd, TIMEOUT_MILLIS);

		if (response != null && "ok".equals(response.getFromBody("response"))) {
			String peerIP = response.getFromBody("peer");
			int listenPort = response.getIntFromBody("listen_port");

			setOtherSideIP(peerIP);
			setOtherSidePort(listenPort);
			return true;
		}
		return false;
	}

	public void refreshFileList() {
		HashMap<String, Object> body = new HashMap<>();
		body.put("command", "get_files_list");
		Message filesCmd = new Message(body, Message.Type.command);

		Message response = sendAndWaitForResponse(filesCmd, TIMEOUT_MILLIS);

		if (response != null && "ok".equals(response.getFromBody("response"))) {
			Map<String, Object> files = response.getFromBody("files");
			fileAndHashes.clear();
			if (files != null) {
				for (Map.Entry<String, Object> entry : files.entrySet()) {
					fileAndHashes.put(entry.getKey(), entry.getValue().toString());
				}
			}
		}
	}

	@Override
	protected boolean handleMessage(Message message) {
		if (message.getType() == Message.Type.file_request) {
			sendMessage(TrackerConnectionController.handleCommand(message));
			return true;
		}
		return false;
	}

	@Override
	public void run() {
		super.run();
		TrackerApp.removePeerConnection(this);
	}

	public Map<String, String> getFileAndHashes() {
		return Map.copyOf(fileAndHashes);
	}

	public boolean isSocketConnected() {
		return socket != null && socket.isConnected() && !socket.isClosed();
	}
}