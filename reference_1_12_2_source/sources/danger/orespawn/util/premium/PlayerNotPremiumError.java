/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn.util.premium;

public class PlayerNotPremiumError
extends Error {
    public PlayerNotPremiumError() {
    }

    public PlayerNotPremiumError(String message) {
        super(message);
    }

    public PlayerNotPremiumError(Throwable cause) {
        super(cause);
    }

    public PlayerNotPremiumError(String message, Throwable cause) {
        super(message, cause);
    }

    public PlayerNotPremiumError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

