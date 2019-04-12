package net.troja.trial.ticketstats.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@Entity
public class Ticket {
    @Id
    @GeneratedValue
    private Long id;
    private String user;
    private String title;
    private String description;
    private LocalDateTime date;
    private TicketState state;

    public Ticket() {

    }

    public Ticket(TicketState state) {
        this.state = state;
    }

    public Ticket(String title, String description, String user, LocalDateTime date, TicketState state) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.date = date;
        this.state = state;
    }
}
