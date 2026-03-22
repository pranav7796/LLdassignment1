/**
 * Demo: How to test pipeline with fake implementations
 * No real plagiarism checker, grader, or file writer needed
 */
public class PipelineTest {

    // Fake 1: Always returns fixed plagiarism score
    static class FakeChecker implements PlagiarismCheckService {
        private final int fixedScore;
        public FakeChecker(int fixedScore) { this.fixedScore = fixedScore; }
        @Override public int check(Submission s) { return fixedScore; }
    }

    // Fake 2: Always returns fixed grade
    static class FakeGrader implements CodeGradingService {
        private final int fixedScore;
        public FakeGrader(int fixedScore) { this.fixedScore = fixedScore; }
        @Override public int grade(Submission s) { return fixedScore; }
    }

    // Fake 3: Always returns fixed report name (no file I/O)
    static class FakeWriter implements EvaluationReportWriter {
        private final String fixedName;
        public FakeWriter(String fixedName) { this.fixedName = fixedName; }
        @Override public String write(Submission s, int plag, int code) { return fixedName; }
    }

    public static void main(String[] args) {
        System.out.println("=== Test Scenario 1: PASS case (total >= 90) ===");
        testPassCase();

        System.out.println("\n=== Test Scenario 2: FAIL case (total < 90) ===");
        testFailCase();

        System.out.println("\n=== Test Scenario 3: Edge case (total = 90 exactly) ===");
        testEdgeCase();
    }

    static void testPassCase() {
        // Setup: fake checker returns 40, fake grader returns 50 => total 90 => PASS
        Submission sub = new Submission("test-001", "code", "test.java");
        PlagiarismCheckService checker = new FakeChecker(40);
        CodeGradingService grader = new FakeGrader(50);
        EvaluationReportWriter writer = new FakeWriter("test-report.txt");

        EvaluationPipeline pipeline = new EvaluationPipeline(checker, grader, writer);
        pipeline.evaluate(sub);
        // Output should have FINAL: PASS (total=90)
    }

    static void testFailCase() {
        // Setup: fake checker returns 30, fake grader returns 40 => total 70 => FAIL
        Submission sub = new Submission("test-002", "code", "test.java");
        PlagiarismCheckService checker = new FakeChecker(30);
        CodeGradingService grader = new FakeGrader(40);
        EvaluationReportWriter writer = new FakeWriter("test-fail.txt");

        EvaluationPipeline pipeline = new EvaluationPipeline(checker, grader, writer);
        pipeline.evaluate(sub);
        // Output should have FINAL: FAIL (total=70)
    }

    static void testEdgeCase() {
        // Setup: fake checker returns 45, fake grader returns 45 => total 90 => PASS (boundary)
        Submission sub = new Submission("test-003", "code", "test.java");
        PlagiarismCheckService checker = new FakeChecker(45);
        CodeGradingService grader = new FakeGrader(45);
        EvaluationReportWriter writer = new FakeWriter("test-edge.txt");

        EvaluationPipeline pipeline = new EvaluationPipeline(checker, grader, writer);
        pipeline.evaluate(sub);
        // Output should have FINAL: PASS (total=90)
    }
}
