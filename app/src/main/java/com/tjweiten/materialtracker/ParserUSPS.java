package com.tjweiten.materialtracker;

import java.util.ArrayList;

/**
 * Created by TJ on 3/28/2015.
 */
public class ParserUSPS {

    public static class Entry {
        public final String TrackSummary;
        public final ArrayList<String> TrackDetails;

        private Entry(String TrackSummary, ArrayList<String> TrackDetails) {
            this.TrackSummary = TrackSummary;
            this.TrackDetails = TrackDetails;
        }
    }

}
