public interface ITaxCalculator {
    double getTaxPercent(String customerType);
    double calculate(double subtotal, String customerType);
}
