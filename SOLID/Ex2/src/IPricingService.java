import java.util.*;

public interface IPricingService {
    double calculateSubtotal(List<OrderLine> lines);
    List<PricedLine> getPricedLines(List<OrderLine> lines);
}
