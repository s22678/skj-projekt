import java.io.*;
import java.net.Socket;

public class NonGatewayHandler implements Runnable {

    private Socket socket = null;
    private BufferedReader bufferedReader = null;
    private BufferedWriter bufferedWriter = null;

    public NonGatewayHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        while(socket.isConnected()) {
            String msgFromServer;
            try {
                msgFromServer = bufferedReader.readLine();
                System.out.println(msgFromServer);
                System.out.println("NOT IMPLEMENTED!!");
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
