/**
 * A class to represent devices in "My Plugins" fragment.
 *
 * @author Zac (Qi ZHANG)
 * Created on 09/29/2014.
 */
package com.dfwexcellerator.iplugin.helpers;

public class PluginItem {

    private String boardDeviceID;
    private String sensorID;
    private String nickName;
    private String deviceType;
    private String deviceBrand;
    private String deviceModel;
    private int deviceConsume;
    private int percent;

    PluginItem(String boardID, String sensor, String name,
               String type, String brand, String model, int consume) {
        this.boardDeviceID = boardID;
        this.sensorID = sensor;
        this.nickName = name;
        this.deviceType = type;
        this.deviceBrand = brand;
        this.deviceModel = model;
        this.deviceConsume = consume;
        this.percent = 0;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getNickName() {
        return nickName;
    }

    public int getPercent() {
        return percent;
    }

    public String getBoardDeviceID() {
        return boardDeviceID;
    }

    public String getSensorID() {
        return sensorID;
    }

    public String getDeviceBrand() {
        return deviceBrand;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public int getDeviceConsume() {
        return deviceConsume;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

}
