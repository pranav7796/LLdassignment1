public interface IDiscountCalculator {
    double calculate(String customerType, double subtotal, int lineCount);
}
