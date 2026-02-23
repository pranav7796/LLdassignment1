import java.util.*;

public class HostelFeeCalculator {
    private final BookingRepo repo;

    public HostelFeeCalculator(BookingRepo repo) { this.repo = repo; }

    public void process(BookingRequest req) {
        Money monthly = calculateMonthly(req);
        Money deposit = new Money(5000.00);

        ReceiptPrinter.print(req, monthly, deposit);

        String bookingId = "H-" + (7000 + new Random().nextInt(1000)); // deterministic-ish
        repo.save(bookingId, req, monthly, deposit);
    }

    private Money calculateMonthly(BookingRequest req) {
        double base = req.roomType.getBasePrice();
        double add = 0.0;
        for (AddOn a : req.addOns) {
            add += a.getPrice();
        }

        return new Money(base + add);
    }
}
