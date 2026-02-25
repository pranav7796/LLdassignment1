import java.util.*;

public class ReceiptPrinter {
    public static void print(BookingRequest req, Money monthly, Money deposit) {
        System.out.println("Room: " + req.roomType.name() + " | AddOns: " + req.addOns);
        System.out.println("Monthly: " + monthly);
        System.out.println("Deposit: " + deposit);
        System.out.println("TOTAL DUE NOW: " + monthly.plus(deposit));
    }
}
