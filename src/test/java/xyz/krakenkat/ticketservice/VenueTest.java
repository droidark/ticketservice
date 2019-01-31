package xyz.krakenkat.ticketservice;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

public class VenueTest {
    @Test
    public void holdTest() {
        Instant t0 = Instant.EPOCH;
        String customer = "pirate@krakenkat.xyz";
        int capacity = 1;
        Duration holdLength = Duration.ofDays(1);
        Venue v = new Venue(capacity, holdLength);
        Instant t1 = t0.plus(holdLength);
        Assert.assertEquals("All seats are available", capacity, v.numSeatsAvailable(t0));
        Assert.assertNull(
                "Attempting to hold more seats than are available",
                v.findAndHoldSeats(t0,capacity + 1, customer));
        try {
            v.findAndHoldSeats(t0,0, customer);
            Assert.fail("Requests to hold fewer than one.");
        } catch (IllegalArgumentException e) {}
        int numRequested = capacity;
        SeatHold hold = v.findAndHoldSeats(t0, numRequested, customer);
        Assert.assertEquals(customer, hold.getCustomer());
        Assert.assertEquals(numRequested, hold.getCount());
        Assert.assertEquals(
                "After the hold has expired, all the seats are available again",
                capacity,
                v.numSeatsAvailable(t1));
    }

    @Test
    public void reserveTest() {
        Instant t0 = Instant.EPOCH;
        String customer = "pirate@krakenkat.xyz";
        int capacity = 1;
        Duration holdLength = Duration.ofDays(1);
        Instant t1 = t0.plus(holdLength);
        for (Instant reserveTime : Arrays.asList(t0, t1)){
            Venue v = new Venue(capacity, Duration.ofDays(1));
            SeatHold hold = v.findAndHoldSeats(t0, capacity, customer);
            String confirmation = v.reserveSeats(reserveTime, hold.getId(), customer);
            if (hold.isExpired(reserveTime)) {
                Assert.assertNull("Reserving on an expired hold", confirmation);
            } else {
                Assert.assertNotNull(confirmation);
            }
            Assert.assertNull(
                    "Reserving more than once yields",
                    v.reserveSeats(reserveTime, hold.getId(), customer));
        }
    }

    @Test
    public void holdAndReserveScenario() {
        Instant t0 = Instant.EPOCH;
        Duration delta = Duration.ofDays(1);
        Instant t1 = t0.plus(delta);
        Instant t2 = t1.plus(delta);
        Instant t3 = t2.plus(delta);
        Instant t4 = t3.plus(delta);
        Instant t5 = t4.plus(delta);

        String customer = "pirate@krakenkat.xyz";

        int capacity = 5;
        Duration holdLength = Duration.ofDays(2);
        Venue v = new Venue(capacity, holdLength);

        SeatHold s0 = v.findAndHoldSeats(t0, 1, customer);
        SeatHold s1 = v.findAndHoldSeats(t0, 4, customer);
        Assert.assertNotNull(s0);
        Assert.assertNotNull(s1);
        Assert.assertEquals(0, v.numSeatsAvailable(t0));

        Assert.assertNull(v.findAndHoldSeats(t1, 1, customer));
        String c0 = v.reserveSeats(t1, s0.getId(), customer);
        Assert.assertNotNull(c0);
        Assert.assertEquals(0, v.numSeatsAvailable(t1));

        Assert.assertNull(v.reserveSeats(t2, s1.getId(), customer));
        SeatHold s2 = v.findAndHoldSeats(t2, 2, customer);
        Assert.assertNotNull(s2);
        Assert.assertEquals(2, v.numSeatsAvailable(t2));
        String c2 = v.reserveSeats(t2, s2.getId(), customer);
        Assert.assertNotNull(c2);
        Assert.assertEquals(2, v.numSeatsAvailable(t2));
        Assert.assertTrue(
                "The first reservation contains the best seat in the room",
                v.getReservedSeats(c0).contains(0));
        for (int earlyReservedSeat : v.getReservedSeats(c0)) {
            for (int lateReservedSeat : v.getReservedSeats(c2)) {
                Assert.assertTrue(
                        "A seat reserved earlier.",
                        earlyReservedSeat < lateReservedSeat);
            }
        }

        Assert.assertEquals(2, v.numSeatsAvailable(t3));
        Assert.assertEquals(2, v.numSeatsAvailable(t4));
        Assert.assertEquals(2, v.numSeatsAvailable(t5));
    }
}