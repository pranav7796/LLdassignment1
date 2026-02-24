public abstract class NotificationSender {
    protected final AuditLog audit;
    protected NotificationSender(AuditLog audit) { this.audit = audit; }
    public final  void send(Notification n){
       if(n==null||n.body==null||n.body.isEmpty())
       {
        return;
       }
       dosend(n);
    }
    protected abstract void dosend(Notification n);
}
