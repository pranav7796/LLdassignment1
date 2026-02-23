import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Cafeteria Billing ===");

        Map<String, MenuItem> menu = new LinkedHashMap<>();
        menu.put("M1", new MenuItem("M1", "Veg Thali", 80.00));
        menu.put("C1", new MenuItem("C1", "Coffee", 30.00));
        menu.put("S1", new MenuItem("S1", "Sandwich", 60.00));

        PricingService pricing = new PricingService(menu);
        TaxCalculator taxCalc = new TaxCalculator();
        DiscountCalculator discountCalc = new DiscountCalculator();
        InvoiceFormatter formatter = new InvoiceFormatter();
        InvoiceRepository repo = new FileStore();

        CafeteriaSystem sys = new CafeteriaSystem(pricing, taxCalc, discountCalc, formatter, repo);

        for (MenuItem item : menu.values()) {
            sys.addToMenu(item);
        }

        List<OrderLine> order = List.of(
                new OrderLine("M1", 2),
                new OrderLine("C1", 1)
        );

        sys.checkout("student", order);
    }
}
