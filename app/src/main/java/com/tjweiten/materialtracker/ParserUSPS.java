package com.tjweiten.materialtracker;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by TJ on 3/28/2015.
 */
public class ParserUSPS {

    private static final String ns = null;

    public static class Entry {
        public final String TrackSummary;
        public final ArrayList<String> TrackDetails;

        private Entry(String TrackSummary, ArrayList<String> TrackDetails) {
            this.TrackSummary = TrackSummary;
            this.TrackDetails = TrackDetails;
        }
    }

    private Entry readTrackResponse(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "entry");
        String summary = null;
        ArrayList<String> details = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("TrackSummary")) {
                summary = readSummary(parser);
            } else if (name.equals("TrackDetail")) {
                details.add(readDetail(parser));
            } else {
                skip(parser);
            }
        }
        return new Entry(summary, details);
    }

    private String readSummary(XmlPullParser parser) throws IOException, XmlPullParserException {

        parser.require(XmlPullParser.START_TAG, ns, "TrackSummary");
        String summary = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "TrackSummary");
        return summary;

    }

    private String readDetail(XmlPullParser parser) throws IOException, XmlPullParserException {

        parser.require(XmlPullParser.START_TAG, ns, "TrackDetail");
        String detail = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "TrackDetail");
        return detail;

    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    public List parse(InputStream in) throws XmlPullParserException, IOException {

        try {

            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);

        } finally {
            in.close();
        }

    }

    private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List entries = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "feed");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            /* starts by looking for a TrackResponse */
            if (name.equals("TrackResponse")) {
                entries.add(readTrackResponse(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

}