package com.example.russell_daniel_courseworkone.Models;

import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

//Daniel Russell S1707149
//The all in one method for getting the XML, parsing and processing the required fields into objects to be retrieved via getResult
public class ThreadedTask extends Thread
{
    private List<Reading> result; //output result of XML processed into Reading objects
    private Object lock; //lock object used for async execution, object set identical to mainactivity to notify thread

    public List<Reading> getResult(){ return result; } //Getter Result
    public void setLock(Object lock){ this.lock = lock; } //Setter Lock

    @Override
    public void run() {
        try {
            URL url = new URL("http://quakes.bgs.ac.uk/feeds/MhSeismology.xml");
            URLConnection conn = url.openConnection();
            InputStream input = conn.getInputStream();

            List temp = new ArrayList<String>(); //temp arraylist to handle raw data
            result = new ArrayList<Reading>();

            XmlPullParserFactory xpFactory = XmlPullParserFactory.newInstance();
            xpFactory.setNamespaceAware(true);
            XmlPullParser xpParser = xpFactory.newPullParser();
            xpParser.setInput(input, null);

            int eType = xpParser.getEventType();

            //Raw XML Processing stage
            while (eType != XmlPullParser.END_DOCUMENT) {
                if (eType == XmlPullParser.START_TAG) {
                    switch (xpParser.getName()) {
                        case "title": temp.add(xpParser.nextText()); break;
                        case "description": temp.add(xpParser.nextText()); break;
                        case "link": temp.add(xpParser.nextText()); break;
                        case "pubDate": temp.add(xpParser.nextText()); break;
                        case "category": temp.add(xpParser.nextText()); break;
                        case "lat":  temp.add(xpParser.nextText()); break;
                        case "long": temp.add(xpParser.nextText()); break;
                    }
                }
                eType = xpParser.next();
            }

            Reading reading = new Reading();
            int propCount = 0;

            //Acquired XML processed into Reading objects following the predicable pattern of 7 properties each
           for (int i = 5; i < temp.size(); i++) {
                    if(propCount > 6){
                        result.add(reading);
                        propCount = 0;
                        reading = new Reading();
                        reading.setTitle(temp.get(i).toString());
                    }
                    else{
                        switch(propCount){
                            case 0: reading.setTitle(temp.get(i).toString()); break;
                            case 1: reading.setDescription(temp.get(i).toString()); break;
                            case 2: reading.setLink(temp.get(i).toString()); break;
                            case 3: reading.setPubdate(temp.get(i).toString()); break;
                            case 4: reading.setCategory(temp.get(i).toString()); break;
                            case 5: reading.setLat(temp.get(i).toString()); break;
                            case 6: reading.setLon(temp.get(i).toString()); break;
                       }
                    }
               propCount++;
            }

           //checks for any forgotten input Readings
           if(reading.getLon() != null){
               result.add(reading);
               reading = null;
           }

           //notify block using lock object to tell thread in mainactivity to wakeup
           synchronized (lock){
               lock.notify();
           }

        } catch (IOException | XmlPullParserException e) {
            Log.println(Log.ERROR, "GetInputStreamThread", e.toString());
        }
    }
}
