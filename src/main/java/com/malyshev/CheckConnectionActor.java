package com.malyshev;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.dispatch.Futures;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;


public class CheckConnectionActor extends AbstractActor {

    private final InteractionService interactionService;
    private final ExecutionContext ec;

    private final ActorRef analyzer;

    public CheckConnectionActor(InteractionService interactionService, ActorRef analyzer) {
        ec = getContext().getSystem().dispatchers().lookup("akka.actor.my-dispatcher");
        this.interactionService = interactionService;
        this.analyzer = analyzer;
    }

    static public Props props(InteractionService interactionService, ActorRef analyzer) {
        return Props.create(CheckConnectionActor.class, () -> new CheckConnectionActor(interactionService, analyzer));
    }

    static public class CheckConnectionData {
        private final String ipAddress;
        private final String command;

        public CheckConnectionData(String ipAddress) {
            this.ipAddress = ipAddress;
            this.command = "eff up";
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CheckConnectionData.class, d -> {
                    System.out.println("Started execution of checkConnection");
//                    Future<Integer> f = Futures.future(() -> {
//                        return responseCode;
//                    }, ec);

                    int responseCode = interactionService.sendTo(d.ipAddress, d.command);
                    System.out.printf("sending to %s%n", analyzer);
                    analyzer.tell(new AnalyzerActor.ResponseMessage(responseCode), self());
                    getContext().stop(getSelf());
                })
                .build();
    }
}
