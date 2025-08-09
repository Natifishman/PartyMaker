# 🤖 PolishBot - Final Code Polish Before Deploy

This document outlines the full polishing process that **must be applied to the codebase** before any deploy, to ensure the app is clean, efficient, and production-ready.

---

## ✅ GOALS

- Produce **Clean Code** across all classes.
- Eliminate **warnings**, unused code, dead references, and poor naming.
- Ensure all files, functions, and resources are **linked or removed**.
- Apply **code architecture standards** (MVVM, SOLID, DRY).
- Perform static analysis and style formatting.
- Leave zero manual polishing tasks before deploy.

---

## 🔄 PROCESS OVERVIEW

### 1. 🧹 Code Cleanup
- Remove:
    - Unused imports, variables, methods, classes.
    - Redundant expressions.
    - Dead layout files or drawables.
- Optimize:
    - Imports (`Organize Imports`)
    - Code structure (`Reformat Code` in IDE or via CLI)

---

### 2. 📐 Naming & Readability
- Rename:
    - Variables with unclear or misleading names.
    - Functions to follow `camelCase`, be descriptive and action-based.
- Split:
    - Long methods (>25 lines) into logical sub-methods.
- Add:
    - Comments where logic is non-obvious.
    - Javadoc/KDoc for public methods.

---

### 3. 🔗 Linking Verification
- Ensure:
    - All layouts are used or deleted.
    - All Activities/Fragments are registered and reachable.
    - All navigation paths are valid.
    - All strings are localized or removed if unused.

---

### 4. ⚙️ Architecture Validation
- Check MVVM separation:
    - View: XML/UI logic only
    - ViewModel: no context, no UI code
    - Repository: handles data sources only
- Respect SOLID principles.

---

### 5. 🔬 Static Analysis
- Run tools:
    - `./gradlew lint`
    - `./gradlew spotlessApply`
    - `./gradlew build` (no warnings or failures)
- Use (if available):
    - SonarLint
    - PMD or Checkstyle

---

### 6. ✨ Code Style & Format
- Apply code style:
    - Google Java Style / Android Developer Guide
- Remove magic numbers – replace with constants
- Align XML attributes properly
- Apply consistent spacing, line breaks, and braces

---

### 7. 🧪 Test Readiness
- Confirm:
    - All test classes are passing
    - No ignored or commented-out tests
    - No TODOs left in code

---

## 🚀 Final Check

| Checkpoint                             | Status |
|----------------------------------------|--------|
| Code builds with no warnings or errors | ✅      |
| No unused declarations or resources    | ✅      |
| Clean, readable, well-structured code  | ✅      |
| All tools (lint, spotless, etc.) pass  | ✅      |
| Code is ready for production           | ✅      |

---

## 🧠 TIP FOR CLAUDE / LLMs

> Please review the following code/file and perform a full polish:
> - Fix all warnings and unused elements
> - Improve naming and readability
> - Ensure all parts are clean, consistent, and follow Clean Code practices
> - Provide comments where needed
> - Follow Android MVVM structure and best practices

---

**When this is done, the codebase should be production-ready. No manual cleanup should be needed after this phase.**