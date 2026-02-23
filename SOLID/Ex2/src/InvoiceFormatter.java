import java.util.*;

public class InvoiceFormatter {
    
    public String format(String invId, List<PricedLine> lines, 
                         double subtotal, double taxPct, double tax,
                         double discount, double total) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Invoice# ").append(invId).append("\n");
        
        for (PricedLine line : lines) {
            sb.append(String.format("- %s x%d = %.2f\n", 
                line.name, line.qty, line.total));
        }
        
        sb.append(String.format("Subtotal: %.2f\n", subtotal));
        sb.append(String.format("Tax(%.0f%%): %.2f\n", taxPct, tax));
        sb.append(String.format("Discount: -%.2f\n", discount));
        sb.append(String.format("TOTAL: %.2f\n", total));
        
        return sb.toString();
    }
}
