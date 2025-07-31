package peer.app;

import common.models.ConnectionThread;
import common.models.Message;
import common.utils.JSONUtils;
import peer.controllers.P2TConnectionController;

import java.io.IOException;
import java.net.Socket;

import static peer.app.PeerApp.TIMEOUT_MILLIS;

public class P2TConnectionThread extends ConnectionThread {

	protected P2TConnectionThread(Socket socket) throws IOException {
		super(socket);
	}

	@Override
	public boolean initialHandshake() {
		try {
			socket.setSoTimeout(TIMEOUT_MILLIS);

			String statusCmdStr = dataInputStream.readUTF();
			Message statusCmd = JSONUtils.fromJson(statusCmdStr);
			if (statusCmd.getType() == Message.Type.command &&
					"status".equals(statusCmd.getFromBody("command"))) {
				Message statusResponse = P2TConnectionController.status();
				sendMessage(statusResponse);
			} else {
				return false;
			}

			String filesCmdStr = dataInputStream.readUTF();
			Message filesCmd = JSONUtils.fromJson(filesCmdStr);
			if (filesCmd.getType() == Message.Type.command &&
					"get_files_list".equals(filesCmd.getFromBody("command"))) {
				Message filesResponse = P2TConnectionController.getFilesList();
				sendMessage(filesResponse);
			} else {
				return false;
			}

			socket.setSoTimeout(0);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	protected boolean handleMessage(Message message) {
		if (message.getType() == Message.Type.command) {
			sendMessage(P2TConnectionController.handleCommand(message));
			return true;
		}
		return false;
	}

	@Override
	public void run() {
		super.run();
		PeerApp.endAll();
		System.exit(0);
	}
}