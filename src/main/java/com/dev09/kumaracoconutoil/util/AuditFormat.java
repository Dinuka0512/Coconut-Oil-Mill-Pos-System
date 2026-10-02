package com.dev09.kumaracoconutoil.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class AuditFormat {

    public static final String EMPTY = "-";

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    private AuditFormat() {
    }

    public static String stamp(LocalDateTime time) {
        return time == null ? EMPTY : STAMP.format(time);
    }

    public static String info(String by, LocalDateTime at) {
        boolean noUser = by == null || by.isBlank();
        boolean noTime = at == null;
        if (noUser && noTime) return EMPTY;
        if (noUser) return stamp(at);
        if (noTime) return by;
        return by + " " + stamp(at);
    }
}
