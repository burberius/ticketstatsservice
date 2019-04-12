package net.troja.trial.ticketstats.rest;

import io.micrometer.core.instrument.MeterRegistry;
import net.troja.trial.ticketstats.model.Ticket;
import net.troja.trial.ticketstats.model.TicketState;
import net.troja.trial.ticketstats.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TicketRestControllerTest {
    private static final long ID = 42;

    @Mock
    private TicketRepository repository;
    @Mock
    private MeterRegistry meter;

    private TicketRestController classToTest;
    private AtomicInteger open;
    private AtomicInteger waiting;
    private AtomicInteger solved;

    @BeforeEach
    public void setUp() {
        classToTest = new TicketRestController();
        classToTest.setMeter(meter);
        classToTest.setRepository(repository);

        open = new AtomicInteger(0);
        waiting = new AtomicInteger(0);
        solved = new AtomicInteger(0);
        when(meter.gauge(eq(classToTest.getGaugeName(TicketState.OPEN)), any())).thenReturn(open);
        when(meter.gauge(eq(classToTest.getGaugeName(TicketState.WAITING)), any())).thenReturn(waiting);
        when(meter.gauge(eq(classToTest.getGaugeName(TicketState.SOLVED)), any())).thenReturn(solved);

        classToTest.init();
    }

    @Test
    public void add() {
        Ticket ticket = new Ticket(TicketState.OPEN);
        when(repository.save(ticket)).thenReturn(ticket);

        classToTest.add(ticket);

        assertEquals(1, open.get());
    }

    @Test
    public void replace() {
        Ticket old = new Ticket(TicketState.OPEN);
        Ticket ticket = new Ticket(TicketState.WAITING);
        when(repository.findById(ID)).thenReturn(Optional.of(old));
        when(repository.save(ticket)).thenReturn(ticket);

        classToTest.replace(ticket, ID);

        assertEquals(-1, open.get());
        assertEquals(1, waiting.get());
    }

    @Test
    public void delete() {
        Ticket ticket = new Ticket(TicketState.WAITING);
        when(repository.findById(ID)).thenReturn(Optional.of(ticket));

        classToTest.delete(ID);

        verify(repository).deleteById(ID);
        assertEquals(-1, waiting.get());
    }
}
