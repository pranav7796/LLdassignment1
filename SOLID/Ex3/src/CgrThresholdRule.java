public class CgrThresholdRule implements EligibilityRule {
    @Override
    public RuleResult check(StudentProfile s) {
        if (s.cgr < 8.0) {
            return new RuleResult(false, "CGR below 8.0");
        }
        return new RuleResult(true, null);
    }
}
