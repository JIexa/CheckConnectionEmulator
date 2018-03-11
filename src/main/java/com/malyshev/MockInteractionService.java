package com.malyshev;

import java.util.Random;

public class MockInteractionService implements InteractionService {

    private final int TIMEOUT;

    public MockInteractionService(int timeout) {
        TIMEOUT = timeout;
    }

    @Override
    public int sendTo(String deviceAddress, String command) {
        int pseudoConnectionTime = new Random().nextInt(TIMEOUT + 2);

        try {
            if (pseudoConnectionTime < TIMEOUT) {
                System.out.printf("thread: %s. Check connection will take %d seconds%n", Thread.currentThread().getName(), pseudoConnectionTime);
                Thread.sleep(pseudoConnectionTime * 1000L);
                return 200;
            } else {
                Thread.sleep(TIMEOUT * 1000L);
                System.out.printf("thread: %s. Timeout expired (%d) %n", Thread.currentThread().getName(), pseudoConnectionTime);
                return 504;
            }
        } catch (InterruptedException e) {
            System.out.printf("error occurred: %s %n", e.getLocalizedMessage());
        }
        return 500;
    }
}
