package xyz.krakenkat.ticketservice;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Venue implements ConfirmedTicketService {

    private final Duration holdLength;
    private final int capacity;
    private final Map<String, Set<Integer>> confirmations = new HashMap<>();
    private int reserveCount;
    private IdGen idGen = new IdGen();
    private List<String> seats;
    private Map<Integer, SeatHold> holds = new HashMap<>();

    public Venue(int capacity, Duration holdLength) {
        this.capacity = capacity;
        this.seats = Arrays.asList(new String[capacity]);
        this.holdLength = holdLength;
    }

    public int getCapacity() {
        return capacity;
    }

    public Duration getHoldLength() {
        return holdLength;
    }

    private synchronized int numHeld(Instant now) {
        holds.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
        return holds
                .entrySet()
                .stream()
                .mapToInt(e -> e.getValue().getCount())
                .sum();
    }

    synchronized int numSeatsAvailable(Instant now) {
        return capacity - numHeld(now) - reserveCount;
    }

    @Override
    public Set<Integer> getReservedSeats(String confirmationCode) {
        synchronized (confirmations) {
            Set<Integer> reservations = confirmations.get(confirmationCode);
            return reservations != null ? reservations : Collections.emptySet();
        }
    }

    @Override
    public synchronized int numSeatsAvailable() {
        return numSeatsAvailable(Instant.now());
    }

    synchronized SeatHold findAndHoldSeats(Instant now, int numSeats, String customerEmail) {
        if (numSeats < 1) {
            throw new IllegalArgumentException("At least one seat must be reserved.");
        }
        if (numSeats > numSeatsAvailable(now)) {
            return null;
        }
        SeatHold hold = new SeatHold(numSeats, customerEmail, now.plus(holdLength));
        holds.put(hold.getId(), hold);
        return hold;
    }

    synchronized String reserveSeats(Instant now, int seatHoldId, String customerEmail) {
        SeatHold hold = holds.get(seatHoldId);
        if (hold == null) {
            return null;
        }
        if (!hold.getCustomer().equals(customerEmail)) {
            throw new IllegalArgumentException();
        }
        if (hold.isExpired(now)) {
            return null;
        }
        String confirmation = Integer.toString(idGen.getNextID());
        holds.remove(seatHoldId);
        int holdCount = hold.getCount();
        reserveCount += holdCount;
        Set<Integer> assignments = new HashSet<>();
        ListIterator<String> iter = seats.listIterator();
        for (int seatNumber = 0; 0 < holdCount; seatNumber++) {
            if (iter.next() == null) {
                iter.set(confirmation);
                assignments.add(seatNumber);
                holdCount--;
            }
        }
        synchronized (confirmations) {
            confirmations.put(confirmation, assignments);
        }
        return confirmation;
    }

    @Override
    public synchronized SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        return findAndHoldSeats(Instant.now(), numSeats, customerEmail);
    }

    @Override
    public synchronized String reserveSeats(int seatHoldId, String customerEmail) {
        return reserveSeats(Instant.now(), seatHoldId, customerEmail);
    }
}
