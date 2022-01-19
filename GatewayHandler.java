import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GatewayHandler implements Runnable {

    public static ConcurrentHashMap<String, NodeResource> map = new ConcurrentHashMap<>();
    public static ConcurrentLinkedQueue<GatewayHandler> queue = new ConcurrentLinkedQueue<>();

    private Socket socket = null;
    private BufferedReader bufferedReader = null;
    private BufferedWriter bufferedWriter = null;
    private NodeResource resource = null;

    // ID Node gateway'a
    private String nodeID = null;
    private String connectedNodeID = null;

    public GatewayHandler(Socket socket, NodeResource resource) {
        try {
            this.socket = socket;
            this.resource =  resource;
            nodeID = resource.getId();
            addResourceToMap();
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connectedNodeID = bufferedReader.readLine();
            queue.add(this);
            broadcastMessage("GW-SERVER: " + connectedNodeID + " polaczyl sie do sieci");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String messageFromNode;
        while(socket.isConnected()) {
            try {
                messageFromNode = bufferedReader.readLine();
                if ("QUERY".equals(messageFromNode)) {
                    for(NodeResource value : map.values()){
                        writeMessage(value.toString());
                    }
//                    writeMessage(queryResources());
                } else if ("ADD".equals(messageFromNode)) {
                    messageFromNode = bufferedReader.readLine();
                    addResources(messageFromNode);
                    System.out.println("GW-SERVER: zasoby dodane " + messageFromNode);
                } else if ("RESERVE".equals(messageFromNode)) {
                    messageFromNode = bufferedReader.readLine();
                    reserveResources(messageFromNode);
                    System.out.println("GW-SERVER: zasoby zarezerwowane " + messageFromNode);
                } else if ("TERMINATE".equals(messageFromNode)) {
                    broadcastMessage(messageFromNode);
                    socket.close();
                } else {
                    String response = "GW-SERVER: polecenie od klienta " + messageFromNode;
                    reserveResources(messageFromNode);
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter, "wystapil blad odczytu wiadomosci od klienta w metodzie 'run'");
                break;
            }
        }
        System.out.println("Zamykam polaczenie");
    }

    // TODO
    public String queryResources() {
        return "A:5,B:3,C:4";
//        map.values().forEach(v -> System.out.println(("value: " + v)));
    }

    // TODO
    public void addResources(String msg) {
        // dodaj zasoby do mapy
    }

    // TODO
    public void reserveResources(String msg) {
        // usun zasoby z mapy
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter, String closingMessage) {
        System.out.print("GW-SERVER: " + closingMessage + " ZAMYKAM POLACZENIE OD: ");
        if (connectedNodeID == null) {
            System.out.println(Thread.currentThread());
        } else {
            System.out.println(connectedNodeID);
        }
        removeResourceFromMap();
        removeChatClientHandler();
        try {
            if(bufferedReader != null) {
                bufferedReader.close();
            }
            if(bufferedWriter != null) {
                bufferedWriter.close();
            }
            if(socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeResourceFromMap() {
        if (map.containsKey(nodeID)) {
            map.remove(nodeID);
        }
    }

    public void removeChatClientHandler() {
        queue.remove(this);
        broadcastMessage("GW-SERVER: " + connectedNodeID + " opuscil siec");
    }

    public void addResourceToMap() {
        try {
            map.put(nodeID, resource);
        } catch (NullPointerException e) {
            closeEverything(socket, bufferedReader, bufferedWriter, "nie mozna dodac klucza (serwer-gw ID) null do mapy");
        }
    }

    public void addResourceToMap(String connectedNodeID, NodeResource nodeResource) {
        try {
            map.put(connectedNodeID, nodeResource);
        } catch (NullPointerException e) {
            closeEverything(socket, bufferedReader, bufferedWriter, "nie mozna dodac klucza (connected node ID) null do mapy");
        }
    }

    // TODO Usunac komentarze z IF'a?
    public void broadcastMessage(String val) {
        for(GatewayHandler gatewayHandler : queue) {
            try {
//                if (!gatewayHandler.connectedNodeID.equals(connectedNodeID)) {
                    gatewayHandler.bufferedWriter.write(val);
                    gatewayHandler.bufferedWriter.newLine();
                    gatewayHandler.bufferedWriter.flush();
//                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter, "nie mozna wyslac broacastu do: " + gatewayHandler.connectedNodeID);
            }
        }
    }

    public void writeMessage(String msg) throws IOException {
        bufferedWriter.write(msg);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }
}
