public class DiscountRules {
    public static double discountAmount(String customerType, double subtotal, int distinctLines) {
        // hard-coded policy (smell)
        if ("student".equalsIgnoreCase(customerType)) {
            if (subtotal >= 180.0) return 10.0;
            return 0.0;
        }
        if ("staff".equalsIgnoreCase(customerType)) {
            if (distinctLines >= 3) return 15.0;
            return 5.0;
        }
        return 0.0;
    }
}
