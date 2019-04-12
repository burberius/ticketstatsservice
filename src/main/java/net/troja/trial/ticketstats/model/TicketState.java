package net.troja.trial.ticketstats.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum TicketState {
    OPEN, WAITING, SOLVED;

    public static final List<TicketState> all = Collections.unmodifiableList(Arrays.asList(new TicketState[]{OPEN, WAITING, SOLVED}));
}
