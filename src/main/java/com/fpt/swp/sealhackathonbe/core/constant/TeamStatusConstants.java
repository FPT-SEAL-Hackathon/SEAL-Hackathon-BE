package com.fpt.swp.sealhackathonbe.core.constant;

import java.util.UUID;

public final class TeamStatusConstants {
    public static final UUID DRAFT = UUID.fromString("60000000-0000-0000-0000-000000000001");
    public static final UUID PENDING = UUID.fromString("60000000-0000-0000-0000-000000000002");
    public static final UUID APPROVED = UUID.fromString("60000000-0000-0000-0000-000000000003");
    public static final UUID DISQUALIFIED = UUID.fromString("60000000-0000-0000-0000-000000000004");

    private TeamStatusConstants() {}
}
