# Contributing to RashikMart

Thank you for contributing to RashikMart! This guide outlines the exact steps to clone, build, run, and test the project locally.

---

## 1. Prerequisites

Before starting, ensure your local workstation has the following tools installed:
- **Java Development Kit (JDK):** Version 17 LTS (e.g. Eclipse Temurin 17 or OpenJDK 17).
- **Apache Maven:** Version 3.8.0 or later.
- **Git:** Version 2.30 or later.
- **IDE (Recommended):** IntelliJ IDEA, Eclipse, or VS Code.

Verify installation by running:
```bash
java -version
mvn -version
git --version
```

---

## 2. Setup & First Run

### Step 1: Clone the Repository
```bash
git clone https://github.com/rashik/RashikMart.git
cd RashikMart
```

### Step 2: Configure Environment (Optional)
RashikMart includes default configurations that work immediately out-of-the-box with embedded H2 database.
If you wish to test with custom settings or the Gemini AI provider, copy `.env.example`:
```bash
cp .env.example .env
```
Key variables:
- `ADMIN_PASSWORD`: Custom administrator password (defaults to `admin123`).
- `AI_CHATBOT_PROVIDER`: `mock` (default) or `gemini`.
- `GEMINI_API_KEY`: Your Gemini API key if using `gemini` provider.

### Step 3: Compile and Run Tests
```bash
mvn clean verify
```
This executes all 159 automated test cases against a self-contained in-memory H2 database.

### Step 4: Run the Web Application

#### Option A: Docker (Fastest)
```bash
docker build -t rashikmart .
docker run -p 8080:8080 rashikmart
```
Visit `http://localhost:8080/RashikMart` in your browser.

#### Option B: Local Tomcat 9
1. Install Apache Tomcat 9.
2. Copy `target/RashikMart.war` into `$CATALINA_HOME/webapps/`.
3. Start Tomcat via `bin/startup.sh` (or `bin/startup.bat`).
4. Access `http://localhost:8080/RashikMart`.

---

## 3. Coding Guidelines & Ground Rules

1. **Strict SQL Parameterization:**
   - Every database query must use `PreparedStatement` with parameterized placeholders (`?`).
   - String concatenation of SQL is strictly forbidden.
   - Always enclose `Connection`, `PreparedStatement`, and `ResultSet` within `try-with-resources`.

2. **Output Escaping & XSS Protection:**
   - In JSP views, always escape user-controlled data with `<%= HtmlUtil.escape(...) %>` or `<c:out>`.
   - Never use raw unescaped scriptlet output.
   - On the frontend, always use `element.textContent` rather than `element.innerHTML` for user content.

3. **CSRF Protection:**
   - All state-changing `POST` requests must validate the session CSRF token via `CsrfUtil.isValid(request)`.

4. **Git Workflow:**
   - Use feature branches: `feature/<name>`.
   - Conventional commit messages only (`feat:`, `fix:`, `test:`, `docs:`, `chore:`).
   - Ensure `mvn clean verify` passes before submitting pull requests.
