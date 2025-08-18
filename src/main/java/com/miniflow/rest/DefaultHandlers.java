package com.miniflow.rest;

import org.springframework.context.annotation.Configuration;

import com.miniflow.core.DbBackedEngine;

import jakarta.annotation.PostConstruct;

@Configuration
public class DefaultHandlers {

    private final DbBackedEngine engine;

    public DefaultHandlers(DbBackedEngine engine) {
        this.engine = engine;
    }

    @PostConstruct
    public void register() {
        // Example: register a Java handler by type (matches <serviceTask id="SendEmail">)
        engine.register("SendEmail", ctx -> {
            // pretend we send an email and write a var
            ctx.vars().put("emailSent", true);
        });
    }
}
