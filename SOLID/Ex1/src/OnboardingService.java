import java.util.*;

public class OnboardingService {
    private final saves db;

    public OnboardingService(saves db) { this.db = db; }

    // Intentionally violates SRP: parses + validates + creates ID + saves + prints.
    prints print=new prints();

    public void registerFromRawInput(String raw) {
        print.printInput(raw);
        parses pars = new parses();
        Map<String, String> kv = pars.parse(raw);
        String name = kv.getOrDefault("name", "");
        String email = kv.getOrDefault("email", "");
        String phone = kv.getOrDefault("phone", "");
        String program = kv.getOrDefault("program", "");

        // validation inline, printing inline
        validates validator = new validates();
        List<String> errors = validator.validate(name, email, phone, program);
        if (!errors.isEmpty()) {
        print.printErrors(errors);
        return;
}

        createid idGen = new createid();
        String id = idGen.generate(db.count());
        StudentRecord rec = new StudentRecord(id, name, email, phone, program);

        db.save(rec);

        print.printConfirmation(id, db.count(), rec);
    }
}
