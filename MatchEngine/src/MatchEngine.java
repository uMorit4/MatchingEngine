import java.util.*;

class MatchingEngine {
    // Para compras, a fila é ordenada de forma decrescente pelo preço 
    private PriorityQueue<Order> buyOrders = new PriorityQueue<>(
        Comparator.<Order>comparingInt(o -> -o.price)
                  .thenComparingLong(o -> o.timestamp)
    );

    // Para vendas, a fila é ordenada de forma crescente pelo preço
    private PriorityQueue<Order> sellOrders = new PriorityQueue<>(
        Comparator.<Order>comparingInt(o -> o.price)
                  .thenComparingLong(o -> o.timestamp)
    );

    // Mapeia o id de uma ordem para a ordem em si
    private Map<Integer, Order> orderMap = new HashMap<>();

    // Atualiza o preço apenas se a ordem for pegged e o lado for compatível
    private void updatePeggedOrder(Order order) {
        int referencePrice = -1;
        if (order.pegType == Order.PegType.BID && order.side == Order.Side.BUY) {
            referencePrice = getBestPrice(Order.Side.BUY);
        } else if (order.pegType == Order.PegType.OFFER && order.side == Order.Side.SELL) {
            referencePrice = getBestPrice(Order.Side.SELL);
        }
        if (referencePrice > 0) {
            order.price = referencePrice;
        }
    }

    // Atualiza todas as ordens pegged do lado especificado
    private void updatePeggedOrders(Order.Side side) {
        PriorityQueue<Order> queue = (side == Order.Side.BUY) ? buyOrders : sellOrders;
        List<Order> peggedOrders = new ArrayList<>();
        for (Order o : queue) {
            if (o.pegType != Order.PegType.NONE) {
                peggedOrders.add(o);
            }
        }
        // Itera por todas as ordens pegged e atualiza o preço
        for (Order o : peggedOrders) {
            queue.remove(o);
            updatePeggedOrder(o);
            queue.add(o);
        }
    }

    private int getBestPrice(Order.Side side) {
        // Seleciona a fila com base no lado
        PriorityQueue<Order> orders = (side == Order.Side.BUY) ? buyOrders : sellOrders;
        // Define o valor inicial conforme o lado
        int best = (side == Order.Side.BUY) ? -1 : Integer.MAX_VALUE;
        // Itera sobre a fila para encontrar o melhor preço entre ordens não-pegged
        for (Order o : orders) {
            if (o.pegType == Order.PegType.NONE) {
                if (side == Order.Side.BUY) {
                    best = Math.max(best, o.price);
                } else { 
                    best = Math.min(best, o.price);
                }
            }
        }
        return (side == Order.Side.BUY) ? best : (best == Integer.MAX_VALUE ? -1 : best);
    }

    // Verifica se uma ordem limit de um lado tem match com a ordem oposta
    private boolean isMatch(Order order, Order oppositeOrder) {
        if (order.side == Order.Side.BUY) {
            return oppositeOrder.price <= order.price;
        } else { 
            return oppositeOrder.price >= order.price;
        }
    }

    // Processa uma ordem contra as ordens opostas
    private void processOrder(Order order, PriorityQueue<Order> oppositeOrders, PriorityQueue<Order> sameSideOrders) {
        // Se a ordem for pegged, atualiza seu preço antes de começar o matching
        if (order.pegType != Order.PegType.NONE) {
            updatePeggedOrder(order);
        }
        // Verifica se a ordem é válida
        // Para ordens limit verifica o melhor preço da ordem oposta para ver se há match
        // Para ordens market, executa o trade com o melhor preço que a ordem oposta oferecer
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
        // Se for ordem limit e ainda houver quantidade remanescente, adiciona ao livro
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
    
    // Adiciona uma ordem (limit, market ou pegged) ao matching engine
    public void addOrder(Order order) {
        // Se a ordem for pegged, atualiza seu preço conforme a referência
        if (order.pegType != Order.PegType.NONE) {
            updatePeggedOrder(order);
        }
        if (order.type == Order.Type.LIMIT || order.type == Order.Type.MARKET) {
            if (order.side == Order.Side.BUY) {
                processOrder(order, sellOrders, buyOrders);
                updatePeggedOrders(Order.Side.BUY);
            } else if (order.side == Order.Side.SELL) {
                processOrder(order, buyOrders, sellOrders);
                updatePeggedOrders(Order.Side.SELL);
            } else {
                System.out.println("Invalid order side");
            }
        } else {
            System.out.println("Invalid order type");
        }
    }

    // Primeiramente remove a ordem do seu id atribuído e então a remove da fila correspondente
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

    // Modifica uma ordem existente com um novo preço e quantidade
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
        // Remove a ordem de sua fila
        if (order.side == Order.Side.BUY) {
            buyOrders.remove(order);
        } else {
            sellOrders.remove(order);
        }
        // Atualiza os campos da ordem e a adiciona de volta à fila, com novo timestamp que define sua ordem ao final da fila
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

    // Exibe o livro de ordens, iterando por todas as ordens de compra e venda
    public void viewOrderBook() {
        System.out.println("--- Order Book ---");
        System.out.println("Buy Orders:");
        buyOrders.forEach(System.out::println);
        System.out.println("Sell Orders:");
        sellOrders.forEach(System.out::println);
    }
}
