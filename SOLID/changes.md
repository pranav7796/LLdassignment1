# SOLID Assignment — Detailed Changes Report (Questions 1–5)

---

## Question 1 — Ex1: Student Onboarding Registration

### SOLID Principle Violated: **Single Responsibility Principle (SRP)**

> *"A class should have only one reason to change."*

### What Was the Problem?

The `OnboardingService.registerFromRawInput()` method was a **god method** — it handled **five distinct responsibilities** in a single method:

1. **Parsing** the raw semicolon-delimited input string into key-value pairs
2. **Validating** name, email, phone, and program fields
3. **Generating** a unique student ID
4. **Persisting** the record into the database (`FakeDb`)
5. **Printing** input echo, error messages, and confirmation output

Additionally, `OnboardingService` was **tightly coupled** to the concrete `FakeDb` class — it could not work with any other storage implementation.

If any one of these responsibilities changed (e.g., new validation rule, different output format, switch to a real database), the entire `registerFromRawInput` method would have to be modified, risking regressions across all five concerns.

---

### Before — Original Code

**`OnboardingService.java` (BEFORE):**
```java
import java.util.*;

public class OnboardingService {
    private final FakeDb db;  // ← tightly coupled to concrete class

    public OnboardingService(FakeDb db) { this.db = db; }

    // God method: parses + validates + creates ID + saves + prints
    public void registerFromRawInput(String raw) {
        System.out.println("INPUT: " + raw);                        // ① printing

        Map<String,String> kv = new LinkedHashMap<>();              // ② parsing
        String[] parts = raw.split(";");
        for (String p : parts) {
            String[] t = p.split("=", 2);
            if (t.length == 2) kv.put(t[0].trim(), t[1].trim());
        }

        String name = kv.getOrDefault("name", "");
        String email = kv.getOrDefault("email", "");
        String phone = kv.getOrDefault("phone", "");
        String program = kv.getOrDefault("program", "");

        List<String> errors = new ArrayList<>();                    // ③ validating
        if (name.isBlank()) errors.add("name is required");
        if (email.isBlank() || !email.contains("@")) errors.add("email is invalid");
        if (phone.isBlank() || !phone.chars().allMatch(Character::isDigit)) errors.add("phone is invalid");
        if (!(program.equals("CSE") || program.equals("AI") || program.equals("SWE"))) errors.add("program is invalid");

        if (!errors.isEmpty()) {
            System.out.println("ERROR: cannot register");           // ① printing
            for (String e : errors) System.out.println("- " + e);
            return;
        }

        String id = IdUtil.nextStudentId(db.count());               // ④ ID generation
        StudentRecord rec = new StudentRecord(id, name, email, phone, program);

        db.save(rec);                                                // ⑤ persistence

        System.out.println("OK: created student " + id);            // ① printing
        System.out.println("Saved. Total students: " + db.count());
        System.out.println("CONFIRMATION:");
        System.out.println(rec);
    }
}
```

**`FakeDb.java` (BEFORE):**
```java
public class FakeDb {  // ← no interface
    private final List<StudentRecord> rows = new ArrayList<>();
    public void save(StudentRecord r) { rows.add(r); }
    public int count() { return rows.size(); }
    public List<StudentRecord> all() { return Collections.unmodifiableList(rows); }
}
```

---

### After — Refactored Code

**New file: `saves.java` — persistence interface:**
```java
public interface saves {
    void save(StudentRecord r);
    int count();
}
```

**New file: `parses.java` — parsing responsibility:**
```java
import java.util.*;

public class parses {
    public Map<String, String> parse(String raw) {
        Map<String, String> kv = new LinkedHashMap<>();
        String[] parts = raw.split(";");
        for (String p : parts) {
            String[] t = p.split("=", 2);
            if (t.length == 2) kv.put(t[0].trim(), t[1].trim());
        }
        return kv;
    }
}
```

**New file: `validates.java` — validation responsibility:**
```java
import java.util.*;

public class validates {
    public List<String> validate(String name, String email, String phone, String program) {
        List<String> errors = new ArrayList<>();
        if (name.isBlank()) errors.add("name is required");
        if (email.isBlank() || !email.contains("@")) errors.add("email is invalid");
        if (phone.isBlank() || !phone.chars().allMatch(Character::isDigit)) errors.add("phone is invalid");
        if (!(program.equals("CSE") || program.equals("AI") || program.equals("SWE"))) errors.add("program is invalid");
        return errors;
    }
}
```

**New file: `createid.java` — ID generation responsibility:**
```java
public class createid {
    public String generate(int currentCount) {
        return IdUtil.nextStudentId(currentCount);
    }
}
```

**New file: `prints.java` — output/printing responsibility:**
```java
import java.util.*;

public class prints {
    public void printInput(String raw) {
        System.out.println("INPUT: " + raw);
    }
    public void printErrors(List<String> errors) {
        System.out.println("ERROR: cannot register");
        for (String e : errors) System.out.println("- " + e);
    }
    public void printConfirmation(String id, int totalCount, StudentRecord rec) {
        System.out.println("OK: created student " + id);
        System.out.println("Saved. Total students: " + totalCount);
        System.out.println("CONFIRMATION:");
        System.out.println(rec);
    }
}
```

**`FakeDb.java` (AFTER) — now implements `saves` interface:**
```java
public class FakeDb implements saves {
    private final List<StudentRecord> rows = new ArrayList<>();
    public void save(StudentRecord r) { rows.add(r); }
    public int count() { return rows.size(); }
    public List<StudentRecord> all() { return Collections.unmodifiableList(rows); }
}
```

**`OnboardingService.java` (AFTER) — now a thin orchestrator:**
```java
import java.util.*;

public class OnboardingService {
    private final saves db;   // ← depends on interface, not concrete class

    public OnboardingService(saves db) { this.db = db; }

    prints print = new prints();

    public void registerFromRawInput(String raw) {
        print.printInput(raw);

        parses pars = new parses();
        Map<String, String> kv = pars.parse(raw);       // delegates parsing
        String name = kv.getOrDefault("name", "");
        String email = kv.getOrDefault("email", "");
        String phone = kv.getOrDefault("phone", "");
        String program = kv.getOrDefault("program", "");

        validates validator = new validates();
        List<String> errors = validator.validate(name, email, phone, program);  // delegates validation
        if (!errors.isEmpty()) {
            print.printErrors(errors);                    // delegates printing
            return;
        }

        createid idGen = new createid();
        String id = idGen.generate(db.count());           // delegates ID generation
        StudentRecord rec = new StudentRecord(id, name, email, phone, program);

        db.save(rec);                                     // delegates persistence (via interface)

        print.printConfirmation(id, db.count(), rec);     // delegates printing
    }
}
```

---

### Structural Changes Summary

| Aspect | Before | After |
|--------|--------|-------|
| **New files created** | — | `saves.java`, `parses.java`, `validates.java`, `createid.java`, `prints.java` |
| **DB dependency** | Concrete `FakeDb` | `saves` interface |
| **Parsing** | Inline in `registerFromRawInput()` | Extracted to `parses.parse()` |
| **Validation** | Inline if-checks | Extracted to `validates.validate()` |
| **ID generation** | Direct static call `IdUtil.nextStudentId()` | Wrapped in `createid.generate()` |
| **Printing** | Scattered `System.out.println` calls | Grouped in `prints` class |
| **`OnboardingService` role** | God class doing everything | Thin orchestrator delegating to specialists |

### Why the Refactor Is Better

| Quality | Explanation |
|---------|-------------|
| **Coupling** | `OnboardingService` no longer depends on `FakeDb` directly — it depends on the `saves` interface. You can swap in any persistence implementation (real DB, mock, etc.) without touching the service. |
| **Extensibility** | Adding a new validation rule means editing only `validates`. Changing the output format means editing only `prints`. Each concern is isolated, so changes don't ripple. |
| **Testability** | Each extracted class (`parses`, `validates`, `createid`, `prints`) can be **unit tested independently** without needing the full registration flow. You can also mock the `saves` interface to test the service without a real database. |

---
---

## Question 2 — Ex2: Campus Cafeteria Billing

### SOLID Principle Violated: **Single Responsibility Principle (SRP)**

> *"A class should have only one reason to change."*

### What Was the Problem?

The `CafeteriaSystem.checkout()` method was another **god method** that combined **five different responsibilities**:

1. **Pricing** — looking up menu items and computing line totals + subtotal
2. **Tax calculation** — applying tax percentage based on customer type
3. **Discount calculation** — applying discounts based on customer type, subtotal, line count
4. **Invoice formatting** — building the entire invoice string with `StringBuilder`
5. **Persistence** — saving the invoice to `FileStore` (a concrete class)

The `InvoiceFormatter` class existed but was a **pointless wrapper** (`identityFormat` just returned its input). The `FileStore` was used as a concrete dependency with no abstraction.

---

### Before — Original Code

**`CafeteriaSystem.java` (BEFORE):**
```java
import java.util.*;

public class CafeteriaSystem {
    private final Map<String, MenuItem> menu = new LinkedHashMap<>();
    private final FileStore store = new FileStore();   // ← concrete dependency
    private int invoiceSeq = 1000;

    public void addToMenu(MenuItem i) { menu.put(i.id, i); }

    // God method: menu lookup + tax + discount + format + persistence
    public void checkout(String customerType, List<OrderLine> lines) {
        String invId = "INV-" + (++invoiceSeq);
        StringBuilder out = new StringBuilder();
        out.append("Invoice# ").append(invId).append("\n");

        double subtotal = 0.0;                          // ① pricing inline
        for (OrderLine l : lines) {
            MenuItem item = menu.get(l.itemId);
            double lineTotal = item.price * l.qty;
            subtotal += lineTotal;
            out.append(String.format("- %s x%d = %.2f\n", item.name, l.qty, lineTotal)); // ④ formatting
        }

        double taxPct = TaxRules.taxPercent(customerType);   // ② tax inline
        double tax = subtotal * (taxPct / 100.0);

        double discount = DiscountRules.discountAmount(customerType, subtotal, lines.size()); // ③ discount inline

        double total = subtotal + tax - discount;

        out.append(String.format("Subtotal: %.2f\n", subtotal));         // ④ formatting
        out.append(String.format("Tax(%.0f%%): %.2f\n", taxPct, tax));
        out.append(String.format("Discount: -%.2f\n", discount));
        out.append(String.format("TOTAL: %.2f\n", total));

        String printable = InvoiceFormatter.identityFormat(out.toString()); // pointless wrapper
        System.out.print(printable);

        store.save(invId, printable);                    // ⑤ persistence
        System.out.println("Saved invoice: " + invId + " (lines=" + store.countLines(invId) + ")");
    }
}
```

**`InvoiceFormatter.java` (BEFORE) — pointless wrapper:**
```java
public class InvoiceFormatter {
    public static String identityFormat(String s) { return s; }  // does nothing
}
```

---

### After — Refactored Code

**New interfaces:**
```java
// IPricingService.java
public interface IPricingService {
    double calculateSubtotal(List<OrderLine> lines);
    List<PricedLine> getPricedLines(List<OrderLine> lines);
}

// ITaxCalculator.java
public interface ITaxCalculator {
    double getTaxPercent(String customerType);
    double calculate(double subtotal, String customerType);
}

// IDiscountCalculator.java
public interface IDiscountCalculator {
    double calculate(String customerType, double subtotal, int lineCount);
}

// IInvoiceFormatter.java
public interface IInvoiceFormatter {
    String format(String invId, List<PricedLine> lines, double subtotal,
                  double taxPct, double tax, double discount, double total);
}

// InvoiceRepository.java
public interface InvoiceRepository {
    void save(String name, String content);
    int countLines(String name);
}
```

**New implementations:**
```java
// PricingService.java
public class PricingService implements IPricingService {
    private final Map<String, MenuItem> menu;
    public PricingService(Map<String, MenuItem> menu) { this.menu = menu; }

    public double calculateSubtotal(List<OrderLine> lines) {
        double subtotal = 0.0;
        for (OrderLine l : lines) {
            MenuItem item = menu.get(l.itemId);
            subtotal += item.price * l.qty;
        }
        return subtotal;
    }

    public List<PricedLine> getPricedLines(List<OrderLine> lines) {
        List<PricedLine> result = new ArrayList<>();
        for (OrderLine l : lines) {
            MenuItem item = menu.get(l.itemId);
            result.add(new PricedLine(item.name, l.qty, item.price * l.qty));
        }
        return result;
    }
}

// TaxCalculator.java
public class TaxCalculator implements ITaxCalculator {
    public double calculate(double subtotal, String customerType) {
        return subtotal * (TaxRules.taxPercent(customerType) / 100.0);
    }
    public double getTaxPercent(String customerType) {
        return TaxRules.taxPercent(customerType);
    }
}

// DiscountCalculator.java
public class DiscountCalculator implements IDiscountCalculator {
    public double calculate(String customerType, double subtotal, int lineCount) {
        return DiscountRules.discountAmount(customerType, subtotal, lineCount);
    }
}

// PricedLine.java — data carrier
public class PricedLine {
    public final String name;
    public final int qty;
    public final double total;
    public PricedLine(String name, int qty, double total) { ... }
}

// InvoiceResult.java — data carrier for checkout result
public class InvoiceResult {
    public final String invId;
    public final String content;
    public final int lines;
    public InvoiceResult(String invId, String content, int lines) { ... }
}
```

**`InvoiceFormatter.java` (AFTER) — now does real work:**
```java
public class InvoiceFormatter implements IInvoiceFormatter {
    public String format(String invId, List<PricedLine> lines,
                         double subtotal, double taxPct, double tax,
                         double discount, double total) {
        StringBuilder sb = new StringBuilder();
        sb.append("Invoice# ").append(invId).append("\n");
        for (PricedLine line : lines) {
            sb.append(String.format("- %s x%d = %.2f\n", line.name, line.qty, line.total));
        }
        sb.append(String.format("Subtotal: %.2f\n", subtotal));
        sb.append(String.format("Tax(%.0f%%): %.2f\n", taxPct, tax));
        sb.append(String.format("Discount: -%.2f\n", discount));
        sb.append(String.format("TOTAL: %.2f\n", total));
        return sb.toString();
    }
}
```

**`FileStore.java` (AFTER) — now implements interface:**
```java
public class FileStore implements InvoiceRepository { ... }
```

**`CafeteriaSystem.java` (AFTER) — thin orchestrator with DI:**
```java
public class CafeteriaSystem {
    private final Map<String, MenuItem> menu = new LinkedHashMap<>();
    private final IPricingService pricing;
    private final ITaxCalculator taxCalc;
    private final IDiscountCalculator discountCalc;
    private final IInvoiceFormatter formatter;
    private final InvoiceRepository repo;
    private int invoiceSeq = 1000;

    public CafeteriaSystem(IPricingService pricing, ITaxCalculator taxCalc,
                           IDiscountCalculator discountCalc, IInvoiceFormatter formatter,
                           InvoiceRepository repo) {
        this.pricing = pricing;
        this.taxCalc = taxCalc;
        this.discountCalc = discountCalc;
        this.formatter = formatter;
        this.repo = repo;
    }

    public InvoiceResult checkout(String customerType, List<OrderLine> lines) {
        String invId = "INV-" + (++invoiceSeq);
        double subtotal = pricing.calculateSubtotal(lines);
        List<PricedLine> pricedLines = pricing.getPricedLines(lines);
        double taxPct = taxCalc.getTaxPercent(customerType);
        double tax = taxCalc.calculate(subtotal, customerType);
        double discount = discountCalc.calculate(customerType, subtotal, lines.size());
        double total = subtotal + tax - discount;
        String printable = formatter.format(invId, pricedLines, subtotal, taxPct, tax, discount, total);
        repo.save(invId, printable);
        return new InvoiceResult(invId, printable, repo.countLines(invId));
    }
}
```

---

### Structural Changes Summary

| Aspect | Before | After |
|--------|--------|-------|
| **New files** | — | `IPricingService`, `ITaxCalculator`, `IDiscountCalculator`, `IInvoiceFormatter`, `InvoiceRepository`, `PricingService`, `TaxCalculator`, `DiscountCalculator`, `PricedLine`, `InvoiceResult` |
| **Pricing** | Inline loop in `checkout()` | Delegated to `PricingService` |
| **Tax** | Direct static call to `TaxRules` | Delegated to `TaxCalculator` (via `ITaxCalculator`) |
| **Discount** | Direct static call to `DiscountRules` | Delegated to `DiscountCalculator` (via `IDiscountCalculator`) |
| **Formatting** | Inline `StringBuilder` + pointless `identityFormat` | Delegated to `InvoiceFormatter` (via `IInvoiceFormatter`) |
| **Persistence** | Concrete `FileStore` field | `InvoiceRepository` interface (injected) |
| **Constructor** | No-arg (hard-wired dependencies) | Constructor injection of all 5 dependencies |
| **`CafeteriaSystem` role** | God class | Pure orchestrator |

### Why the Refactor Is Better

| Quality | Explanation |
|---------|-------------|
| **Coupling** | `CafeteriaSystem` no longer directly depends on `FileStore`, `TaxRules`, `DiscountRules`, or formatting logic. Every dependency is injected through an interface. The system is now **loosely coupled**. |
| **Extensibility** | Need a new tax rule? Create a new `ITaxCalculator` implementation. Need a different invoice format (HTML, PDF)? Create a new `IInvoiceFormatter`. Need to save to a real DB? Implement `InvoiceRepository`. None of this requires touching `CafeteriaSystem`. |
| **Testability** | Each component (`PricingService`, `TaxCalculator`, `DiscountCalculator`, `InvoiceFormatter`) can be tested in isolation. For integration tests, you can inject mock implementations of every interface. The god method is gone — there's nothing to "bypass" during testing. |

---
---

## Question 3 — Ex3: Placement Eligibility Rules Engine

### SOLID Principle Violated: **Open/Closed Principle (OCP)**

> *"Software entities should be open for extension but closed for modification."*

### What Was the Problem?

The `EligibilityEngine.evaluate()` method used a **long if/else-if chain** to check each eligibility rule:

- Disciplinary flag check
- CGR threshold check
- Attendance percentage check
- Earned credits check

Every rule was **hard-coded** directly in the method body. To add a new rule (e.g., "backlog count"), you would have to **modify** the `evaluate()` method itself — violating OCP. This makes the method fragile: any change risks breaking existing rules, and the method grows linearly with each new rule.

---

### Before — Original Code

**`EligibilityEngine.java` (BEFORE):**
```java
import java.util.*;

public class EligibilityEngine {
    private final FakeEligibilityStore store;

    public EligibilityEngine(FakeEligibilityStore store) { this.store = store; }

    public void runAndPrint(StudentProfile s) {
        ReportPrinter p = new ReportPrinter();
        EligibilityEngineResult r = evaluate(s);  // giant conditional inside
        p.print(s, r);
        store.save(s.rollNo, r.status);
    }

    public EligibilityEngineResult evaluate(StudentProfile s) {
        List<String> reasons = new ArrayList<>();
        String status = "ELIGIBLE";

        // OCP violation: long chain for each rule
        if (s.disciplinaryFlag != LegacyFlags.NONE) {
            status = "NOT_ELIGIBLE";
            reasons.add("disciplinary flag present");
        } else if (s.cgr < 8.0) {
            status = "NOT_ELIGIBLE";
            reasons.add("CGR below 8.0");
        } else if (s.attendancePct < 75) {
            status = "NOT_ELIGIBLE";
            reasons.add("attendance below 75");
        } else if (s.earnedCredits < 20) {
            status = "NOT_ELIGIBLE";
            reasons.add("credits below 20");
        }

        return new EligibilityEngineResult(status, reasons);
    }
}
```

---

### After — Refactored Code

**New file: `EligibilityRule.java` — rule abstraction:**
```java
public interface EligibilityRule {
    RuleResult check(StudentProfile s);
}
```

**New file: `RuleResult.java` — rule output data carrier:**
```java
public class RuleResult {
    public final boolean passed;
    public final String reason;
    public RuleResult(boolean passed, String reason) {
        this.passed = passed;
        this.reason = reason;
    }
}
```

**New file: `DisciplinaryFlagRule.java`:**
```java
public class DisciplinaryFlagRule implements EligibilityRule {
    @Override
    public RuleResult check(StudentProfile s) {
        if (s.disciplinaryFlag != LegacyFlags.NONE) {
            return new RuleResult(false, "disciplinary flag present");
        }
        return new RuleResult(true, null);
    }
}
```

**New file: `CgrThresholdRule.java`:**
```java
public class CgrThresholdRule implements EligibilityRule {
    @Override
    public RuleResult check(StudentProfile s) {
        if (s.cgr < 8.0) {
            return new RuleResult(false, "CGR below 8.0");
        }
        return new RuleResult(true, null);
    }
}
```

**New file: `AttendanceRule.java`:**
```java
public class AttendanceRule implements EligibilityRule {
    @Override
    public RuleResult check(StudentProfile s) {
        if (s.attendancePct < 75) {
            return new RuleResult(false, "attendance below 75");
        }
        return new RuleResult(true, null);
    }
}
```

**New file: `CreditsRule.java`:**
```java
public class CreditsRule implements EligibilityRule {
    @Override
    public RuleResult check(StudentProfile s) {
        if (s.earnedCredits < 20) {
            return new RuleResult(false, "credits below 20");
        }
        return new RuleResult(true, null);
    }
}
```

**`EligibilityEngine.java` (AFTER) — iterates over rule list:**
```java
import java.util.*;

public class EligibilityEngine {
    private final FakeEligibilityStore store;
    private final List<EligibilityRule> rules;

    // Default constructor with standard rules
    public EligibilityEngine(FakeEligibilityStore store) {
        this.store = store;
        this.rules = List.of(
            new DisciplinaryFlagRule(),
            new CgrThresholdRule(),
            new AttendanceRule(),
            new CreditsRule()
        );
    }

    // Overloaded constructor: inject custom rules
    public EligibilityEngine(FakeEligibilityStore store, List<EligibilityRule> rules) {
        this.store = store;
        this.rules = rules;
    }

    public void runAndPrint(StudentProfile s) {
        ReportPrinter p = new ReportPrinter();
        EligibilityEngineResult r = evaluate(s);
        p.print(s, r);
        store.save(s.rollNo, r.status);
    }

    public EligibilityEngineResult evaluate(StudentProfile s) {
        List<String> reasons = new ArrayList<>();
        String status = "ELIGIBLE";

        for (EligibilityRule rule : rules) {      // ← iterate, don't branch
            RuleResult result = rule.check(s);
            if (!result.passed) {
                status = "NOT_ELIGIBLE";
                reasons.add(result.reason);
                break;
            }
        }

        return new EligibilityEngineResult(status, reasons);
    }
}
```

---

### Structural Changes Summary

| Aspect | Before | After |
|--------|--------|-------|
| **New files** | — | `EligibilityRule.java`, `RuleResult.java`, `DisciplinaryFlagRule.java`, `CgrThresholdRule.java`, `AttendanceRule.java`, `CreditsRule.java` |
| **Rule evaluation** | Hard-coded if/else-if chain in `evaluate()` | Loop over `List<EligibilityRule>` |
| **Adding a new rule** | Edit `evaluate()` method, risk regressions | Create a new class implementing `EligibilityRule`, add to list — **zero** edits to engine |
| **Rule injection** | Not possible | Overloaded constructor accepts custom `List<EligibilityRule>` |
| **Rule data model** | Status strings and reasons created inline | `RuleResult` data carrier with `passed` flag and `reason` |

### Why the Refactor Is Better

| Quality | Explanation |
|---------|-------------|
| **Coupling** | Each rule is self-contained in its own class. Rules don't know about each other. The engine doesn't know the internals of any specific rule — it just calls `check()` on each one. |
| **Extensibility** | To add a "backlog count" rule, you simply create `BacklogRule implements EligibilityRule` and add it to the rules list. The `EligibilityEngine` class **does not need to be modified** — this is OCP in action. |
| **Testability** | Each rule can be tested independently with a `StudentProfile` — no need to set up the entire engine. You can also test the engine with a custom rules list (via the overloaded constructor) to verify orchestration logic in isolation. |

---
---

## Question 4 — Ex4: Hostel Fee Calculator

### SOLID Principle Violated: **Open/Closed Principle (OCP)**

> *"Software entities should be open for extension but closed for modification."*

### What Was the Problem?

The `HostelFeeCalculator.calculateMonthly()` method had **two OCP violations**:

1. **Room pricing via switch-case on int constants** — `LegacyRoomTypes` used raw `int` values (`SINGLE=1`, `DOUBLE=2`, etc.) and a `switch` statement mapped them to prices. Adding a new room type required editing the switch.

2. **Add-on pricing via if/else chain** — each `AddOn` enum value was checked individually (`if MESS → 1000`, `if LAUNDRY → 500`, `if GYM → 300`). Adding a new add-on required editing this chain.

Additionally, the `BookingRequest` used `int roomType` (a **primitive obsession** smell), and `FakeBookingRepo` was used as a concrete dependency without an interface.

---

### Before — Original Code

**`LegacyRoomTypes.java` (BEFORE) — raw int constants:**
```java
public class LegacyRoomTypes {
    public static final int SINGLE = 1;
    public static final int DOUBLE = 2;
    public static final int TRIPLE = 3;

    public static String nameOf(int type) {
        return switch (type) {
            case SINGLE -> "SINGLE";
            case DOUBLE -> "DOUBLE";
            case TRIPLE -> "TRIPLE";
            default -> "UNKNOWN";
        };
    }
}
```

**`AddOn.java` (BEFORE) — plain enum, no pricing data:**
```java
public enum AddOn {
    MESS, LAUNDRY, GYM
}
```

**`BookingRequest.java` (BEFORE) — int roomType:**
```java
public class BookingRequest {
    public final int roomType;       // ← primitive obsession
    public final List<AddOn> addOns;
    public BookingRequest(int roomType, List<AddOn> addOns) { ... }
}
```

**`HostelFeeCalculator.java` (BEFORE):**
```java
public class HostelFeeCalculator {
    private final FakeBookingRepo repo;  // ← concrete class

    public HostelFeeCalculator(FakeBookingRepo repo) { this.repo = repo; }

    public void process(BookingRequest req) {
        Money monthly = calculateMonthly(req);
        Money deposit = new Money(5000.00);
        ReceiptPrinter.print(req, monthly, deposit);
        String bookingId = "H-" + (7000 + new Random(1).nextInt(1000));
        repo.save(bookingId, req, monthly, deposit);
    }

    private Money calculateMonthly(BookingRequest req) {
        double base;
        switch (req.roomType) {                              // ← OCP violation: switch
            case LegacyRoomTypes.SINGLE -> base = 14000.0;
            case LegacyRoomTypes.DOUBLE -> base = 15000.0;
            case LegacyRoomTypes.TRIPLE -> base = 12000.0;
            default -> base = 16000.0;
        }

        double add = 0.0;
        for (AddOn a : req.addOns) {                         // ← OCP violation: if/else
            if (a == AddOn.MESS) add += 1000.0;
            else if (a == AddOn.LAUNDRY) add += 500.0;
            else if (a == AddOn.GYM) add += 300.0;
        }

        return new Money(base + add);
    }
}
```

---

### After — Refactored Code

**New file: `RoomType.java` — enum with embedded pricing:**
```java
public enum RoomType {
    SINGLE(14000.0),
    DOUBLE(15000.0),
    TRIPLE(12000.0),
    DELUXE(16000.0);

    private final double basePrice;
    RoomType(double basePrice) { this.basePrice = basePrice; }
    public double getBasePrice() { return basePrice; }
}
```

**`AddOn.java` (AFTER) — enum with embedded pricing:**
```java
public enum AddOn {
    MESS(1000.0), LAUNDRY(500.0), GYM(300.0);
    private final double price;
    AddOn(double price) { this.price = price; }
    public double getPrice() { return price; }
}
```

**New file: `BookingRepo.java` — persistence interface:**
```java
public interface BookingRepo {
    void save(String id, BookingRequest req, Money monthly, Money deposit);
}
```

**`FakeBookingRepo.java` (AFTER):**
```java
public class FakeBookingRepo implements BookingRepo { ... }
```

**`BookingRequest.java` (AFTER) — uses `RoomType` enum:**
```java
public class BookingRequest {
    public final RoomType roomType;    // ← type-safe enum
    public final List<AddOn> addOns;
    public BookingRequest(RoomType roomType, List<AddOn> addOns) { ... }
}
```

**`HostelFeeCalculator.java` (AFTER) — no switch, no if/else:**
```java
public class HostelFeeCalculator {
    private final BookingRepo repo;     // ← interface dependency

    public HostelFeeCalculator(BookingRepo repo) { this.repo = repo; }

    public void process(BookingRequest req) {
        Money monthly = calculateMonthly(req);
        Money deposit = new Money(5000.00);
        ReceiptPrinter.print(req, monthly, deposit);
        String bookingId = "H-" + (7000 + new Random().nextInt(1000));
        repo.save(bookingId, req, monthly, deposit);
    }

    private Money calculateMonthly(BookingRequest req) {
        double base = req.roomType.getBasePrice();    // ← polymorphic, no switch
        double add = 0.0;
        for (AddOn a : req.addOns) {
            add += a.getPrice();                       // ← polymorphic, no if/else
        }
        return new Money(base + add);
    }
}
```

**`LegacyRoomTypes.java` → DELETED** (no longer needed)

---

### Structural Changes Summary

| Aspect | Before | After |
|--------|--------|-------|
| **Room types** | `LegacyRoomTypes` with int constants + switch | `RoomType` enum with `getBasePrice()` |
| **Add-on pricing** | If/else chain in calculator | `AddOn` enum with `getPrice()` |
| **Room type in request** | `int roomType` (primitive) | `RoomType roomType` (type-safe enum) |
| **Persistence** | Concrete `FakeBookingRepo` | `BookingRepo` interface |
| **Files deleted** | — | `LegacyRoomTypes.java` removed |
| **Files created** | — | `RoomType.java`, `BookingRepo.java` |

### Why the Refactor Is Better

| Quality | Explanation |
|---------|-------------|
| **Coupling** | `HostelFeeCalculator` no longer knows room prices or add-on prices. Each enum carries its own data. The calculator also depends on `BookingRepo` interface, not a concrete class. |
| **Extensibility** | Adding a new room type (e.g., `SUITE(20000.0)`) = add one line to the `RoomType` enum. Adding a new add-on (e.g., `WIFI(200.0)`) = add one line to `AddOn` enum. **Zero changes** to `HostelFeeCalculator` — OCP satisfied. |
| **Testability** | The calculator can be tested with any `BookingRepo` mock. Room/add-on pricing is self-contained in enums and can be verified independently. No more fragile switch-cases to test exhaustively. |

---
---

## Question 5 — Ex5: File Exporter Hierarchy

### SOLID Principle Violated: **Liskov Substitution Principle (LSP)**

> *"Objects of a supertype should be replaceable with objects of a subtype without altering the correctness of the program."*

### What Was the Problem?

The `Exporter` base class defined `public abstract ExportResult export(ExportRequest req)`, but each subclass **violated the implicit contract** in different ways:

| Subclass | LSP Violation |
|----------|---------------|
| **`PdfExporter`** | **Tightened preconditions** — threw `IllegalArgumentException` if `body.length() > 20`. A caller using a base `Exporter` reference would not expect this restriction. |
| **`CsvExporter`** | **Changed semantics / weakened postconditions** — silently replaced newlines and commas with spaces (`req.body.replace("\n", " ").replace(",", " ")`), causing **lossy data corruption**. The caller gets back different data than they sent in. |
| **`JsonExporter`** | **Inconsistent null handling** — returned an empty result for `null` input instead of failing, while other exporters would crash. Callers couldn't rely on uniform behavior. |

Because each subclass behaved differently, **callers could not treat `Exporter` references uniformly**. The `Main` class had to wrap calls in `try/catch` — proof that LSP was violated.

---

### Before — Original Code

**`Exporter.java` (BEFORE) — no enforced contract:**
```java
public abstract class Exporter {
    // implied "contract" but not enforced (smell)
    public abstract ExportResult export(ExportRequest req);
}
```

**`PdfExporter.java` (BEFORE) — tightens precondition:**
```java
public class PdfExporter extends Exporter {
    @Override
    public ExportResult export(ExportRequest req) {
        // LSP violation: tightens precondition arbitrarily
        if (req.body != null && req.body.length() > 20) {
            throw new IllegalArgumentException("PDF cannot handle content > 20 chars");
        }
        String fakePdf = "PDF(" + req.title + "):" + req.body;
        return new ExportResult("application/pdf", fakePdf.getBytes(StandardCharsets.UTF_8));
    }
}
```

**`CsvExporter.java` (BEFORE) — lossy conversion:**
```java
public class CsvExporter extends Exporter {
    @Override
    public ExportResult export(ExportRequest req) {
        // LSP issue: changes meaning by lossy conversion
        String body = req.body == null ? "" : req.body.replace("\n", " ").replace(",", " ");
        String csv = "title,body\n" + req.title + "," + body + "\n";
        return new ExportResult("text/csv", csv.getBytes(StandardCharsets.UTF_8));
    }
}
```

**`JsonExporter.java` (BEFORE) — inconsistent null handling:**
```java
public class JsonExporter extends Exporter {
    @Override
    public ExportResult export(ExportRequest req) {
        // inconsistent handling (surprise)
        if (req == null) return new ExportResult("application/json", new byte[0]);
        String json = "{\"title\":\"" + escape(req.title) + "\",\"body\":\"" + escape(req.body) + "\"}";
        return new ExportResult("application/json", json.getBytes(StandardCharsets.UTF_8));
    }
    ...
}
```

---

### After — Refactored Code

**`Exporter.java` (AFTER) — Template Method Pattern with enforced contract:**
```java
public abstract class Exporter {
    // CONTRACT: req must NOT be null; title and body must be non-null
    public final ExportResult export(ExportRequest req) {
        if (req == null) throw new IllegalArgumentException("ExportRequest must not be null");
        if (req.title == null) throw new IllegalArgumentException("ExportRequest.title must not be null");
        if (req.body == null) throw new IllegalArgumentException("ExportRequest.body must not be null");
        return doExport(req);
    }

    protected abstract ExportResult doExport(ExportRequest req);
}
```

**`PdfExporter.java` (AFTER) — no more arbitrary restrictions:**
```java
public class PdfExporter extends Exporter {
    @Override
    protected ExportResult doExport(ExportRequest req) {
        // No size restriction — accept any input
        String fakePdf = "PDF(" + req.title + "):" + req.body;
        return new ExportResult("application/pdf", fakePdf.getBytes(StandardCharsets.UTF_8));
    }
}
```

**`CsvExporter.java` (AFTER) — proper CSV escaping, no data loss:**
```java
public class CsvExporter extends Exporter {
    @Override
    protected ExportResult doExport(ExportRequest req) {
        String title = req.title == null ? "" : req.title;
        String body  = req.body  == null ? "" : req.body;

        // Proper CSV: escape double-quotes by doubling them, wrap fields in quotes
        title = title.replace("\"", "\"\"");
        body  = body.replace("\"", "\"\"");

        String csv = "\"title\",\"body\"\n"
                   + "\"" + title + "\",\"" + body + "\"\n";

        return new ExportResult("text/csv", csv.getBytes(StandardCharsets.UTF_8));
    }
}
```

**`JsonExporter.java` (AFTER) — trusts the base contract:**
```java
public class JsonExporter extends Exporter {
    @Override
    protected ExportResult doExport(ExportRequest req) {
        // No special null check — contract guarantees req won't be null
        String json = "{\"title\":\"" + escape(req.title) + "\",\"body\":\"" + escape(req.body) + "\"}";
        return new ExportResult("application/json", json.getBytes(StandardCharsets.UTF_8));
    }
    ...
}
```

---

### Structural Changes Summary

| Aspect | Before | After |
|--------|--------|-------|
| **Base class method** | `public abstract export()` — no rules | `public final export()` — enforces preconditions, then calls `doExport()` |
| **Extension point** | Subclasses override `export()` directly | Subclasses override `protected doExport()` — can't skip validation |
| **Pattern used** | None | **Template Method Pattern** |
| **PdfExporter** | Threw exception if body > 20 chars | Accepts any input — tightened precondition removed |
| **CsvExporter** | Stripped newlines/commas (lossy) | Proper RFC-compliant CSV quoting — data preserved |
| **JsonExporter** | Returned empty on null input | Trusts base contract, no special null handling |
| **Contract enforcement** | None (implicit, unenforced) | Explicit: `req`, `title`, `body` must all be non-null |

### Why the Refactor Is Better

| Quality | Explanation |
|---------|-------------|
| **Coupling** | Callers depend only on the `Exporter` base class and its documented contract. They no longer need format-specific workarounds (`try/catch`, `instanceof` checks). Any exporter can be swapped in transparently. |
| **Extensibility** | To add a new exporter (e.g., `XmlExporter`), you only implement `doExport()`. The base class automatically enforces the contract for you — you **cannot** accidentally tighten preconditions because `export()` is `final`. |
| **Testability** | The contract is centralized — test it once in the base class. Each subclass test only needs to verify format-specific output, knowing preconditions are already guaranteed. No need to test null/edge cases in every subclass. |

---
---

## Overall Summary Table

| Question | Exercise | SOLID Principle | Core Problem | Key Fix |
|----------|----------|----------------|--------------|---------|
| **Q1** | Ex1 — Student Onboarding | **SRP** | God method does parsing, validation, ID gen, persistence, printing | Extract each responsibility into its own class + introduce `saves` interface |
| **Q2** | Ex2 — Cafeteria Billing | **SRP** | God method does pricing, tax, discount, formatting, persistence | Extract 5 components with interfaces + constructor injection |
| **Q3** | Ex3 — Eligibility Engine | **OCP** | Hard-coded if/else-if chain for each rule | Extract rules behind `EligibilityRule` interface + iterate over list |
| **Q4** | Ex4 — Hostel Fee Calculator | **OCP** | Switch-case for room types + if/else for add-ons | Replace int constants with `RoomType` enum + embed prices in enums |
| **Q5** | Ex5 — File Exporter | **LSP** | Subclasses tighten preconditions, corrupt data, handle nulls inconsistently | Template Method Pattern: `final export()` enforces contract → subclasses implement `doExport()` |
