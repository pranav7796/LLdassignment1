public interface EvaluationReportWriter {
    String write(Submission s, int plag, int code);
}