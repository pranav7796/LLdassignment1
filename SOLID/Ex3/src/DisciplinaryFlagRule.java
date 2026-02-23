public class DisciplinaryFlagRule implements EligibilityRule {
    @Override
    public RuleResult check(StudentProfile s) {
        if (s.disciplinaryFlag != LegacyFlags.NONE) {
            return new RuleResult(false, "disciplinary flag present");
        }
        return new RuleResult(true, null);
    }
}
