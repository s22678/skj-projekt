import javax.xml.soap.Node;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkNode {

    private ServerSocket serverSocket;
    private Runnable handler;
    private NodeResource r;

    public NetworkNode(ServerSocket serverSocket, NodeResource r) {
        this.serverSocket = serverSocket;
        this.r = r;
    }

    public void startServer(boolean isGateway) {
        try {
            while(!serverSocket.isClosed()) {
                System.out.println(serverSocket.getLocalPort());
                // 1,5 sekundowy timeout
                //serverSocket.setSoTimeout(1500);
                Socket socket = serverSocket.accept();
                System.out.println("nowy node/klient polaczony");
                if(isGateway) {
                    handler = new GatewayHandler(socket, r);
                } else {
                    handler = new NonGatewayHandler(socket);
                }
                Thread thread = new Thread(handler);
                thread.start();
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        try {
            NodeResource r = new NodeResource(args, "adds");
            ServerSocket serversocket = new ServerSocket(r.getNodePort());
            NetworkNode server = new NetworkNode(serversocket, r);
            System.out.println("DEBUG:");
            server.startServer(r.isGateway());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}