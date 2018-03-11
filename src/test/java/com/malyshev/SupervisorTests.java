package com.malyshev;

import com.malyshev.metrics.Timer;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SupervisorTests {

    private final int NUMBER_OF_DEVICES = 1000;

    private Timer timer;

    private List<String> devices;

    private InteractionService interactionService;
    private Supervisor supervisor;

    @Before
    public void creatingDeviceList() {
        initializingInstances();

        devices = new ArrayList<>(NUMBER_OF_DEVICES);

        timer.start();
        Random randomValueForAddress = new Random();
        for (int i = 0; i < NUMBER_OF_DEVICES; i++) {

            devices.add(String.format("%d.%d.%d.%d", randomValueForAddress.nextInt(256), randomValueForAddress.nextInt(256), randomValueForAddress.nextInt(256), randomValueForAddress.nextInt(256)));
        }
        System.out.println("list of devices is ready");
        timer.stop();
    }

    private void initializingInstances() {
        System.out.println("============ SetUp ==============");

        interactionService = new MockInteractionService(3);
        supervisor = new ThreadSupervisor(interactionService, NUMBER_OF_DEVICES);
        timer = new Timer();

    }

    @Test
    public void sendToDevice() throws InterruptedException {
        supervisor.checkConnectionFor(devices);
    }

}
