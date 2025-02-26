class Order {
    enum Type { LIMIT, MARKET }
    enum Side { BUY, SELL }

    private static int idCounter = 1;
    private static long timeCounter = 1;

    int id;
    Type type;
    Side side;
    int price;
    int quantity;
    long timestamp;

    public Order(Type type, Side side, int price, int quantity) {
        this.id = idCounter++;
        this.type = type;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timeCounter++;
    }

    @Override
    public String toString() {
        return "Order: " + side + " " + quantity + " @ " + price + " (ID: " + id + ")";
    }
}