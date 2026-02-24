public abstract class Exporter {
    // CONTRACT: req must NOT be null; title and body must be non-null
    public final ExportResult export(ExportRequest req) {
        if (req == null) throw new IllegalArgumentException("ExportRequest must not be null");
        if (req.title == null) throw new IllegalArgumentException("ExportRequest.title must not be null");
        if (req.body == null) throw new IllegalArgumentException("ExportRequest.body must not be null");
        return doExport(req);
    }

    protected abstract ExportResult doExport(ExportRequest req);
}
