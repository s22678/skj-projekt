import javax.xml.soap.Node;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GatewayHandler implements Runnable {

    private final static String ADDS = "adds";
    private final static String RESERVES = "reserves";

    public static ConcurrentHashMap<String, NodeResource> map = new ConcurrentHashMap<>();
    public static ConcurrentLinkedQueue<GatewayHandler> queue = new ConcurrentLinkedQueue<>();

    private Socket socket = null;
    private BufferedReader bufferedReader = null;
    private BufferedWriter bufferedWriter = null;
    private NodeResource resource = null;

    // ID Node gateway'a
    private String nodeID = null;
    private String connectedClientID = null;

    public GatewayHandler(Socket socket, NodeResource resource) {
        try {
            this.socket = socket;
            this.resource =  resource;
            nodeID = resource.getId();
            addResourceToMap();
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connectedClientID = socket.getInetAddress().getHostName() + ":" + socket.getPort();
            queue.add(this);
            broadcastMessage("GW-SERVER: " +  connectedClientID + " polaczyl sie do sieci");
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
                    writeMessage(queryResources());
                } else if ("ADD".equals(messageFromNode)) {
                    messageFromNode = bufferedReader.readLine();
                    addNodeResource(messageFromNode, GatewayHandler.ADDS);
                    System.out.println("GW-SERVER: zasoby dodane " + messageFromNode);
                } else if ("RESERVE".equals(messageFromNode)) {
                    messageFromNode = bufferedReader.readLine();
                    addNodeResource(messageFromNode, GatewayHandler.RESERVES);
                    System.out.println("GW-SERVER: zasoby zarezerwowane " + messageFromNode);
                } else if ("TERMINATE".equals(messageFromNode)) {
                    broadcastMessage(messageFromNode);
                    socket.close();
                } else if(socket.getInputStream().read() == -1) {
                    System.out.println("klient/node odlaczony, zamykam socket...");
                    socket.close();
                } else {
                    System.out.println("GW-SERVER: wiadomosc od klienta " + messageFromNode);
                    writeMessage("nie rozpoznano polecenia " + messageFromNode);
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter, "wystapil blad odczytu wiadomosci od klienta w metodzie 'run'");
                break;
            } catch (NullPointerException e) {
                System.out.println("GW-SERVER: POWAZNY BLAD");
                e.printStackTrace();
                closeEverything(socket, bufferedReader, bufferedWriter, "wystapil blad odczytu wiadomosci od klienta w metodzie 'run'");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                closeEverything(socket, bufferedReader, bufferedWriter, "wystapil blad odczytu wiadomosci od klienta w metodzie 'run'");
            }
        }
        System.out.println("Zamykam polaczenie");
    }

    // TODO
    public String queryResources() {
        Integer totalA = 0;
        Integer totalB = 0;
        Integer totalC = 0;
        for(NodeResource nodeResource : map.values()){
            if(nodeResource.getOperation().equals("adds")) {
                totalA += nodeResource.getA();
                totalB += nodeResource.getB();
                totalC += nodeResource.getC();
            } else {
                totalA -= nodeResource.getA();
                totalB -= nodeResource.getB();
                totalC -= nodeResource.getC();
            }
        }
        return "A:" + totalA + " B:" + totalB + " C:" + totalC;
    }

//    // TODO
//    public void addResources(String msg) {
//        System.out.println("GW-SERVER: metoda addResources()");
//        NodeResource tempNodeResource = createResource(msg, "adds");
//        connectedClientID = tempNodeResource.getId();
//        addResourceToMap(connectedClientID, tempNodeResource);
//    }
//
//    // TODO
//    public void reserveResources(String msg) {
//        System.out.println("GW-SERVER: metoda reserveResources()");
//            NodeResource tempNodeResource = createResource(msg, "removes");
//            connectedClientID = tempNodeResource.getId();
//            addResourceToMap(connectedClientID, tempNodeResource);
//    }
//
//    public NodeResource createResource(String msg, String ops) {
//        NodeResource r = null;
//        try{
//            r = new NodeResource(msg, ops);
//        } catch (InstantiationException e) {
//            closeEverything(socket, bufferedReader, bufferedWriter, e.getMessage());
//        }
//
//        return r;
//    }

    public void addNodeResource(String messageFromClient, String operations) throws IllegalArgumentException {
        System.out.println("GW-SERVER: metoda addNodeResources()");
        NodeResource tempNodeResource = new NodeResource(messageFromClient, operations);
        addResourceToMap(connectedClientID, tempNodeResource);
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter, String closingMessage) {
        System.out.print("GW-SERVER: " + closingMessage + " ZAMYKAM POLACZENIE OD: ");
        if (connectedClientID == null) {
            System.out.println(Thread.currentThread());
        } else {
            System.out.println(connectedClientID);
        }
        removeResourceFromMap();
        removeClientHandler();
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
        if (map.containsKey(connectedClientID)) {
            map.remove(connectedClientID);
        }
    }

    public void removeClientHandler() {
        queue.remove(this);
        broadcastMessage("GW-SERVER: " + nodeID + " opuscil siec");
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
                closeEverything(socket, bufferedReader, bufferedWriter, "nie mozna wyslac broacastu do: " + gatewayHandler.nodeID);
            }
        }
    }

    public void writeMessage(String msg) throws IOException {
        bufferedWriter.write(msg);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }
}
