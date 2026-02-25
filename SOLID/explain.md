
Let me quickly check your Ex6 changes too so I can cover all 6.

Here's your viva cheat sheet — memorize the 3-line formula for each question so you can answer confidently no matter which one you're asked.

The Formula (use for every question)
"The problem was ___, the principle violated was ___, and I fixed it by ___."

Then if the teacher probes deeper, you expand with before → after → why it's better.

Q1 — Student Onboarding (SRP)
One-liner: "The OnboardingService was a god method doing 5 things — parsing, validation, ID generation, persistence, and printing. That violates SRP because it has 5 reasons to change."

What you did: "I extracted each responsibility into its own class — parses, validates, createid, prints — and introduced a saves interface so the service doesn't depend on FakeDb directly."

If asked "why is this better?": "Now I can change validation rules without risking the printing logic. I can unit test each piece independently. And I can swap the database by implementing saves differently."

Q2 — Cafeteria Billing (SRP)
One-liner: "Same principle — CafeteriaSystem.checkout() was doing pricing, tax, discount, formatting, AND persistence in one method."

What you did: "I extracted PricingService, TaxCalculator, DiscountCalculator, InvoiceFormatter, and introduced interfaces for all of them (IPricingService, ITaxCalculator, etc.). Everything is injected via constructor."

If asked "why interfaces?": "So I can swap implementations without touching CafeteriaSystem. For example, a different tax policy = new ITaxCalculator implementation. Also makes unit testing easy — I can mock any dependency."

Q3 — Eligibility Engine (OCP)
One-liner: "evaluate() had a hard-coded if/else-if chain for 4 rules. Adding a new rule means editing that method — that's an OCP violation."

What you did: "I created an EligibilityRule interface with a check() method. Each rule (attendance, CGR, credits, disciplinary flag) is its own class. The engine just iterates over a List<EligibilityRule>."

If asked "show me how to add a new rule": "I create BacklogRule implements EligibilityRule, implement check(), and add it to the list. Zero changes to EligibilityEngine — that's OCP."

Q4 — Hostel Fee Calculator (OCP)
One-liner: "Room pricing used a switch on int constants, add-ons used if/else. Both break OCP."

What you did: "I replaced LegacyRoomTypes (int constants) with a RoomType enum that carries its own price — DOUBLE(15000.0). Same for AddOn — MESS(1000.0). Now the calculator just calls getBasePrice() and getPrice() — no switch, no if/else."

If asked "what's primitive obsession?": "Using int roomType = 2 instead of a type-safe enum. With int, I can pass 99 and it compiles fine but crashes at runtime. With enum, the compiler catches it."

Q5 — File Exporter (LSP)
One-liner: "Three subclasses broke the parent contract in three different ways — PdfExporter threw for body > 20 chars (tightened precondition), CsvExporter silently stripped commas and newlines (corrupted data), and JsonExporter returned empty for null instead of failing (inconsistent behavior)."

What you did: "I used the Template Method Pattern — made export() final in the base class with precondition checks (null validation), then added protected abstract doExport() for subclasses. Subclasses can't skip validation and can't tighten preconditions."

Key phrase to say: "The proof LSP works is the caller code — e.export(req) works the same regardless of which exporter it is. No try/catch, no instanceof."

Q6 — Notification Sender (LSP)
One-liner: "Same principle as Q5. EmailSender silently truncated body to 40 chars (changed meaning), WhatsAppSender threw an exception if phone didn't start with + (tightened precondition). Main had a try/catch around only WhatsApp — proof LSP was broken."

What you did: "Same fix — Template Method Pattern. send() is now final in NotificationSender, checks n != null and body not empty, then calls protected abstract dosend(). EmailSender no longer truncates. WhatsAppSender prints a message instead of throwing."

If asked "how do you know LSP is fixed?": "Look at Main — all three senders are called the same way: just sender.send(n). No try/catch, no special handling. That's substitutability."

Common Follow-up Questions & Answers
Question	Answer
"What is SRP?"	A class should have only one reason to change. If I change tax rules, I shouldn't be touching invoice formatting code.
"What is OCP?"	Open for extension, closed for modification. I can add new behavior (new rule, new room type) without editing existing code.
"What is LSP?"	If I replace a parent with any of its children, the program should still work correctly. No surprises, no crashes, no data loss.
"What is Template Method Pattern?"	Base class has a final method with the contract (validation), then calls a protected abstract method that subclasses implement. Base owns the rules, subclasses own the specifics.
"What is DI (Dependency Injection)?"	Instead of creating dependencies inside the class (new FakeDb()), I pass them through the constructor. This lets me swap implementations and mock for testing.
"Why interfaces over concrete classes?"	Loose coupling. The class depends on what something does (interface), not how it does it (implementation). Makes testing and future changes easy.
"What's the difference between Q5 and Q6?"	Same principle (LSP), same fix (Template Method). Q5 is about data export formats, Q6 is about notification channels. The pattern is identical — that's the point.
Pro Tip for the Viva
Start every answer with: "The original code violated [principle] because..." — this immediately shows you understand the why, not just the what. Teachers care more about why you made a change than the code itself.