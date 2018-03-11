package com.malyshev;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.routing.*;
import com.malyshev.metrics.Timer;

import java.util.ArrayList;
import java.util.List;

public class ActorSupervisor extends AbstractActor {

    private static final int TIMEOUT = 3;

    private final List<String> devices;
    private int countDown;
    private final Timer timer;

    private ActorRef analyzer;

    private Router router;

    static public Props props(List<String> devices, ActorRef analyzer) {
        return Props.create(ActorSupervisor.class, () -> new ActorSupervisor(devices, analyzer));
    }

    public ActorSupervisor(List<String> devices, ActorRef analyzer) {
        this.analyzer = analyzer;
        this.devices = devices;

        timer = new Timer();
        countDown = devices.size();

        initializeWorkers(devices);
    }

    private void initializeWorkers(List<String> devices) {
        List<Routee> routees = new ArrayList<>();

        devices.forEach(device -> {
            ActorRef worker = getContext().actorOf(CheckConnectionActor.props(new MockInteractionService(TIMEOUT), analyzer).withDispatcher("akka.actor.my-dispatcher"));
            getContext().watch(worker);

            routees.add(new ActorRefRoutee(worker));
        });
        router = new Router(new RoundRobinRoutingLogic(), routees);
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CheckConnectionActor.CheckConnectionData.class,
                        m -> {
                            if (timer.getCurTime() == 0) {
                                timer.start();
                            }
                            router.route(m, getSender());
                        })
                .match(Terminated.class,
                        m -> {
                            System.out.println("router was terminated");
                            router = router.removeRoutee(m.actor());
                            System.out.printf("checkers are left: %d%n", --countDown);
                            if (countDown == 0) {
                                analyzer.tell(new AnalyzerActor.LastMessage(), self());
                                timer.stop();
                                getContext().stop(self());
                            }
                        })
                .build();
    }
}
