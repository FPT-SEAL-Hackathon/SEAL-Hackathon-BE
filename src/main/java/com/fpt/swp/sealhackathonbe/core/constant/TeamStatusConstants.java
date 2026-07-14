package com.fpt.swp.sealhackathonbe.core.constant;

import java.util.UUID;

public final class TeamStatusConstants {
    public static final UUID FORMING = UUID.fromString("60000000-0000-0000-0000-000000000001");
    public static final UUID ACTIVE = UUID.fromString("60000000-0000-0000-0000-000000000002");
    public static final UUID DISQUALIFIED = UUID.fromString("60000000-0000-0000-0000-000000000003");
    public static final UUID PENDING = UUID.fromString("60000000-0000-0000-0000-000000000005");
    public static final UUID REJECTED = UUID.fromString("60000000-0000-0000-0000-000000000006");

    /**
     * Backward-compatible aliases for older service code and tests.
     */
    @Deprecated
    public static final UUID DRAFT = FORMING;
    @Deprecated
    public static final UUID APPROVED = DISQUALIFIED;

    private TeamStatusConstants() {
    }
}
