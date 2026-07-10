package com.fpt.swp.sealhackathonbe.core.constant;

import java.util.UUID;

public final class SubmissionStatusConstants {
    public static final UUID DRAFT = UUID.fromString("50000000-0000-0000-0000-000000000001");
    public static final UUID SUBMITTED = UUID.fromString("50000000-0000-0000-0000-000000000002");
    public static final UUID UNDER_REVIEW = UUID.fromString("50000000-0000-0000-0000-000000000003");
    public static final UUID DISQUALIFIED = UUID.fromString("50000000-0000-0000-0000-000000000004");
    public static final UUID SCORED = UUID.fromString("50000000-0000-0000-0000-000000000005");
    public static final UUID IN_PROGRESS = UUID.fromString("50000000-0000-0000-0000-000000000006");

    private SubmissionStatusConstants() {}
}
