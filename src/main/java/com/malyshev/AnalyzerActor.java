package com.malyshev;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.malyshev.metrics.Timer;
import scala.compat.java8.FutureConverters;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class AnalyzerActor extends AbstractActor {

    private final List<Integer> timeoutResponseCounter = new ArrayList<>();
    private final List<Integer> okResponseCounter = new ArrayList<>();

    private final ExecutionContext ec;
    
    static public Props props() {
        return Props.create(AnalyzerActor.class, AnalyzerActor::new);
    }

    public AnalyzerActor() {
        ec = getContext().getSystem().dispatchers().lookup("akka.actor.my-dispatcher");
    }

    static public class ResponseMessage {
        private final Future<Integer> future;

        public ResponseMessage(Future<Integer> future) {
            this.future = future;
        }
    }

    static public class LastMessage {
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ResponseMessage.class, rm -> {
                    System.out.println("============ start analyzing ============");

                    CompletionStage<Integer> cs = FutureConverters.toJava(rm.future);

                    cs.thenApply(x -> {
                        if (x == 200)
                            okResponseCounter.add(x);
                        return x;
                    }).exceptionally(x -> {
                        int responseCode = 504;
                        timeoutResponseCounter.add(responseCode);
                        return responseCode;
                    });
                })
                .match(LastMessage.class, x -> {
                    System.out.printf("successful messages: %d, failed messages: %d%n", okResponseCounter.size(), timeoutResponseCounter.size());
                    getContext().stop(self());
                })
                .build();
    }
}
