package net.troja.trial.ticketstats.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class TicketExistsAdvice {
    @ResponseBody
    @ExceptionHandler(TicketExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String ticketExistsHandler(TicketExistsException ex) {
        return null;
    }
}
