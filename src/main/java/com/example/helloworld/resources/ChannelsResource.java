package com.example.helloworld.resources;

import com.codahale.metrics.annotation.Timed;
import com.example.helloworld.core.Channels;
import com.example.helloworld.core.Saying;
import com.google.common.base.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.atomic.AtomicLong;

@Path("/channels")
@Produces(MediaType.APPLICATION_JSON)
public class ChannelsResource {
    private final String[] channels;
    private final AtomicLong counter;

    public ChannelsResource(String[] channels) {
        this.channels = channels;
        this.counter = new AtomicLong();
    }

    @GET
    @Timed
    public Channels getChannels() {
        return new Channels(counter.incrementAndGet(), channels);
    }
}
