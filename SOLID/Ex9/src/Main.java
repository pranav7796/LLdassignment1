public class Main {
    public static void main(String[] args) {
        System.out.println("=== Evaluation Pipeline ===");
        Submission sub = new Submission("23BCS1007", "public class A{}", "A.java");

        PlagiarismCheckService checker = new PlagiarismChecker();
        CodeGradingService grader = new CodeGrader();
        EvaluationReportWriter writer = new ReportWriter();

        EvaluationPipeline pipeline = new EvaluationPipeline(checker, grader, writer);
        pipeline.evaluate(sub);
    }
}
