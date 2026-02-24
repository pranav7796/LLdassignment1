import java.nio.charset.StandardCharsets;

public class JsonExporter extends Exporter {
    @Override
    protected ExportResult doExport(ExportRequest req) {
        // No special null check here — contract says req won't be null, trust the contract
        String json = "{\"title\":\"" + escape(req.title) + "\",\"body\":\"" + escape(req.body) + "\"}";
        return new ExportResult("application/json", json.getBytes(StandardCharsets.UTF_8));
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }
}
