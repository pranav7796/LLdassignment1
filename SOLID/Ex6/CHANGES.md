# Changes Summary — Ex6 (LSP: Notification Sender)

## 1. NotificationSender.java — Base class
**Before:** `send()` was abstract — no rules, subclasses could do anything.

**After:**
- `send()` is now **concrete and `final`** — it checks `n != null` and `body != empty`, then calls `doSend()`
- Added `protected abstract void doSend()` — the new extension point for subclasses
- This is the **Template Method Pattern** — base class owns the contract, subclasses only provide channel-specific logic

---

## 2. EmailSender.java — Email channel
**Before:** Overrode `send()`, silently truncated body to 40 chars (LSP violation — changed meaning).

**After:**
- Renamed `send()` → `doSend()` (protected)
- Removed truncation — body is sent as-is
- No more silent surprises

---

## 3. SmsSender.java — SMS channel
**Before:** Overrode `send()`.

**After:**
- Renamed `send()` → `doSend()` (protected)
- No other changes — ignoring subject is fine since SMS doesn't support it

---

## 4. WhatsAppSender.java — WhatsApp channel
**Before:** Overrode `send()`, threw `IllegalArgumentException` if phone didn't start with `+` (LSP violation — tightened precondition).

**After:**
- Renamed `send()` → `doSend()` (protected)
- Replaced `throw` with `print message + return` — handles bad phone **gracefully**
- No more exceptions thrown to caller

---

## 5. Main.java — Client code
**Before:** Had `try/catch` around `wa.send()` — proof that LSP was broken (had to know it was WhatsApp).

**After:**
- Removed `try/catch` — all three senders are called the same way: just `send(n)`
- **This is the proof LSP works** — Main doesn't need to know which sender it's talking to

---

## Key talking points
1. **Problem:** Subclasses broke the parent's contract (throwing, truncating, ignoring fields)
2. **Fix:** Template Method Pattern — `send()` (final, with rules) → `doSend()` (abstract, for subclass logic)
3. **Result:** Any `NotificationSender` can be swapped in without surprises — that's **Liskov Substitution Principle**
