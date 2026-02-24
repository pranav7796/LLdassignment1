public class WhatsAppSender extends NotificationSender {
    public WhatsAppSender(AuditLog audit) { super(audit); }

    @Override
    public void dosend(Notification n) {
        // LSP violation: tightens precondition
        if (n.phone == null || !n.phone.startsWith("+")) {
           System.out.print("phone number is wrong");
           audit.add("wafailed");
           return;
        }
        System.out.println("WA -> to=" + n.phone + " body=" + n.body);
        audit.add("wa sent");
    }
}
