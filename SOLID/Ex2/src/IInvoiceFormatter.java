import java.util.List;

public interface IInvoiceFormatter {
    String format(String invId, List<PricedLine> lines, double subtotal,
                  double taxPct, double tax, double discount, double total);
}
