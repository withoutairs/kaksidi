package com.ktcb.kaksidi.views;

import com.ktcb.kaksidi.core.Play;
import io.dropwizard.views.View;

import java.util.List;

public class NowPlayingView extends View {
    private final List<Play> plays;
    public NowPlayingView(List<Play> plays) {
        super("now-playing.ftl");
        this.plays = plays;
    }

    public List<Play> getPlays() {
        return plays;
    }
}
