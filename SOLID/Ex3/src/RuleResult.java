public class RuleResult {
    public final boolean passed;
    public final String reason;

    public RuleResult(boolean passed, String reason) {
        this.passed = passed;
        this.reason = reason;
    }
}
