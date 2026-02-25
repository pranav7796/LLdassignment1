public class AttendanceRule implements EligibilityRule {
    @Override
    public RuleResult check(StudentProfile s) {
        if (s.attendancePct < 75) {
            return new RuleResult(false, "attendance below 75");
        }
        return new RuleResult(true, null);
    }
}
