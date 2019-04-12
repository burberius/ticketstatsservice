package net.troja.trial.ticketstats.repository;

import net.troja.trial.ticketstats.model.Ticket;
import net.troja.trial.ticketstats.model.TicketState;
import net.troja.trial.ticketstats.model.TicketStateStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class TicketRepositoryIntegrationTest {
    private static final TicketState STATE = TicketState.WAITING;

    @Autowired
    private TicketRepository classToTest;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        classToTest.deleteAll();
    }

    @Test
    public void creationAndCount() {
        Ticket ticket = new Ticket();
        classToTest.save(ticket);

        Integer count = jdbcTemplate.queryForObject("select count(*) from ticket", Integer.class);
        assertEquals(1, count.intValue());
    }

    @Test
    public void createAndCheckStateEnum() {
        Ticket ticket = new Ticket("Test", "Nothing here", "Nobody", LocalDateTime.now(ZoneOffset.UTC), STATE);
        classToTest.save(ticket);

        Long state = jdbcTemplate.queryForObject("select state from ticket limit 1", Long.class);
        assertEquals(STATE.ordinal(), state.longValue());
    }

    @Test
    public void findTicketCount() {
        classToTest.save(new Ticket(TicketState.OPEN));
        classToTest.save(new Ticket(TicketState.WAITING));
        classToTest.save(new Ticket(TicketState.WAITING));
        classToTest.save(new Ticket(TicketState.SOLVED));
        classToTest.save(new Ticket(TicketState.SOLVED));
        classToTest.save(new Ticket(TicketState.SOLVED));

        List<TicketStateStatistics> expected = Arrays.asList(new TicketStateStatistics[]{
                new TicketStateStatistics(TicketState.OPEN, 1),
                new TicketStateStatistics(TicketState.WAITING, 2),
                new TicketStateStatistics(TicketState.SOLVED, 3)
        });

        List<TicketStateStatistics> ticketCount = classToTest.findTicketCount();
        assertIterableEquals(expected, ticketCount);
    }

    @Test
    public void findTicketCountEmpty() {
        List<TicketStateStatistics> ticketCount = classToTest.findTicketCount();
        assertTrue(ticketCount.isEmpty());
    }
}
