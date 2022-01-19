public class NodeResource {
    private int a;
    private int b;
    private int c;

    String operation = null;

    private boolean isGateway;
    private int ip;
    private String id;
    int gw_port;
    int port;

    public NodeResource(String[] args, String operation) {
        String gateway = null;
        String remaining = null;
        for (int i=0; i<args.length; i++) {
            switch (args[i]) {
                case "-ident":
                    id = args[++i];
                    break;
                case "-gateway":
                    String[] gatewayArray = args[++i].split(":");
                    gateway = gatewayArray[0];
                    gw_port = Integer.parseInt(gatewayArray[1]);
                    break;
                case "-tcpport":
                    port = Integer.parseInt(args[++i]);
                default:
                    if(remaining == null) remaining = args[i];
                    else remaining += " " + args[i];
            }
        }
        String[] resourceArray = remaining.split(":");
        for (int i = 0; i < resourceArray.length; i++) {
            switch (resourceArray[i]) {
                case "A":
                    a = Integer.parseInt(args[++i]);
                case "B":
                    b = Integer.parseInt(args[++i]);
                case "C":
                    c = Integer.parseInt(args[++i]);
            }
        }
        isGateway = gateway == null;
    }

    public NodeResource(String arg, String operation) {
        this(arg.split(" "), operation);
    }

    public boolean isGateway() {
        return isGateway;
    }

    public int getIP() {
        return ip;
    }

    public String getOperation() {
        return operation;
    }

    public String getId() {
        return id;
    }
}