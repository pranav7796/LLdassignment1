public class EvaluationPipeline {
    private final PlagiarismCheckService plagiarismChecker;
    private final CodeGradingService codeGrader;
    private final EvaluationReportWriter reportWriter;

    public EvaluationPipeline(
            PlagiarismCheckService plagiarismChecker,
            CodeGradingService codeGrader,
            EvaluationReportWriter reportWriter) {
        this.plagiarismChecker = plagiarismChecker;
        this.codeGrader = codeGrader;
        this.reportWriter = reportWriter;
    }

    public void evaluate(Submission sub) {
        int plag = plagiarismChecker.check(sub);
        System.out.println("PlagiarismScore=" + plag);

        int code = codeGrader.grade(sub);
        System.out.println("CodeScore=" + code);

        String reportName = reportWriter.write(sub, plag, code);
        System.out.println("Report written: " + reportName);

        int total = plag + code;
        String result = (total >= 90) ? "PASS" : "FAIL";
        System.out.println("FINAL: " + result + " (total=" + total + ")");
    }
}
