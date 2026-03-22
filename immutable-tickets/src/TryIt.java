import com.example.tickets.IncidentTicket;
import com.example.tickets.TicketService;

import java.util.ArrayList;
import java.util.List;

/**
 * Starter demo that shows why mutability is risky.
 *
 * After refactor:
 * - direct mutation should not compile (no setters)
 * - external modifications to tags should not affect the ticket
 * - service "updates" should return a NEW ticket instance
 */
public class TryIt {

    public static void main(String[] args) {
        TicketService service = new TicketService();

        IncidentTicket t = service.createTicket("TCK-1001", "reporter@example.com", "Payment failing on checkout");
        System.out.println("Created: " + t);

        // Updates now return new objects, leaving original untouched.
        IncidentTicket assigned = service.assign(t, "agent@example.com");
        IncidentTicket escalated = service.escalateToCritical(assigned);

        System.out.println("\nOriginal remains unchanged: " + t);
        System.out.println("After update-by-copy       : " + escalated);

        // External mutation via getter is blocked.
        List<String> tags = escalated.getTags();
        try {
            tags.add("HACKED_FROM_OUTSIDE");
        } catch (UnsupportedOperationException ex) {
            System.out.println("\nTags list is immutable from outside.");
        }
        System.out.println("After external attempt      : " + escalated);

        // Defensive copy check: source list changes do not affect ticket tags.
        List<String> sourceTags = new ArrayList<>();
        sourceTags.add("A");
        sourceTags.add("B");
        IncidentTicket extra = IncidentTicket.builder()
                .id("TCK-1002")
                .reporterEmail("reporter@example.com")
                .title("Secondary issue")
                .tags(sourceTags)
                .build();
        sourceTags.add("CHANGED_OUTSIDE");
        System.out.println("\nBuilt with copied tags      : " + extra);
    }
}
