# SOLID Assignment — Design Analysis & Class Diagrams (Q1–Q5)

---

## Exercise 1 — Student Onboarding

### Design Issue: **SRP Violation + Tight Coupling**

`OnboardingService` had **5 reasons to change**: parsing logic, validation rules, ID format, persistence mechanism, and output format. It was also coupled to the concrete `FakeDb` class.

### Proposed (Implemented) Design

```
┌─────────────────────────────────────────────────────────────────┐
│                        <<interface>>                            │
│                            saves                                │
│                  ┌──────────────────────┐                       │
│                  │ + save(StudentRecord) │                       │
│                  │ + count(): int        │                       │
│                  └──────────┬───────────┘                       │
│                             △                                   │
│                             │ implements (is-a)                  │
│                  ┌──────────┴───────────┐                       │
│                  │       FakeDb         │                       │
│                  │──────────────────────│                       │
│                  │ - rows: List<>       │                       │
│                  │ + save()             │                       │
│                  │ + count()            │                       │
│                  │ + all()              │                       │
│                  └──────────────────────┘                       │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              OnboardingService (orchestrator)            │   │
│  │──────────────────────────────────────────────────────────│   │
│  │ - db: saves            ◆────── depends-on saves          │   │
│  │ - print: prints        ◆────── has-a prints              │   │
│  │──────────────────────────────────────────────────────────│   │
│  │ + registerFromRawInput(String)                           │   │
│  │   internally uses:                                       │   │
│  │     parses     ◇─── has-a (creates locally)              │   │
│  │     validates   ◇─── has-a (creates locally)              │   │
│  │     createid    ◇─── has-a (creates locally)              │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐   │
│  │   parses   │ │ validates  │ │  createid   │ │   prints   │   │
│  │────────────│ │────────────│ │────────────│ │────────────│   │
│  │+ parse()   │ │+ validate()│ │+ generate()│ │+ printInput│   │
│  │  :Map      │ │  :List     │ │  :String   │ │+ printError│   │
│  └────────────┘ └────────────┘ └────────────┘ │+ printConf │   │
│                                                └────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### Relationships & Why They Exist

| Relationship | Type | Why It Exists |
|---|---|---|
| `FakeDb` → `saves` | **is-a** (implements) | Decouples persistence; `OnboardingService` talks to the interface, not the implementation. Enables swapping to a real DB without touching the service. |
| `OnboardingService` → `saves` | **depends-on** (injected) | The service needs to persist data but shouldn't know *how*. Injected via constructor = **DIP compliance**. |
| `OnboardingService` → `prints` | **has-a** (composition) | Printing is a separate concern. Extracted so output format changes don't touch business logic. |
| `OnboardingService` → `parses` | **has-a** (local creation) | Parsing raw strings is not the service's job. Isolated so parsing format changes don't affect validation/persistence. |
| `OnboardingService` → `validates` | **has-a** (local creation) | Validation rules change independently of orchestration. Testable in isolation. |
| `OnboardingService` → `createid` | **has-a** (local creation) | ID generation strategy may change (UUID, sequence, etc.). Isolated from business flow. |

### What Breaks If Structured Differently?

| Alternative Structure | What Breaks |
|---|---|
| Keep all logic in one method | Any change (new field, new validation rule, new output format) forces editing the same 40-line method. Unit testing requires running the full flow. |
| Use `FakeDb` directly (no interface) | Can't swap to a real DB or mock for tests. `OnboardingService` is permanently tied to in-memory storage. |
| Make `prints` methods static | Can't substitute a different printer (e.g., file logger) for testing or different environments. |

---

## Exercise 2 — Cafeteria Billing

### Design Issue: **SRP Violation + Concrete Coupling**

`CafeteriaSystem.checkout()` mixed pricing, tax, discount, formatting, and persistence — **5 responsibilities**. All dependencies were concrete classes.

### Proposed (Implemented) Design

```
┌──────────────────────────────────────────────────────────────────────┐
│                                                                      │
│  <<interface>>        <<interface>>        <<interface>>              │
│  IPricingService      ITaxCalculator       IDiscountCalculator       │
│  ┌──────────────┐     ┌──────────────┐     ┌──────────────────┐      │
│  │+calcSubtotal │     │+getTaxPct    │     │+calculate        │      │
│  │+getPricedLns │     │+calculate    │     └────────┬─────────┘      │
│  └──────┬───────┘     └──────┬───────┘              △                │
│         △                    △                      │ implements     │
│         │ implements         │ implements            │                │
│  ┌──────┴───────┐     ┌──────┴───────┐     ┌───────┴─────────┐      │
│  │PricingService│     │TaxCalculator │     │DiscountCalculator│      │
│  │──────────────│     │──────────────│     │──────────────────│      │
│  │- menu: Map   │     │              │     │                  │      │
│  └──────────────┘     └──────────────┘     └──────────────────┘      │
│                                                                      │
│  <<interface>>              <<interface>>                             │
│  IInvoiceFormatter          InvoiceRepository                        │
│  ┌──────────────────┐       ┌──────────────────┐                     │
│  │ +format():String │       │ +save()           │                     │
│  └────────┬─────────┘       │ +countLines():int │                     │
│           △                 └────────┬──────────┘                     │
│           │ implements               △                                │
│  ┌────────┴─────────┐       ┌────────┴──────────┐                     │
│  │InvoiceFormatter   │       │    FileStore       │                     │
│  └──────────────────┘       └───────────────────┘                     │
│                                                                      │
│  ┌───────────────────────────────────────────┐                       │
│  │         CafeteriaSystem (orchestrator)     │                       │
│  │───────────────────────────────────────────│                       │
│  │ ◆── pricing:    IPricingService           │  all injected via     │
│  │ ◆── taxCalc:    ITaxCalculator            │  constructor (DI)     │
│  │ ◆── discountCalc: IDiscountCalculator     │                       │
│  │ ◆── formatter:  IInvoiceFormatter         │                       │
│  │ ◆── repo:       InvoiceRepository         │                       │
│  │───────────────────────────────────────────│                       │
│  │ + checkout(): InvoiceResult               │                       │
│  └───────────────────────────────────────────┘                       │
│                                                                      │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐            │
│  │  PricedLine   │  │ InvoiceResult │  │   OrderLine   │  (data)    │
│  └───────────────┘  └───────────────┘  └───────────────┘            │
└──────────────────────────────────────────────────────────────────────┘
```

### Relationships & Why They Exist

| Relationship | Type | Why It Exists |
|---|---|---|
| `CafeteriaSystem` → `IPricingService` | **depends-on** (injected) | Pricing logic (how line totals are computed) should be swappable. Maybe bulk pricing or seasonal pricing later. |
| `CafeteriaSystem` → `ITaxCalculator` | **depends-on** (injected) | Tax rules vary by region/policy. Interface allows plugging in different tax strategies without modifying the system. |
| `CafeteriaSystem` → `IDiscountCalculator` | **depends-on** (injected) | Discount policies change frequently (festive, loyalty). Isolated so new policies don't touch billing orchestration. |
| `CafeteriaSystem` → `IInvoiceFormatter` | **depends-on** (injected) | Output format (plain text, HTML, PDF) is independent of billing logic. Separation enables multiple output formats. |
| `CafeteriaSystem` → `InvoiceRepository` | **depends-on** (injected) | Persistence mechanism (file, DB, cloud) is independent. Mockable for tests. |
| `FileStore` → `InvoiceRepository` | **is-a** (implements) | Concrete persistence strategy. Can be replaced with `DbStore`, `S3Store`, etc. |
| `PricingService` → `Map<MenuItem>` | **has-a** (injected) | Needs menu data to compute prices. Injected so the same service works with different menus. |

### What Breaks If Structured Differently?

| Alternative Structure | What Breaks |
|---|---|
| Keep everything in `checkout()` (original) | Adding HTML invoice format = editing the same method that also does tax math. A tax rule bug fix risks breaking the invoice text. |
| Use concrete classes instead of interfaces | Can't mock for unit tests. `CafeteriaSystem` test requires a real `FileStore`, real `PricingService`, etc. Test failures become ambiguous — is it a pricing bug or a formatting bug? |
| Skip `InvoiceResult` data carrier | `checkout()` must both print and return data, tangling I/O with business logic. Caller can't decide how/when to display. |
| Make `TaxCalculator` call `TaxRules` directly with no interface | Works, but `CafeteriaSystem` can't use a different tax strategy (e.g., GST vs VAT) at runtime. |

---

## Exercise 3 — Placement Eligibility Engine

### Design Issue: **OCP Violation**

`EligibilityEngine.evaluate()` used a 4-branch `if/else-if` chain. Every new rule = edit the method = risk breaking existing rules.

### Proposed (Implemented) Design

```
┌───────────────────────────────────────────────────────────────────┐
│                                                                   │
│                       <<interface>>                               │
│                      EligibilityRule                              │
│                  ┌───────────────────┐                            │
│                  │ +check(profile)   │                            │
│                  │  : RuleResult     │                            │
│                  └─────────┬─────────┘                            │
│                            △                                      │
│             ┌──────────────┼──────────────┬──────────────┐        │
│             │ implements   │ implements   │ implements   │        │
│   ┌─────────┴────┐ ┌──────┴──────┐ ┌────┴────────┐ ┌───┴─────┐  │
│   │Disciplinary  │ │CgrThreshold │ │Attendance   │ │Credits  │  │
│   │FlagRule      │ │Rule         │ │Rule         │ │Rule     │  │
│   └──────────────┘ └─────────────┘ └─────────────┘ └─────────┘  │
│                                                                   │
│   ┌─────────────────────────────────────────────────────┐         │
│   │            EligibilityEngine (orchestrator)         │         │
│   │─────────────────────────────────────────────────────│         │
│   │ ◆── store: FakeEligibilityStore    (has-a)          │         │
│   │ ◆── rules: List<EligibilityRule>   (has-a, list)    │         │
│   │─────────────────────────────────────────────────────│         │
│   │ + evaluate(profile): Result                         │         │
│   │   for each rule in rules:     ←── ITERATE, not IF   │         │
│   │       rule.check(profile)                           │         │
│   │ + runAndPrint(profile)                              │         │
│   └──────────────────────┬──────────────────────────────┘         │
│                          │ uses                                    │
│                          ▼                                         │
│               ┌────────────────────┐                              │
│               │   ReportPrinter    │                              │
│               │ + print(s, result) │                              │
│               └────────────────────┘                              │
│                                                                   │
│   ┌─────────────┐                                                 │
│   │ RuleResult  │  (data carrier)                                 │
│   │─────────────│                                                 │
│   │ passed: bool│                                                 │
│   │ reason: Str │                                                 │
│   └─────────────┘                                                 │
└───────────────────────────────────────────────────────────────────┘

Adding a new rule (e.g., BacklogRule):

  ┌────────────┐
  │ BacklogRule │ implements EligibilityRule
  └────────────┘
  Just add to the rules list. EligibilityEngine: UNTOUCHED.
```

### Relationships & Why They Exist

| Relationship | Type | Why It Exists |
|---|---|---|
| `DisciplinaryFlagRule` → `EligibilityRule` | **is-a** (implements) | Polymorphism: engine iterates over rules without knowing which specific rule it's calling. |
| `CgrThresholdRule` → `EligibilityRule` | **is-a** (implements) | Same — each rule is independently defined, testable, and pluggable. |
| `EligibilityEngine` → `List<EligibilityRule>` | **has-a** (composition) | Engine holds a collection of rules. The list is the extension point — add/remove rules here. |
| `EligibilityEngine` → `FakeEligibilityStore` | **has-a** (injected) | Persistence of evaluation results. Could be abstracted to an interface for further decoupling. |
| `EligibilityEngine` → `ReportPrinter` | **depends-on** (local) | Printing is separate from evaluation logic. |
| Each rule → `StudentProfile` | **depends-on** (parameter) | Rules read student data to make decisions. They don't modify it. |

### What Breaks If Structured Differently?

| Alternative Structure | What Breaks |
|---|---|
| Keep if/else chain (original) | Adding "backlog count" rule = edit `evaluate()`. If you accidentally change an existing condition, all other rules might break. No way to test one rule in isolation. |
| Use inheritance instead of interface | Java is single-inheritance. If rules need to extend another class later, you're stuck. Interfaces are more flexible. |
| Don't use `RuleResult`, return boolean | You lose the `reason` string. The engine would need to know *why* a rule failed, coupling it back to rule internals. |
| Don't provide overloaded constructor | Can't inject custom rules for testing. Test must always run all 4 rules — can't test engine logic with a single mock rule. |

---

## Exercise 4 — Hostel Fee Calculator

### Design Issue: **OCP Violation (switch-case + if/else) + Primitive Obsession**

Room types were `int` constants with a `switch` to map to prices. Add-ons used an `if/else` chain. Both required modification to add new types.

### Proposed (Implemented) Design

```
┌──────────────────────────────────────────────────────────────────┐
│                                                                  │
│  <<enum>>                       <<enum>>                         │
│  RoomType                       AddOn                            │
│  ┌──────────────────────┐       ┌──────────────────────┐         │
│  │ SINGLE  (14000.0)    │       │ MESS     (1000.0)    │         │
│  │ DOUBLE  (15000.0)    │       │ LAUNDRY  (500.0)     │         │
│  │ TRIPLE  (12000.0)    │       │ GYM      (300.0)     │         │
│  │ DELUXE  (16000.0)    │       ├──────────────────────│         │
│  ├──────────────────────│       │ + getPrice(): double │         │
│  │ + getBasePrice(): dbl│       └──────────────────────┘         │
│  └──────────────────────┘                                        │
│     Data lives WITH the type — no external switch needed.        │
│                                                                  │
│  ┌──────────────────────┐       <<interface>>                    │
│  │   BookingRequest     │       BookingRepo                      │
│  │──────────────────────│       ┌──────────────────┐             │
│  │ roomType: RoomType   │       │ +save(id,req,    │             │
│  │ addOns: List<AddOn>  │       │   monthly,deposit)│             │
│  └──────────────────────┘       └────────┬─────────┘             │
│                                          △                        │
│                                          │ implements             │
│                                 ┌────────┴─────────┐             │
│                                 │  FakeBookingRepo  │             │
│                                 └──────────────────┘             │
│                                                                  │
│  ┌──────────────────────────────────────────────────────┐        │
│  │          HostelFeeCalculator (orchestrator)          │        │
│  │──────────────────────────────────────────────────────│        │
│  │ ◆── repo: BookingRepo           (depends-on, DI)    │        │
│  │──────────────────────────────────────────────────────│        │
│  │ + process(req)                                       │        │
│  │ - calculateMonthly(req):                             │        │
│  │     base = req.roomType.getBasePrice()  ← no switch  │        │
│  │     for each addOn: add += a.getPrice() ← no if/else│        │
│  └──────────────────────────────────────────────────────┘        │
│                          │ uses                                   │
│                          ▼                                        │
│               ┌────────────────────┐                             │
│               │   ReceiptPrinter   │ (static helper)             │
│               └────────────────────┘                             │
└──────────────────────────────────────────────────────────────────┘

Adding SUITE room:   RoomType enum  →  SUITE(20000.0)    ← 1 line
Adding WIFI add-on:  AddOn enum     →  WIFI(200.0)       ← 1 line
HostelFeeCalculator: ZERO changes.
```

### Relationships & Why They Exist

| Relationship | Type | Why It Exists |
|---|---|---|
| `RoomType` (enum with data) | **self-contained** | Each room type carries its own price. Eliminates the need for an external switch. Polymorphic via `getBasePrice()`. |
| `AddOn` (enum with data) | **self-contained** | Same — each add-on knows its price. The calculator just calls `getPrice()`. |
| `BookingRequest` → `RoomType` | **has-a** (composition) | Request describes *what* was booked. Type-safe (can't pass invalid int). |
| `BookingRequest` → `List<AddOn>` | **has-a** (composition) | Request lists selected add-ons. |
| `HostelFeeCalculator` → `BookingRepo` | **depends-on** (injected) | Persistence is decoupled. Mockable. Could switch to real DB. |
| `FakeBookingRepo` → `BookingRepo` | **is-a** (implements) | Concrete in-memory implementation. |
| `HostelFeeCalculator` → `ReceiptPrinter` | **depends-on** (static call) | Printing is separated from calculation. |

### What Breaks If Structured Differently?

| Alternative Structure | What Breaks |
|---|---|
| Keep `int` room types + switch (original) | Adding a new room type requires editing the switch in `calculateMonthly()`. Miss a case = silent bug (falls to `default`). No compile-time safety — `new BookingRequest(99, ...)` compiles fine but crashes at runtime. |
| Keep plain `AddOn` enum without prices | Calculator must maintain a parallel if/else chain. Add-on and its price are defined in different places — easy to forget one. |
| Use `FakeBookingRepo` directly (no interface) | Can't mock persistence in tests. Can't swap to real DB without editing `HostelFeeCalculator`. |
| Use abstract class instead of enum | Enums are simpler and guarantee a fixed set of constants. Abstract class would allow arbitrary instantiation, defeating type safety. |

---

## Exercise 5 — File Exporter Hierarchy

### Design Issue: **LSP Violation (3 different ways)**

Subclasses of `Exporter` broke the parent's implicit contract:
- `PdfExporter` **tightened preconditions** (rejected body > 20 chars)
- `CsvExporter` **weakened postconditions** (silently corrupted data)
- `JsonExporter` **inconsistent behavior** (returned empty on null instead of failing)

### Proposed (Implemented) Design

```
┌───────────────────────────────────────────────────────────────────────┐
│                                                                       │
│                    <<abstract>>                                       │
│                     Exporter                                          │
│  ┌───────────────────────────────────────────────────────────┐        │
│  │ + export(req): ExportResult          ← FINAL (template)  │        │
│  │   ┌─────────────────────────────────────────────────┐     │        │
│  │   │ if req == null  → throw IllegalArgument         │     │        │
│  │   │ if title == null → throw IllegalArgument        │     │        │
│  │   │ if body == null  → throw IllegalArgument        │     │        │
│  │   │ return doExport(req)  ← delegate to subclass    │     │        │
│  │   └─────────────────────────────────────────────────┘     │        │
│  │                                                           │        │
│  │ # doExport(req): ExportResult        ← PROTECTED ABSTRACT│        │
│  │   (subclasses implement this only)                        │        │
│  └───────────────────────────────────────────────────────────┘        │
│                            △                                          │
│           ┌────────────────┼────────────────┐                         │
│           │ extends        │ extends        │ extends                 │
│  ┌────────┴──────┐ ┌──────┴───────┐ ┌──────┴───────┐                 │
│  │  PdfExporter  │ │ CsvExporter  │ │ JsonExporter │                 │
│  │───────────────│ │──────────────│ │──────────────│                 │
│  │# doExport()   │ │# doExport()  │ │# doExport()  │                 │
│  │               │ │              │ │              │                 │
│  │ No size limit │ │ Proper CSV   │ │ No null check│                 │
│  │ (was: >20     │ │ quoting      │ │ (trusts base │                 │
│  │  threw error) │ │ (was: lossy  │ │  contract)   │                 │
│  │               │ │  stripping)  │ │              │                 │
│  └───────────────┘ └──────────────┘ └──────────────┘                 │
│                                                                       │
│  KEY: Subclasses CANNOT skip validation because export() is final.    │
│       They CANNOT tighten preconditions — base already validated.     │
│       They MUST return valid ExportResult — postcondition preserved.  │
│                                                                       │
│  ┌──────────────┐     ┌──────────────┐                                │
│  │ExportRequest │     │ExportResult  │  (data carriers)               │
│  │──────────────│     │──────────────│                                │
│  │ title: String│     │ contentType  │                                │
│  │ body: String │     │ bytes: byte[]│                                │
│  └──────────────┘     └──────────────┘                                │
└───────────────────────────────────────────────────────────────────────┘

  Caller code:
  ┌─────────────────────────────────────────────────┐
  │  Exporter e = anyExporter;   // don't care which│
  │  ExportResult r = e.export(req);  // SAFE always│
  │  // No try/catch needed                         │
  │  // No instanceof check needed                  │
  │  // That's LSP.                                 │
  └─────────────────────────────────────────────────┘
```

### Relationships & Why They Exist

| Relationship | Type | Why It Exists |
|---|---|---|
| `PdfExporter` → `Exporter` | **is-a** (extends) | All exporters share the same contract. Callers treat them uniformly via `Exporter` reference. |
| `CsvExporter` → `Exporter` | **is-a** (extends) | Same — substitutability. Any `Exporter` ref works identically regardless of concrete type. |
| `JsonExporter` → `Exporter` | **is-a** (extends) | Same. |
| `Exporter.export()` → `doExport()` | **Template Method** | Base class owns the **contract** (precondition checks). Subclasses own the **format-specific logic**. This split is what enforces LSP. |
| All exporters → `ExportRequest` | **depends-on** (parameter) | Input data. Contract guarantees non-null fields by the time `doExport()` runs. |
| All exporters → `ExportResult` | **depends-on** (return) | Output data. Every exporter must produce this — consistent postcondition. |

### What Breaks If Structured Differently?

| Alternative Structure | What Breaks |
|---|---|
| Keep `export()` abstract (original) | Nothing stops a subclass from throwing for valid input (PdfExporter), silently corrupting data (CsvExporter), or handling null differently (JsonExporter). **LSP violated** — callers can't trust `Exporter` references. |
| Don't make `export()` final | A subclass could override it and skip the null checks, recreating the original problem. `final` is the enforcement mechanism. |
| Use `if/instanceof` in caller | Defeats polymorphism entirely. Every time you add a new exporter, you must update every caller's instanceof chain. **OCP violated.** |
| Throw exceptions from subclass for unsupported content | Caller must know which exporter it's using to handle format-specific exceptions. That's the definition of LSP violation — the subtype isn't substitutable. |
| Put validation in each subclass independently | Duplicated logic, inconsistent behavior. One subclass might forget a check. Centralized in base = **single source of truth**. |

---

## Quick-Reference: Relationship Legend

```
  △          is-a (inheritance / implements)
  ◆────      has-a (composition, owned, injected)
  ◇────      has-a (composition, locally created)
  ──▶        depends-on (uses / calls)
```
