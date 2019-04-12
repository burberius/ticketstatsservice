package net.troja.trial.ticketstats.repository;

import net.troja.trial.ticketstats.model.Ticket;
import net.troja.trial.ticketstats.model.TicketStateStatistics;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TicketRepository extends CrudRepository<Ticket, Long> {
    @Query("SELECT " +
            "    new net.troja.trial.ticketstats.model.TicketStateStatistics(t.state, COUNT(t)) " +
            "FROM " +
            "    Ticket t " +
            "GROUP BY " +
            "    t.state")
    List<TicketStateStatistics> findTicketCount();
}
