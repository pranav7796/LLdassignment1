public class InvoiceResult {
    public final String invId;
    public final String content;
    public final int lines;

    public InvoiceResult(String invId, String content, int lines) {
        this.invId = invId;
        this.content = content;
        this.lines = lines;
    }
}
