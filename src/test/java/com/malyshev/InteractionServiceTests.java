package com.malyshev;

import org.junit.Assert;
import org.junit.Test;

public class InteractionServiceTests {

    @Test
    public void sendTo_200() {

        InteractionService interactionService = new MockInteractionService(2);


        int responseCode = interactionService.sendTo("addr", "command");

        Assert.assertEquals( "connection was established", 200, responseCode);

    }
}
