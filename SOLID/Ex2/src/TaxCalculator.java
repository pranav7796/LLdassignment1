public class TaxCalculator {
    
    public double calculate(double subtotal, String customerType) {
        double taxPct = TaxRules.taxPercent(customerType);
        return subtotal * (taxPct / 100.0);
    }

    public double getTaxPercent(String customerType) {
        return TaxRules.taxPercent(customerType);
    }
}
