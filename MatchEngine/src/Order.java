class Order {
    enum Type { LIMIT, MARKET }
    enum Side { BUY, SELL }
    enum PegType { NONE, BID, OFFER } 

    // Identificador e contador de ordem de entrada na fila
    private static int idCounter = 1;
    private static long timeCounter = 1;

    int id;
    Type type;
    Side side;
    int price;
    int quantity;
    long timestamp;
    PegType pegType; 

    // Construtor para casos de ordens de limite e mercado sem peg
    public Order(Type type, Side side, int price, int quantity) {
        this.id = idCounter++;
        this.type = type;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timeCounter++;
        this.pegType = PegType.NONE;
    }

    // Construtor para casos de ordens de limite e mercado com peg
    public Order(Type type, Side side, int quantity, PegType pegType) {
        this.id = idCounter++;
        this.type = type;
        this.side = side;
        this.quantity = quantity;
        this.timestamp = timeCounter++;
        this.pegType = pegType;
        this.price = 0; 
    }

    @Override
    public String toString() {
        String base =  "Order: " + side + " " + quantity + " @ " + price + " (ID: " + id + ")";
        if (pegType != PegType.NONE) {
            base += " [Pegged to " + pegType + "]";
        }
        return base;
    }
}