package net.troja.trial.ticketstats.model;

import lombok.Data;

@Data
public class TicketStateStatistics {
    private TicketState state;
    private long count;

    public TicketStateStatistics(TicketState state, long count) {
        this.state = state;
        this.count = count;
    }
}
