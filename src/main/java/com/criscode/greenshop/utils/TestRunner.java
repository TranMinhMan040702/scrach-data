package com.criscode.greenshop.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestRunner implements CommandLineRunner {

    private final ApplicationContext context;
    private final ScratchData scratchData;

    @Override
    public void run(String... args) throws Exception {
        context.getApplicationName();

        scratchData.run();
    }

}
