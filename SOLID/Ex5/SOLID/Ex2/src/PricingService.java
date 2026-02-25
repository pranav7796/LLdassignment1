import java.util.*;

public class PricingService {
    private final Map<String, MenuItem> menu;

    public PricingService(Map<String, MenuItem> menu) {
        this.menu = menu;
    }

    public double calculateSubtotal(List<OrderLine> lines) {
        double subtotal = 0.0;
        for (OrderLine l : lines) {
            MenuItem item = menu.get(l.itemId);
            double lineTotal = item.price * l.qty;
            subtotal += lineTotal;
        }
        return subtotal;
    }

    public List<PricedLine> getPricedLines(List<OrderLine> lines) {
        List<PricedLine> result = new ArrayList<>();
        for (OrderLine l : lines) {
            MenuItem item = menu.get(l.itemId);
            double lineTotal = item.price * l.qty;
            result.add(new PricedLine(item.name, l.qty, lineTotal));
        }
        return result;
    }
}
