public class DiscountCalculator {
    
    public double calculate(String customerType, double subtotal, int lineCount) {
        return DiscountRules.discountAmount(customerType, subtotal, lineCount);
    }
}
