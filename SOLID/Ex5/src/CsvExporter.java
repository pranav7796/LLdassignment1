import java.nio.charset.StandardCharsets;

public class CsvExporter extends Exporter {
    @Override
    protected ExportResult doExport(ExportRequest req) {
        // Wrap each field in double quotes — this is the correct way to handle commas inside CSV fields
        String title = req.title == null ? "" : req.title;
        String body  = req.body  == null ? "" : req.body;

        // If a field itself contains a double-quote character, escape it by doubling it: " → ""
        title = title.replace("\"", "\"\"");
        body  = body.replace("\"", "\"\"");

        // Build the CSV: header row, then data row
        String csv = "\"title\",\"body\"\n"
                   + "\"" + title + "\",\"" + body + "\"\n";

        return new ExportResult("text/csv", csv.getBytes(StandardCharsets.UTF_8));
    }
}
