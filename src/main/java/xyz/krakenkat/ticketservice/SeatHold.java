package xyz.krakenkat.ticketservice;

import java.time.Instant;

public class SeatHold {
    private static IdGen idGen = new IdGen();

    private int id;
    private int count;
    private String customer;
    private Instant expiry;

    boolean isExpired(Instant now) {
        return !expiry.isAfter(now);
    }

    public SeatHold(int numSeats, String customerEmail, Instant expireTime) {
        customer = customerEmail;
        count = numSeats;
        expiry = expireTime;
        id = idGen.getNextID();
    }

    public int getId() {
        return id;
    }

    public int getCount() {
        return count;
    }

    public String getCustomer() {
        return customer;
    }
}
