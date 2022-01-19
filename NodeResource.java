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

    public NodeResource(String[] args, String operation) throws IllegalArgumentException {
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

        try {
            String[] resourceArray = remaining.split("[:,]");
            for (int i = 0; i < resourceArray.length; i++) {
                switch (resourceArray[i]) {
                    case "A":
                        try {
                            a = Integer.parseInt(resourceArray[++i]);
                        } catch (NumberFormatException e) {
                            a = 0;
                        }
                        break;
                    case "B":
                        try {
                            b = Integer.parseInt(resourceArray[++i]);
                        } catch (NumberFormatException e) {
                            b = 0;
                        }
                        break;
                    case "C":
                        try {
                            c = Integer.parseInt(resourceArray[++i]);
                        } catch (NumberFormatException e) {
                            c = 0;
                        }
                        break;
                }
            }
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("niepoprawna ilosc argumentow. uzycie: NetworkNode -ident <identyfikator:liczba> -tcpport <port:liczba> <zasob:liczba> [<zasob:liczba>] ");
        }

        isGateway = (gatewayHostAddr == null && "adds".equals(operation));
        if (id == null) {
            throw new IllegalArgumentException("ID IS NULL! PROVIDE -ident <INTEGER> flag to the request");
        }
    }

    public NodeResource(String arg, String operation) throws IllegalArgumentException {
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

    public Integer getA() {
        return a;
    }

    public Integer getB() {
        return b;
    }

    public Integer getC() {
        return c;
    }
}