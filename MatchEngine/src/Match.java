public class Match {
    public static void main(String[] args) {
        MatchingEngine engine = new MatchingEngine();
        Order order1 = new Order(Order.Type.LIMIT, Order.Side.BUY, 4, 50);
        engine.addOrder(order1);
        Order order2 = new Order(Order.Type.LIMIT, Order.Side.BUY, 3, 100);  // Deve ser executado depois de order1
        engine.addOrder(order2);
        Order order3 = new Order(Order.Type.LIMIT, Order.Side.SELL, 4, 250);
        engine.addOrder(order3);
        Order order4 = new Order(Order.Type.MARKET, Order.Side.BUY, 10, 400);  // Deve ser executado depois de order1
        engine.addOrder(order4);
        engine.viewOrderBook();
    }
}
