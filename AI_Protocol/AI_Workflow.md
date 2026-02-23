# AI Interaction Protocol

**Project:** Budget Management App
**Team:** Jacob,Majo,Agus
**Duration:** 2.5 days  
**Last Updated:** 2026-02-06

---

## 1. Methodology

### Agile-DevOps Hybrid Approach

For this 2.5-day intensive app development project, we combine lightweight Agile practices with DevOps principles:

#### **Agile Adaptation (Scrum-inspired)**

Given our 2.5-day constraint, we adapt Scrum to micro-sprints:

- **Sprint Duration:** 1 day per sprint (2 total sprints)
- **Daily Standup:** 2-3 hour daily meeting (detailed structure in Section 4)
  **Day 1 Sprint Goals:**
- Project setup and architecture
- Time planning and structure of the project

**Day 2 Sprint Goals:**

- Feature completion
- Testing and bug fixes
- Deployment and documentation

#### **DevOps Principles**

- **Continuous Integration:** Commit code frequently, trigger automated checks
- **Continuous Deployment:** Deploy to staging/production environments rapidly
- **Infrastructure as Code:** Version control all configurations
- **Automated Testing:** Integrate tests in CI/CD pipeline
- **Monitoring:** Track app performance and errors from day one

#### **AI Integration**

- GitHub Copilot and Open Code as Junior Developer member of our team
- Apply prompt engineering best practices https://docs.github.com/es/copilot/concepts/prompting/prompt-engineering
- Ethics and Quality Standards (detailed in section 5)
- Document AI interactions for team learning in PROMPT_DOCUMENTATION.md

---

## 2. Key Interactions

### Types of AI Interactions

#### **A. Debugging**

Encountering errors, unexpected behavior, or failing tests

---

#### **B. Code Improvement**

Enhancing code quality, performance, readability, or maintainability

---

#### **C. Documentation**

Writing README files, API documentation, code comments, or user guides

---

#### **D. Testing**

**When to use:** Creating unit tests, integration tests, or end-to-end tests

---

## 3. Key Documents and Context

### A. Project Context Documents

Maintain these key documents in the repository:

```
📁 Budget_Management_App
├── 📂 AI_Protocol/                 # Methodology & AI Guidelines
│   └── 📄 AI_Workflow.md
├── 📂 Automatic Debugging/         # Debugging scripts & reports
│   └── 📄 bug_report.md
├── 📂 CI-CD Pipeline/              # CI/CD Configuration
│   └── 📄 CI_CD_Pipeline.yml
├── 📂 PROMPT_DOCUMENTATION/        # Prompt Library (Frontend, Backend, QA)
│   ├── 📄 Front-End.md
│   ├── 📄 Back-End.md
│   └── 📄 QA.md
├── 📂 Tests/                       # Test Suites (Mapped to .github/workflows)
│   └── 📄 ci.yml
├── 📂 app/                         # Main Application Code
│   ├── 📂 Frontend/                # React Application (Modular Monolith)
│   │   ├── 📂 src/
│   │   ├── 📄 package.json
│   │   ├── 📄 vite.config.ts
│   │   └── 📄 vitest.config.ts
│   └── 📂 backend-microservice/    # Spring Boot Microservices
│       ├── 📂 transaction/         # Transaction Service (Producer)
│       │   ├── 📂 src/
│       │   ├── 📄 pom.xml
│       │   └── 📄 Dockerfile
│       ├── 📂 report/              # Report Service (Consumer)
│       │   ├── 📂 src/
│       │   ├── 📄 pom.xml
│       │   └── 📄 Dockerfile
│       └── 📂 docker-compose/      # Orchestration configs
└── 📄 README.md                    # Project Entry Point
```

---

### B. Context Template for AI Interactions

Before requesting AI assistance, gather this information:

```markdown
## Context for AI Interaction

**Project**: Budget Management App
**Repository**: majoymajo/Budget_Management_App
**Task**: [What you're trying to achieve]
**Current file**: [Full file path]
**Related files**: [Other relevant files]
**Tech Stack**:

- Language: [JAVA]
- Framework: [React, Sprint]
- Broker : Rabbit MQ
- Infra: Docker
  **Constraints**: [Limitations, requirements, deadlines]
  **Previous attempts**: [What you've already tried]
  **Error message** (if applicable): [Full error with stack trace]
```

---

### C. Example of Good Contextualization

**Example 1: Code Generation**

> "In our React Native app (majoymajo/Budget_Management_App), I'm building a product catalog screen. We use React Navigation v6, Redux Toolkit for state, and Styled Components for styling. I need a FlatList component that displays products from the Redux store, includes pull-to-refresh, and handles loading/error states. Each product card should show image, name, price, and rating. Follow our existing component structure in `src/components/`."

**Example 2: Debugging**

> "In our Node.js Express API, the POST /api/users endpoint in `src/routes/userRoutes.js` returns a 500 error when creating users. Error: 'Cannot read property 'hash' of undefined' in `src/controllers/userController.js:23`. We use bcrypt for password hashing and Sequelize ORM with PostgreSQL. Here's the controller code: [code]. Database connection is working for GET requests."

---

## 4. Interaction Dynamics

### A. Project Phases Structure

Our 2.5-day project follows these phases:

#### **Phase 1: Beginning - Project Setup (Day 1, Afternoon)**

**Duration:** 2-3 hours  
**Activities:**

- Environment setup
- Repository initialization
- Architecture design
- Tech stack configuration
- Initial AI prompts

**AI Interaction Focus:**

- Project structure generation
- Configuration file creation
- Setup documentation

---

#### **Phase 2: Code and Develop ( Day 2 Morning)**

**Duration:** 2-5 hours  
**Activities:**

- Feature implementation
- Component/module development
- API integration
- UI/UX implementation
- Continuous integration

**AI Interaction Focus:**

- Code generation
- Problem-solving
- Code improvement
- Documentation

**Development Workflow:**

```
1. 🎯 Define feature/task clearly
2. 📋 Break down into sub-tasks
3. 🔍 Search existing code for patterns
4. 💬 Craft detailed AI prompt with context
5. 🧪 Test AI response in isolation
6. 🔄 Iterate and refine if needed
7. ✅ Validate and integrate
8. 📝 Document and commit
9. 🚀 Push and trigger CI/CD
```

---

#### **Phase 3: Test and Validate (Day 2, Afternoon)**

**Duration:** 3-4 hours  
**Activities:**

- Unit testing
- Integration testing
- Bug fixing
- Performance optimization
- Security review

**AI Interaction Focus:**

- Test generation
- Debugging
- Code improvement
- Security analysis

**Testing Checklist:**

- [ ] All functions have unit tests
- [ ] API endpoints tested
- [ ] Error handling verified
- [ ] Edge cases covered
- [ ] Performance acceptable
- [ ] Security vulnerabilities checked
- [ ] Cross-browser/platform tested (if applicable)

---

#### **Phase 4: Conclusion and Deployment (Day 3, End)**

**Duration:** 1-2 hours  
**Activities:**

- Final bug fixes
- Documentation completion
- Deployment to production
- Project retrospective
- Lessons learned documentation

**AI Interaction Focus:**

- Documentation finalization
- Deployment-Github actions
- Troubleshooting

**Deliverables:**

- [ ] Working application (deployed)
- [ ] Complete documentation
- [ ] Test coverage report
- [ ] Lessons learned document
- [ ] Prompt library with successful patterns

---

#### **Knowledge Sharing**

- **Daily 2-hour meetings** scheduled through Teams
- **Real-time documentation** of effective prompts in PROMPT_DOCUMENTATION.md
- **End-of-project retrospective** capturing lessons learned

## 5. Quality Gate
# SonarCloud Quality Gate in Our CI/CD Workflow

## What is a Quality Gate?
A Quality Gate is an automated policy that prevents code from progressing if it doesn’t meet agreed quality standards (e.g., code coverage, bugs, vulnerabilities, code smells, duplications). We use **SonarCloud** to enforce this gate for both backend and frontend code.

## Where it runs in the pipeline
- Job: `sonarqube` (SonarCloud Analysis)
- It executes after:
  - `backend-test` (Java/Spring unit tests)
  - `frontend-test` (React/TypeScript unit tests)
- The `build` job depends on `sonarqube`. If the Quality Gate fails, `build` is skipped automatically.

## What the job does
- Backend:
  - Runs `mvn sonar:sonar` with:
    - `sonar.projectKey`: `majoymajo_Budget_Management_App`
    - `sonar.organization`: `majoymajo`
    - `sonar.host.url` from the secret `SONAR_HOST_URL`
    - `sonar.login` from the secret `SONAR_TOKEN`
- Frontend:
  - Uses `sonarsource/sonarqube-scan-action@v2` with:
    - `sonar.projectKey`: `majoymajo_Budget_Management_App`
    - `sonar.organization`: `majoymajo`
    - `sonar.sources`: `app/Frontend`
    - Coverage report at `app/Frontend/coverage/lcov.info`
    - Exclusions for `node_modules/**` and `dist/**`
  - Auth via `SONAR_TOKEN` and `SONAR_HOST_URL` secrets
 The `sonarqube` job evaluates code health and coverage against SonarCloud rules.

## Where to check results
- Pipeline Summary: shows “SonarCloud Analysis” status and a dedicated “Quality Gate” section.
- SonarCloud Project: https://sonarcloud.io/project/overview?id=majoymajo_Budget_Management_App
- Pull Request : on the comments sections appear the checks from Sonar Cloud.

## Environment and secrets used
- Env:
  - `SONAR_PROJECT_KEY_BACKEND`: `majoymajo_Budget_Management_App`
  - `SONAR_PROJECT_KEY_FRONTEND`: `majoymajo_Budget_Management_App`
  - `SONAR_ORGANIZATION`: `majoymajo`
- Secrets:
  - `SONAR_HOST_URL`: `https://sonarcloud.io`
  - `SONAR_TOKEN`: token from SonarCloud (“My Account” → “Security”)

---

## 6. Metrics & Continuous Improvement

### A. Metrics

- ⏱️ Time saved per AI interaction
- 🐛 Bugs introduced by AI-generated code vs. human code
- ✅ Successful vs. unsuccessful AI interactions
- 📈 Most effective prompt patterns
- 🎯 AI accuracy by task type

### B. Post-Project Review

After 2-day project completion:

- Identify most/least effective AI use cases
- Document best prompt patterns
- Calculate overall time saved
- Plan improvements for future projects

---

## 7. Resources & References

### Prompt Engineering Resources

- [GitHub Copilot Best Practices](https://docs.github.com/en/copilot/using-github-copilot/best-practices-for-using-github-copilot)
- [Anthropic Prompt Library](https://docs.anthropic.com/claude/prompt-library)

### DevOps Tools

- **CI/CD:** GitHub Actions
- **Testing:**
  - Backend: JUnit 5, Spring Boot Test
  - Frontend: Jest, React Testing Library
  - E2E: Cypress/Selenium
- **Deployment:**
  - Platform: GitHub Actions
  - Infrastructure: Docker
  - Target: [Not specified due to time constraints]
- **Monitoring:**
  - Application: Spring Boot Actuator + Prometheus + Grafana
  - Error Tracking: Sentry
  - Logs: [To be configured]

---
