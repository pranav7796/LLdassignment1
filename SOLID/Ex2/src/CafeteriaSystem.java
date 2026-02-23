import java.util.*;

public class CafeteriaSystem {
    private final Map<String, MenuItem> menu = new LinkedHashMap<>();
    private final PricingService pricing;
    private final TaxCalculator taxCalc;
    private final DiscountCalculator discountCalc;
    private final InvoiceFormatter formatter;
    private final InvoiceRepository repo;
    private int invoiceSeq = 1000;

    public CafeteriaSystem(PricingService pricing, TaxCalculator taxCalc,
                           DiscountCalculator discountCalc, InvoiceFormatter formatter,
                           InvoiceRepository repo) {
        this.pricing = pricing;
        this.taxCalc = taxCalc;
        this.discountCalc = discountCalc;
        this.formatter = formatter;
        this.repo = repo;
    }

    public void addToMenu(MenuItem i) { menu.put(i.id, i); }

    public void checkout(String customerType, List<OrderLine> lines) {
        String invId = "INV-" + (++invoiceSeq);

        double subtotal = pricing.calculateSubtotal(lines);
        List<PricedLine> pricedLines = pricing.getPricedLines(lines);

        double taxPct = taxCalc.getTaxPercent(customerType);
        double tax = taxCalc.calculate(subtotal, customerType);

        double discount = discountCalc.calculate(customerType, subtotal, lines.size());

        double total = subtotal + tax - discount;

        String printable = formatter.format(invId, pricedLines, subtotal, taxPct, tax, discount, total);
        System.out.print(printable);

        repo.save(invId, printable);
        System.out.println("Saved invoice: " + invId + " (lines=" + repo.countLines(invId) + ")");
    }
}
