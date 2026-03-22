public class CodeGrader implements CodeGradingService {
    private final Rubric rubric;

    public CodeGrader() {
        this.rubric = new Rubric();
    }
    
    public int grade(Submission s) {
        // fake scoring (but deterministic)
        int base = Math.min(80, 50 + s.code.length() % 40);
        return base + rubric.bonus;
    }
}
