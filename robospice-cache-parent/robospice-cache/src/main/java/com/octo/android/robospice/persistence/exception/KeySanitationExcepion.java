package com.octo.android.robospice.persistence.exception;

/**
 * Thrown when a sanitation/desanitation problem occurs.
 * @author SNI
 */
public final class KeySanitationExcepion extends SpiceException {

    private static final long serialVersionUID = 1140114915715955254L;

    private KeySanitationExcepion() {
        super("A problem occured during sanitation/desanitation of a key.");
    }

    private KeySanitationExcepion(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    private KeySanitationExcepion(String arg0) {
        super(arg0);
    }

    private KeySanitationExcepion(Throwable arg0) {
        super(arg0);
    }
}
