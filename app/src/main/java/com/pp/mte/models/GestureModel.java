package com.pp.mte.models;

import android.content.Context;

import com.pp.mte.EditorI;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;

/**
 * Created by kevin on 21/02/19.
 */

public class GestureModel implements EditorI.Model {
    Context context;
    public GestureModel(Context context) {
        this.context = context;
    }

    /**
     * Gets code to output based on fired neuron
     * @param index neuron fired
     * @return code to output
     */
    public String getCode(int index) {
        int i = 0;
        try {
            XmlPullParserFactory parserFactory;
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            InputStream is = context.getAssets().open("code.xml");
            parser.setInput(is, null);

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("code".equals(parser.getName())) {
                            if (i == index) {
                                return parser.nextText();
                            } else {
                                i++;
                            }
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {}
        return "";
    }
}
