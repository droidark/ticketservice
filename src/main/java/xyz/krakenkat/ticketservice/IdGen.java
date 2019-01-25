package xyz.krakenkat.ticketservice;

public class IdGen {
    private int nextID = Integer.MIN_VALUE;
    public synchronized int getNextID() {
        return nextID++;
    }
}
