package com.ktcb.kaksidi.views;

import com.ktcb.kaksidi.core.Play;
import io.dropwizard.views.View;

public class PlayView extends View {
    private final Play play;
    public PlayView (Play play) {
        super("play.ftl");
        this.play = play;
    }

    public Play getPlay() {
        return play;
    }
}
