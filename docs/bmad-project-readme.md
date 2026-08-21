# BMAD Project Artifacts

This file group stores the working artifacts for the smart-glasses prototype built on top of `samples/CameraAccess`.

The structure is intentionally lightweight but follows the BMAD documentation pattern:

- `docs/bmad-project-context.md` - technical context, constraints, and implementation rules
- `docs/bmad-project-brief.md` - the master product and technical intent for the prototype
- `docs/bmad-implementation-roadmap.md` - staged execution order
- `docs/bmad-backlog.md` - feature list and current status
- `docs/bmad-decision-log.md` - durable architectural and scope decisions
- `docs/bmad-feature-template.md` - reusable template for future iterations
- `docs/bmad-feature-F001-voice-foundation.md` - first feature record

Workflow for each feature:

1. Copy the feature template into a new `docs/bmad-feature-FXXX-*.md`
2. Fill in the goal, acceptance checks, dependencies, and test plan
3. Implement the feature
4. Record test evidence and result
5. Record reusable knowledge and decisions back into the decision log or project context

This gives the project a persistent delivery trail so validated functionality can be reused later without rediscovering the same implementation details.
