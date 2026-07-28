# SEAL Hackathon BE - Agent Rules & Project Lessons Learned

## 1. Hibernate Lazy Initialization Boundary (@Transactional)
- **Rule**: ALWAYS annotate Spring `@Service` implementation classes or methods with `@Transactional(readOnly = true)` for query methods, and `@Transactional` for state mutation methods whenever DTO Mappers or service methods access lazy-loaded entity proxy fields (e.g. `roundJudge.getJudge().getFullName()`, `categoryMentor.getMentor().getUserId()`, `event.getCreatedBy()`, `cm.getCategory().getEvent()`).
- **Rationale**: Dereferencing lazy proxies outside an active Hibernate session causes `org.hibernate.LazyInitializationException: Could not initialize proxy [...] - no session`.

## 2. Consultation Request Business Rules
- **Rule**: Restrict `sendMessage()`, `updateTeamMentorNote()`, and `MilestoneService.create()` so they require consultation request status to be `ACCEPTED` or `IN_PROGRESS`. Throw `BAD_REQUEST` if status is `PENDING`, `REJECTED`, or `CANCELLED`.

## 3. Event Participant Access Control
- **Rule**: `SubmissionQueryServiceImpl` verifies `eventParticipantService.assertActiveParticipant(eventId, userId)`. Users must be registered for the event and approved by an organizer (status = `ACTIVE`) before viewing or submitting work for round submissions.
