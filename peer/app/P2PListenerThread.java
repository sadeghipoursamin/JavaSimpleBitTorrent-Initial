package peer.app;

import common.models.Message;
import common.utils.JSONUtils;
import common.utils.MD5Hash;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static peer.app.PeerApp.TIMEOUT_MILLIS;

public class P2PListenerThread extends Thread {
    private final ServerSocket serverSocket;

    public P2PListenerThread(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    private void handleConnection(Socket socket) throws Exception {
        DataInputStream dataInputStream;
        DataOutputStream dataOutputStream;
        try {
            socket.setSoTimeout(TIMEOUT_MILLIS);

            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            String messageStr = dataInputStream.readUTF();

            Message message = JSONUtils.fromJson(messageStr);

            if (message.getType() == Message.Type.download_request) {
                String fileName = message.getFromBody("name");
                String md5 = message.getFromBody("md5");
                String receiverIp = message.getFromBody("receiver_ip");
                int receiverPort = (int) ((double) ((Double) message.getFromBody("receiver_port")));

                File file = new File(fileName);
                if (!file.exists()) {
                    socket.close();
                    return;
                }

                if (md5 != null && !md5.trim().isEmpty()) {
                    String fileHash = MD5Hash.HashFile(file.getAbsolutePath());
                    if (fileHash == null || !fileHash.equals(md5)) {
                        socket.close();
                        return;
                    }
                }

                socket.setSoTimeout(0);


            }
        }
    }

    @Override
    public void run() {
        while (!PeerApp.isEnded()) {
            try {
                Socket socket = serverSocket.accept();
                handleConnection(socket);
            } catch (Exception e) {
                break;
            }
        }

        try {
            serverSocket.close();
        } catch (Exception ignored) {
        }
    }
}
