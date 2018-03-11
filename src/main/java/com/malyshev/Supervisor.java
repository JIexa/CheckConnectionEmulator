package com.malyshev;

import java.util.List;

public interface Supervisor {

    void checkConnectionFor(List<String> devices) throws InterruptedException;
}
