/**
 * A class to store data for log info from SensorLogic server.
 *
 * @author Zac (Qi ZHANG)
 * Created on 10/01/2014.
 */
package com.dfwexcellerator.iplugin.helpers;

import java.io.Serializable;

public class LogItem implements Serializable {

    private long timeStamp;
    private long value;

    LogItem(long ts, long v) {
        timeStamp = ts;
        value = v;
    }

    public long getValue() {
        return value;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

}
