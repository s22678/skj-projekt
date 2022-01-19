import java.util.Collections;
import java.util.Hashtable;

public class NodeResource {
    private Integer a = 0;
    private Integer b = 0;
    private Integer c = 0;

    String operation = null;

    private boolean isGateway = false;
    private int nodeIP = 0;
    private String id = null;
    int gatewayPort = 0;
    int nodePort = 0;
    String gatewayHostAddr = null;

    @Override
    public String toString() {
        return "NodeResource{" +
                "a=" + a +
                ", b=" + b +
                ", c=" + c +
                ", operation='" + operation + '\'' +
                ", isGateway=" + isGateway +
                ", nodeIP=" + nodeIP +
                ", id='" + id + '\'' +
                ", gatewayPort=" + gatewayPort +
                ", nodePort=" + nodePort +
                ", gatewayHostAddr='" + gatewayHostAddr + '\'' +
                '}';
    }

    public NodeResource(String[] args, String operation) {
        this.operation = operation;
        String remaining = null;

        for (int i=0; i<args.length; i++) {
            switch (args[i]) {
                case "NetworkNode":
                    break;
                case "-ident":
                    id = args[++i];
                    break;
                case "-gateway":
                    String[] gatewayArray = args[++i].split(":");
                    gatewayHostAddr = gatewayArray[0];
                    gatewayPort = Integer.parseInt(gatewayArray[1]);
                    break;
                case "-tcpport":
                    nodePort = Integer.parseInt(args[++i]);
                    break;
                default:
                    if(remaining == null) remaining = args[i];
                    else remaining += "," + args[i];
            }
        }

        String[] resourceArray = remaining.split("[:,]");
        for (int i = 0; i < resourceArray.length; i++) {
            switch (resourceArray[i]) {
                case "A":
                    a = Integer.parseInt(resourceArray[++i]);
                    break;
                case "B":
                    b = Integer.parseInt(resourceArray[++i]);
                    break;
                case "C":
                    c = Integer.parseInt(resourceArray[++i]);
                    break;
            }
        }

        isGateway = (gatewayHostAddr == null && "adds".equals(operation));
    }

    public NodeResource(String arg, String operation) {
        this(arg.split(" "), operation);
    }

    public boolean isGateway() {
        return isGateway;
    }

    public int getIP() {
        return nodeIP;
    }

    public String getOperation() {
        return operation;
    }

    public String getId() {
        return id;
    }

    public int getNodePort() {
        return nodePort;
    }

    public Hashtable<String, Integer> getAddedRecources() {
        if ("adds".equals(operation)) {
            Hashtable<String, Integer> tempHash = new Hashtable<String, Integer>();
            tempHash.put("A", a);
            tempHash.put("B", b);
            tempHash.put("C", c);

            return tempHash;
        } else {
            return (Hashtable<String, Integer>) Collections.<String, Integer>emptyMap();
        }
    }
}