---
name: 🐛 Automated Bug Report
about: Auto-generated bug report from CI/CD pipeline failures
title: "🐛 [AUTO] Pipeline Failure"
labels: ['bug', 'automated-report']
---

<!-- This is an automatically generated bug report from the CI/CD pipeline -->

**Status:** 🔴 FAILED

## Pipeline Information
- **Workflow:** [Link to workflow run]
- **Branch:** [Branch name]
- **Commit:** [Commit SHA]
- **Author:** [Author handle]
- **Timestamp:** [ISO timestamp]

## Failed Jobs
<!-- List of failed jobs -->
- [ ] Validate Code Quality
- [ ] Backend Tests (Java/Spring)
- [ ] Frontend Tests (React/TypeScript)
- [ ] Integration Tests
- [ ] Security Scan
- [ ] Build
- [ ] Deploy

## Error Details
<!-- Detailed error information -->
```
Paste the error output here
```

## Debugging Information
<!-- Steps to reproduce and debug locally -->
1. Checkout the branch mentioned above
2. Run the failed job locally
3. Review the error logs

### Local Reproduction
```bash
# For backend issues
cd app/backend-microservice/transaction && mvn clean test

# For frontend issues
cd app/Frontend && pnpm install && pnpm test

# For integration test issues
cd app/backend-microservice/transaction && mvn verify -DskipUnitTests=true
```

## Related Resources
- [Workflow Artifacts](link-to-artifacts)
- [Test Results](link-to-test-results)
