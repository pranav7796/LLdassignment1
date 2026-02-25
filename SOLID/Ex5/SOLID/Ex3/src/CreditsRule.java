public class CreditsRule implements EligibilityRule {
    @Override
    public RuleResult check(StudentProfile s) {
        if (s.earnedCredits < 20) {
            return new RuleResult(false, "credits below 20");
        }
        return new RuleResult(true, null);
    }
}
