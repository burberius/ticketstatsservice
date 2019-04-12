package net.troja.trial.ticketstats.rest;

import io.micrometer.core.instrument.MeterRegistry;
import net.troja.trial.ticketstats.model.Ticket;
import net.troja.trial.ticketstats.model.TicketState;
import net.troja.trial.ticketstats.model.TicketStateStatistics;
import net.troja.trial.ticketstats.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/ticket")
public class TicketRestController {
    @Autowired
    private TicketRepository repository;
    @Autowired
    private MeterRegistry meter;

    private Map<TicketState, AtomicInteger> gauges = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        TicketState.all.forEach(t -> gauges.put(t, meter.gauge(getGaugeName(t), new AtomicInteger(0))));
        getStats().forEach(s -> gauges.get(s.getState()).set((int) s.getCount()));
    }

    @GetMapping
    public List<Ticket> getAll() {
        List<Ticket> target = new ArrayList<>();
        repository.findAll().forEach(target::add);
        return target;
    }

    @PostMapping
    public Ticket add(@RequestBody Ticket ticket) {
        if(ticket.getId() != null && repository.existsById(ticket.getId())) {
            throw new TicketExistsException();
        }
        return updateGauge(repository.save(ticket), 1);
    }

    @GetMapping("/{id}")
    public Ticket getSingle(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new TicketNotFoundException());
    }

    @PutMapping("/{id}")
    public Ticket replace(@RequestBody Ticket ticket, @PathVariable Long id) {
        Ticket old = repository.findById(id).orElseThrow(() -> new TicketNotFoundException());
        updateGauge(old, -1);
        ticket.setId(id);
        updateGauge(repository.save(ticket), 1);
        return ticket;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        Ticket ticket = repository.findById(id).orElseThrow(() -> new TicketNotFoundException());
        repository.deleteById(id);
        updateGauge(ticket, -1);
    }

    @GetMapping("/stats")
    public List<TicketStateStatistics> getStats() {
        return repository.findTicketCount();
    }

    private Ticket updateGauge(Ticket ticket, int value) {
        gauges.get(ticket.getState()).getAndAdd(value);
        return ticket;
    }

    protected String getGaugeName(TicketState state) {
        return "state." + state.name().toLowerCase(Locale.ENGLISH);
    }

    protected void setRepository(TicketRepository repository) {
        this.repository = repository;
    }

    protected void setMeter(MeterRegistry meter) {
        this.meter = meter;
    }
}
