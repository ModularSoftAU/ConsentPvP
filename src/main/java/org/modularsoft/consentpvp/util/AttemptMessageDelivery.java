package org.modularsoft.consentpvp.util;

public enum AttemptMessageDelivery {
    CHAT,
    ACTION_BAR;

    public static AttemptMessageDelivery fromConfig(String value) {
        if (value == null) {
            return CHAT;
        }

        try {
            return AttemptMessageDelivery.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return CHAT;
        }
    }
}
