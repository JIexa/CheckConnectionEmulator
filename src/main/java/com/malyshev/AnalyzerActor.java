package com.malyshev;

import akka.actor.AbstractActor;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.Terminated;
import scala.concurrent.Future;

import java.util.ArrayList;
import java.util.List;

public class AnalyzerActor extends AbstractActor {

    private final List<Integer> timeoutResponseCounter = new ArrayList<>();
    private final List<Integer> okResponseCounter = new ArrayList<>();

    static public Props props() {
        return Props.create(AnalyzerActor.class, AnalyzerActor::new);
    }

    static public class ResponseMessage {
        private final int responseCode;

        public ResponseMessage(int responseCode) {
            this.responseCode = responseCode;
        }
//    }    static public class ResponseMessage {
//        private final Future<Integer> future;
//
//        public ResponseMessage(Future<Integer> future) {
//            this.future = future;
//        }
    }

    static public class LastMessage {
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ResponseMessage.class, rm -> {
                    System.out.println("++++++++++ start analyzing +++++++++");

                    int responseCode = rm.responseCode;

                    if ( responseCode == 200)
                        okResponseCounter.add(responseCode);
                    else if ( responseCode == 504)
                        timeoutResponseCounter.add(responseCode);
                })
                .match(LastMessage.class, x -> {
                    System.out.printf("successful messages: %d, failed messages: %d%n", okResponseCounter.size(), timeoutResponseCounter.size());
                    getContext().stop(self());
                })
                .build();
    }
}
