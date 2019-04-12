package net.troja.trial.ticketstats.rest;

import net.troja.trial.ticketstats.model.Ticket;
import net.troja.trial.ticketstats.model.TicketState;
import net.troja.trial.ticketstats.model.TicketStateStatistics;
import net.troja.trial.ticketstats.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TicketRestControllerIntegrationTest {
    private static final String TITLE = "Some Title";
    private static final String OTHER_TITLE = "Other Title";
    private static final String DESCRIPTION = "Desc";
    private static final String USER = "Ben";
    private static final LocalDateTime DATE = LocalDateTime.now(ZoneOffset.UTC);
    private static final TicketState STATE = TicketState.OPEN;
    public static final String URI = "/ticket/";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TicketRepository repository;

    @BeforeEach
    public void setUp() {
        repository.deleteAll();
    }

    @Test
    public void getAll() {
        repository.save(new Ticket());
        repository.save(new Ticket());

        ResponseEntity<Ticket[]> response = restTemplate.getForEntity(URI, Ticket[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);
    }

    @Test
    public void add() {
        Ticket ticket = new Ticket(TITLE, DESCRIPTION, USER, DATE, STATE);
        ResponseEntity<Ticket> response = restTemplate.postForEntity(URI, ticket, Ticket.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Ticket responseTicket = response.getBody();
        assertNotNull(responseTicket);
        assertNotEquals(ticket.getId(), responseTicket.getId());
        assertEquals(1, repository.count());
    }

    @Test
    public void addWithExistingId() {
        Ticket saved = repository.save(new Ticket());

        Ticket ticket = new Ticket(TITLE, DESCRIPTION, USER, DATE, STATE);
        ticket.setId(saved.getId());
        ResponseEntity<Ticket> response = restTemplate.postForEntity(URI, ticket, Ticket.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void getSingle() {
        Ticket ticket = repository.save(new Ticket());

        ResponseEntity<Ticket> response = restTemplate.getForEntity(URI + ticket.getId() + "/", Ticket.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ticket.getId(), response.getBody().getId());
    }

    @Test
    public void getSingleNotExisting() {
        ResponseEntity<Ticket> response = restTemplate.getForEntity(URI + 42 + "/", Ticket.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void replace() {
        Ticket ticket = new Ticket(TITLE, DESCRIPTION, USER, DATE, STATE);
        repository.save(ticket);

        ticket.setTitle(OTHER_TITLE);
        restTemplate.put(URI + ticket.getId() + "/", ticket);

        Optional<Ticket> result = repository.findById(ticket.getId());
        assertTrue(result.isPresent());
        assertEquals(OTHER_TITLE, result.get().getTitle());
    }

    @Test
    public void replaceNotExisting() {
        HttpEntity<Ticket> entity = new HttpEntity<>(new Ticket());
        ResponseEntity<Ticket> response = restTemplate.exchange(URI + 42 + "/", HttpMethod.PUT, entity, Ticket.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


    @Test
    public void delete() {
        Ticket ticket = new Ticket(TITLE, DESCRIPTION, USER, DATE, STATE);
        repository.save(ticket);

        restTemplate.delete(URI + ticket.getId() + "/");

        assertEquals(0, repository.count());
    }

    @Test
    public void deleteNotExisting() {
        HttpEntity<Ticket> entity = new HttpEntity<>(new Ticket());
        ResponseEntity<Ticket> response = restTemplate.exchange(URI + 42 + "/", HttpMethod.DELETE, entity, Ticket.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getStats() {
        repository.save(new Ticket(TicketState.OPEN));
        repository.save(new Ticket(TicketState.WAITING));
        repository.save(new Ticket(TicketState.WAITING));
        repository.save(new Ticket(TicketState.SOLVED));
        repository.save(new Ticket(TicketState.SOLVED));
        repository.save(new Ticket(TicketState.SOLVED));

        List<TicketStateStatistics> expected = Arrays.asList(new TicketStateStatistics[]{
                new TicketStateStatistics(TicketState.OPEN, 1),
                new TicketStateStatistics(TicketState.WAITING, 2),
                new TicketStateStatistics(TicketState.SOLVED, 3)
        });

        ResponseEntity<TicketStateStatistics[]> response = restTemplate.getForEntity(URI + "stats", TicketStateStatistics[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertIterableEquals(expected, Arrays.asList(response.getBody()));
    }
}
