package xyz.krakenkat.ticketservice;

import java.util.Set;

public interface ConfirmedTicketService extends TicketService {
    Set<Integer> getReservedSeats(String confirmationCode);
}
