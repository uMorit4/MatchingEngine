import java.util.*;

class MatchingEngine {
    private PriorityQueue<Order> buyOrders = new PriorityQueue<>(
        Comparator.<Order>comparingInt(o -> -o.price)
                  .thenComparingLong(o -> o.timestamp)
    );

    private PriorityQueue<Order> sellOrders = new PriorityQueue<>(
        Comparator.<Order>comparingInt(o -> o.price)
                  .thenComparingLong(o -> o.timestamp)
    );

    private Map<Integer, Order> orderMap = new HashMap<>();

    
    private boolean isMatch(Order order, Order oppositeOrder) {
        if (order.side == Order.Side.BUY) {
            return oppositeOrder.price <= order.price;
        } else {
            return oppositeOrder.price >= order.price;
        }
    }


    private void processOrder(Order order, PriorityQueue<Order> oppositeOrders, PriorityQueue<Order> sameSideOrders) {
        while (order.quantity > 0 && !oppositeOrders.isEmpty() && (order.type == Order.Type.MARKET || (order.type == Order.Type.LIMIT && isMatch(order, oppositeOrders.peek())))) {
            Order matchingOrder = oppositeOrders.poll();
            int tradeQty = Math.min(order.quantity, matchingOrder.quantity);
            System.out.println("Trade, price: " + matchingOrder.price + ", qty: " + tradeQty);
            order.quantity -= tradeQty;
            matchingOrder.quantity -= tradeQty;
            if (matchingOrder.quantity > 0) {
                oppositeOrders.add(matchingOrder);
            }
        }
        if (order.type == Order.Type.LIMIT) {
            if (order.quantity > 0) {
                sameSideOrders.add(order);
                orderMap.put(order.id, order);
                System.out.println("Order added to book: " + order);
            }
        } else if (order.type == Order.Type.MARKET && order.quantity > 0) {
            System.out.println("Market order not fully filled, remaining quantity: " + order.quantity);
        }
    }
    
    public void addOrder(Order order) {
        if (order.type == Order.Type.LIMIT || order.type == Order.Type.MARKET) {
            if (order.side == Order.Side.BUY) {
                processOrder(order, sellOrders, buyOrders);
            } else if (order.side == Order.Side.SELL) {
                processOrder(order, buyOrders, sellOrders);
            } else {
                System.out.println("Invalid order side");
            }
        } else {
            System.out.println("Invalid order type");
        }
    }

    public void cancelOrder(int orderId) {
        Order order = orderMap.remove(orderId);
        if (order != null) {
            if (order.side == Order.Side.BUY) {
                buyOrders.remove(order);
            } else {
                sellOrders.remove(order);
            }
            System.out.println("Order cancelled: " + order);
        } else {
            System.out.println("Order not found");
        }
    }

    public void modifyOrder(int orderId, int newPrice, int newQuantity) {
        Order order = orderMap.get(orderId);
        if (order == null) {
            System.out.println("Order not found");
            return;
        }
        if (newPrice <= 0 || newQuantity <= 0) {
            System.out.println("Invalid price or quantity");
            return;
        }
        if (order.side == Order.Side.BUY) {
            buyOrders.remove(order);
        } else {
            sellOrders.remove(order);
        }
        order.price = newPrice;
        order.quantity = newQuantity;
        order.timestamp = System.nanoTime();
        if (order.side == Order.Side.BUY) {
            buyOrders.add(order);
        } else {
            sellOrders.add(order);
        }
        System.out.println("Order modified: " + order);
    }

    public void viewOrderBook() {
        System.out.println("--- Order Book ---");
        System.out.println("Buy Orders:");
        buyOrders.forEach(System.out::println);
        System.out.println("Sell Orders:");
        sellOrders.forEach(System.out::println);
    }
}
