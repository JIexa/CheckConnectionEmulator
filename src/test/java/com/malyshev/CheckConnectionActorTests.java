package com.malyshev;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.routing.RoundRobinPool;
import com.malyshev.metrics.Timer;
import org.junit.Before;
import org.junit.Test;
import scala.concurrent.Await;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.malyshev.ActorSupervisor.*;

public class CheckConnectionActorTests {

    private final int NUMBER_OF_DEVICES = 1000;

    private Timer timer;

    private List<String> devices;

    private ActorSystem system;
    private ActorRef checker;
    private ActorRef router;
    private ActorRef analyzer;
    private ActorRef router2;
    private InteractionService interactionService;

    @Before
    public void setUpActorSystem() {
        timer = new Timer();
        system = ActorSystem.create("check-connection-actor-system");

        interactionService = new MockInteractionService(30);
        fillDeviceList();

        analyzer = system.actorOf(AnalyzerActor.props(), "analyzer");
        checker = system.actorOf(props(devices, analyzer), "checker");
        router = system.actorOf(ActorSupervisor.props(devices, analyzer).withDispatcher("akka.actor.my-dispatcher"), "router");
//        router2 = system.actorOf(new RoundRobinPool(devices.size()).props(CheckConnectionActor.props(new MockInteractionService(3), analyzer)));
    }

    private void fillDeviceList() {

        devices = new ArrayList<>(NUMBER_OF_DEVICES);

        timer.start();
        Random randomValueForAddress = new Random();
        for (int i = 0; i < NUMBER_OF_DEVICES; i++) {

            devices.add(String.format("%d.%d.%d.%d", randomValueForAddress.nextInt(256), randomValueForAddress.nextInt(256), randomValueForAddress.nextInt(256), randomValueForAddress.nextInt(256)));
        }
        System.out.println("list of devices is ready");
        timer.stop();
    }

    @Test
    public void checkConnectionByActor() {

        List<CheckConnectionActor.CheckConnectionData> checkConnectionDataList = getCheckConnectionDataFrom(devices);

        checkConnectionDataList.parallelStream().forEach(ccd -> router.tell(ccd, ActorRef.noSender()));

        while (!analyzer.isTerminated());

        System.out.println("it is over!");

    }

    private List<CheckConnectionActor.CheckConnectionData> getCheckConnectionDataFrom(List<String> devices) {

        List<CheckConnectionActor.CheckConnectionData> checkConnectionDataList = new ArrayList<>();

        devices.forEach( device -> {
            CheckConnectionActor.CheckConnectionData ccd = new CheckConnectionActor.CheckConnectionData(device);

            checkConnectionDataList.add(ccd);
        });

        return checkConnectionDataList;
    }
}