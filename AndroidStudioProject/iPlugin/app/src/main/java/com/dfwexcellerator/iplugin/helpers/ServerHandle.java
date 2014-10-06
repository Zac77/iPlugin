/**
 * An enum type to handle all requests to and from server.
 *
 * Used "Singleton pattern" for this enum class.
 * Reference:
 * http://en.wikipedia.org/wiki/Singleton_pattern
 * "The enum way" part
 *
 * @author Zac (Qi ZHANG) and Trent (Quan ZHANG)
 * Created by Zac on 09/23/2014.
 * Methods finished by Zac and Trent.
 */
package com.dfwexcellerator.iplugin.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dfwexcellerator.iplugin.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public enum ServerHandle {
    INSTANCE;

    /***********************************
     * This part is finished by Zac.
     ***********************************/

    private static final String TAG = "ServerHandle.INSTANCE";

    // Saved user authorization info
    private String userEmail = "";
    private String authorization = "";

    /**
     * Set user authorization info.
     *
     * @param email user's email address
     * @param auth user's authorization info
     */
    public void setServerHandleAuthorization(String email, String auth) {
        this.userEmail = email;
        this.authorization = auth;
    }

    /**
     * Get user's plugins' info from server.
     *
     * @param activity application environment info
     * @param mProgressBar ProgressBar View for updating progress
     * @param mTextView TextView for showing text info of updating progress
     * @return A list for all plugins the user has, or null if error occurred
     */
    @SuppressWarnings("unchecked")
    public List<PluginItem> getPluginsList(final Activity activity,
                                           ProgressBar mProgressBar, final TextView mTextView) {
        try {
            // Set UI
            mProgressBar.setProgress(0);
            mTextView.setText(R.string.text_downloading_data_without_percent);

            // Get plugins' list for current user
            ArrayList<PluginItem> pluginsInfo = getAllPluginsInfoFromServer();

            // Get preferences data file
            SharedPreferences dataFile = activity.getSharedPreferences(
                    Constants.PREFS_SENSOR_DATA_FILE_NAME, Context.MODE_PRIVATE);

            // Used for calculate total power consume
            BigInteger[] powerConsume = new BigInteger[pluginsInfo.size()];

            // Used for show message on UI
            int totalNumberOfElementsNeedToGet;
            int numberOfElementsAlreadyGot;

            // Get data
            for (int i = 0; i < pluginsInfo.size() &&
                    !Thread.currentThread().isInterrupted(); i++) {

                // Initialize
                numberOfElementsAlreadyGot = 0;
                PluginItem currentPluginItem = pluginsInfo.get(i);
                String preferenceKeyLastTimeStamp = currentPluginItem.getBoardDeviceID() +
                        currentPluginItem.getSensorID() + Constants.SUFFIX_LAST_TIMESTAMP;
                String preferenceKeyPowerConsume = currentPluginItem.getBoardDeviceID() +
                        currentPluginItem.getSensorID() + Constants.SUFFIX_POWER_CONSUME;
                String preferenceKeySavedData = currentPluginItem.getBoardDeviceID() +
                        currentPluginItem.getSensorID() + Constants.SUFFIX_SAVED_DATA_SET;
                long lastTimeStampInLong = dataFile.getLong(preferenceKeyLastTimeStamp, -1);
                powerConsume[i] = new BigInteger(dataFile
                        .getString(preferenceKeyPowerConsume, "0"));
                String currentTimeStampInServerFormat = getTimeStampInServerFormat();
                String authCredentials = getAuthCredentials(currentTimeStampInServerFormat);
                String url = Constants.AUTH_STATUS_REPORT_API_ADDRESS + "propertyName=" +
                        currentPluginItem.getSensorID();

                // If there is a "Last Time Stamp" in preference, use it for downloading data
                if (lastTimeStampInLong > 0)
                    url += "&fromDate=" +
                            convertTimeFormatFromLongToServerFormat(lastTimeStampInLong);

                url += "&toDate=" + currentTimeStampInServerFormat + authCredentials +
                        "&pageSize=" + Constants.PAGE_SIZE;
                JSONObject jsonObjectFromServer = sendHttpGetRequest(url).getJSONObject("body")
                        .getJSONObject("statusReports");
                int totalPages = Integer.parseInt(jsonObjectFromServer.getString("totalPages"));
                int totalNumberOfElementsFromServer = Integer.parseInt(
                        jsonObjectFromServer.getString("totalNumberOfElements"));
                totalNumberOfElementsNeedToGet = totalNumberOfElementsFromServer;
                ArrayList<LogItem> newDataMap =
                        new ArrayList<LogItem>(totalNumberOfElementsFromServer);

                // Get data page by page
                for (int j = totalPages; j >= 1 && !Thread.currentThread().isInterrupted(); j--) {
                    jsonObjectFromServer = sendHttpGetRequest(url + "&pageNumber=" + j)
                            .getJSONObject("body").getJSONObject("statusReports");
                    int numberOfElementsInThisPage = Integer.parseInt(jsonObjectFromServer
                            .getString("numberOfElements"));
                    JSONArray resultSet = jsonObjectFromServer.getJSONArray("resultSet");
                    for (int k = numberOfElementsInThisPage-1; k >= 0 &&
                            !Thread.currentThread().isInterrupted(); k--) {
                        int propertiesArraySize = resultSet.getJSONObject(k)
                                .getJSONArray("properties").length();
                        int value = Integer.parseInt(resultSet.getJSONObject(k)
                                .getJSONArray("properties")
                                .getJSONArray(propertiesArraySize - 1).getString(1)
                                .replaceAll("\\.", ""));
                        Long timeStamp = convertTimeFormatFromServerFormatToLong(
                                resultSet.getJSONObject(k).getString("happened"));
                        newDataMap.add(new LogItem(timeStamp, value));
                    }
                    numberOfElementsAlreadyGot += numberOfElementsInThisPage;
                    final int downloadingPercent = 100 / pluginsInfo.size() * i +
                            numberOfElementsAlreadyGot * 100 / totalNumberOfElementsNeedToGet /
                                    pluginsInfo.size();
                    mProgressBar.setProgress(downloadingPercent);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTextView.setText(activity.getString(
                                    R.string.text_downloading_data) + ' ' + downloadingPercent + '%');
                        }
                    });
                }

                // Save data to preference
                String oldDataMapString = dataFile.getString(preferenceKeySavedData, null);
                ArrayList<LogItem> dataMap;
                LogItem lastValue;
                if (oldDataMapString != null) {
                    dataMap = (ArrayList<LogItem>) deSerialize(Base64
                            .decode(oldDataMapString, Base64.DEFAULT));
                    lastValue = dataMap.get(dataMap.size()-1);
                } else {
                    dataMap = new ArrayList<LogItem>();
                    lastValue = new LogItem(0, 0);
                }
                for (LogItem item : newDataMap) {
                    powerConsume[i] = powerConsume[i].add(new BigInteger(String
                            .valueOf((item.getTimeStamp() - lastValue.getTimeStamp()) *
                                    lastValue.getValue() * currentPluginItem.getDeviceConsume())));
                    lastValue = item;
                }
                dataMap.addAll(newDataMap);
                if (!Thread.currentThread().isInterrupted())
                    dataFile.edit()
                            .remove(preferenceKeySavedData)
                            .remove(preferenceKeyLastTimeStamp)
                            .remove(preferenceKeyPowerConsume)
                            .putString(preferenceKeySavedData,
                                    Base64.encodeToString(serialize(dataMap), Base64.DEFAULT))
                            .putLong(preferenceKeyLastTimeStamp,
                                    convertTimeFormatFromServerFormatToLong(
                                            currentTimeStampInServerFormat))
                            .putString(preferenceKeyPowerConsume, powerConsume[i].toString())
                            .apply();
            }

            // Calculate power consume percent for each device
            if (!Thread.currentThread().isInterrupted()) {
                BigInteger totalPowerConsume = new BigInteger("0");
                for (BigInteger l : powerConsume)
                    totalPowerConsume = totalPowerConsume.add(l);
                BigInteger big100 = new BigInteger("100");
                for (int i = 0; i < pluginsInfo.size(); i++)
                    pluginsInfo.get(i).setPercent(powerConsume[i]
                            .multiply(big100).divide(totalPowerConsume).intValue());
                for (int i = 0; i < pluginsInfo.size() - 1; i++) {
                    int maxPercent = pluginsInfo.get(i).getPercent();
                    int maxIndex = i;
                    for (int j = i + 1; j < pluginsInfo.size() - 1; j++)
                        if (pluginsInfo.get(j).getPercent() > maxPercent) {
                            maxPercent = pluginsInfo.get(j).getPercent();
                            maxIndex = j;
                        }
                    if (i != maxIndex)
                        Collections.swap(pluginsInfo, i, maxIndex);
                }
            }

            // Return result
            if (!Thread.currentThread().isInterrupted())
                return pluginsInfo;
        } catch (InterruptedException e) {
            Log.w(TAG, "Thread Interrupted.");
        } catch (Exception e) {
            Log.e(TAG, "Caught an exception.");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get data from server for drawing the chart to show activity for a given plugin.
     *
     * @param plugin the given plugin
     * @param timeRange A string to determine data time range
     * @return a list for all data, or null if error occurred
     */
    @SuppressWarnings("unchecked")
    public List<LogItem> getChartDataForAPlugin(
            PluginItem plugin, String timeRange, Context context) {
        try {
            Calendar c = Calendar.getInstance();
            c.setTimeZone(TimeZone.getTimeZone("UTC"));
            long currentTimeInMilliseconds = c.getTimeInMillis();
            int totalDataSize;
            boolean realTimeData;

            if (timeRange.equals("10 Mins")) {
                c.add(Calendar.MINUTE, -10);
                totalDataSize = 10;
                realTimeData = true;
            } else if(timeRange.equals("3 Hours")) {
                c.add(Calendar.HOUR_OF_DAY, -3);
                totalDataSize = 180;
                realTimeData = false;
            } else if(timeRange.equals("6 Hours")) {
                c.add(Calendar.HOUR_OF_DAY, -6);
                totalDataSize = 360;
                realTimeData = false;
            } else {
                c.add(Calendar.HOUR_OF_DAY, -24);
                totalDataSize = 1440;
                realTimeData = false;
            }
            long methodStartTime = c.getTimeInMillis();

            SharedPreferences dataFile = context.getSharedPreferences(
                    Constants.PREFS_SENSOR_DATA_FILE_NAME, Context.MODE_PRIVATE);
            String preferenceKeySavedData = plugin.getBoardDeviceID() +
                    plugin.getSensorID() + Constants.SUFFIX_SAVED_DATA_SET;
            String oldDataMapString = dataFile.getString(preferenceKeySavedData, null);
            ArrayList<LogItem> dataMap = (ArrayList<LogItem>) deSerialize(Base64
                    .decode(oldDataMapString, Base64.DEFAULT));

            // Search through dataMap to create sub ArrayList<LogItem>
            int index = dataMap.size();
            for (int i = 0; i < dataMap.size(); i++)
                if(dataMap.get(i).getTimeStamp() >= methodStartTime) {
                    index = i;
                    break;
                }

            List<LogItem> subDataMap = dataMap.subList(index, dataMap.size());
            if (subDataMap.size() == 0) {
                List<LogItem> returnList = new ArrayList<LogItem>(2);
                returnList.add(new LogItem(methodStartTime, 0));
                returnList.add(new LogItem(currentTimeInMilliseconds, 0));
                return returnList;
            }
            if (realTimeData) {
                List<LogItem> returnList = new ArrayList<LogItem>(dataMap.size() + 2);
                returnList.add(new LogItem(methodStartTime, 0));
                returnList.addAll(subDataMap);
                returnList.add(new LogItem(currentTimeInMilliseconds, 0));
                return returnList;
            }

            // Calculate average power consume for each minutes
            List<LogItem> returnList = new ArrayList<LogItem>(totalDataSize);
            long loopStartTime = methodStartTime / 60000l * 60000l;
            int searchedIndex = 0;
            int i = 0;
            while (i < totalDataSize) {
                long loopEndTime = loopStartTime + 60000;
                if (subDataMap.get(searchedIndex).getTimeStamp() >= loopEndTime ||
                        searchedIndex == subDataMap.size()-1)
                    returnList.add(new LogItem(loopStartTime, 0));
                else {
                    int loopBeginIndex = searchedIndex;
                    while (searchedIndex < subDataMap.size() &&
                            subDataMap.get(searchedIndex).getTimeStamp() < loopEndTime)
                        searchedIndex++;
                    searchedIndex--;
                    if (searchedIndex > loopBeginIndex) {
                        long loopSum = (subDataMap.get(loopBeginIndex + 1).getTimeStamp() -
                                loopStartTime) * subDataMap.get(loopBeginIndex).getValue();
                        for (int j = loopBeginIndex + 1; j < searchedIndex; j++)
                            loopSum += (subDataMap.get(j + 1).getTimeStamp() - subDataMap
                                    .get(j).getTimeStamp()) * subDataMap.get(j).getValue();
                        loopSum += (loopEndTime - subDataMap.get(searchedIndex).getTimeStamp()) *
                                subDataMap.get(searchedIndex).getValue();
                        returnList.add(new LogItem(loopStartTime, loopSum / 60000));
                    } else
                        returnList.add(new LogItem(loopStartTime, 0));
                }
                loopStartTime = loopEndTime;
                i++;
            }
            return returnList;
        } catch (Exception e) {
            Log.e(TAG, "Caught an exception.");
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Convert HTTP a entity in a HTTP response to JSON object.
     *
     * @param entity the entity needed to convert
     * @return Converted JSON object, or null if error occurred
     */
    private JSONObject convertHTTPResponseEntityToJSON(HttpEntity entity) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                builder.append(line);
            return new JSONObject(builder.toString());
        } catch (Exception e) {
            Log.e("ServerRequestClass", "Caught an exception.");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Send a HTTP GET request to given URL, and convert response to a JSON object.
     *
     * @param url target URL
     * @return a JSON Object from server response, or null if error occurred
     * @throws IOException
     */
    private JSONObject sendHttpGetRequest(String url) throws IOException{
        HttpUriRequest request = new HttpGet(url);
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            Log.e(TAG, "Failed to get JSON object in getStatusReport(), HTTP statusCode: " +
                    statusCode);
            return null;
        } else
            return convertHTTPResponseEntityToJSON(entity);
    }

    /***********************************
     * Below part is finished by Trent.
     ***********************************/

    /**
     * Use authorization info to authorize user with server.
     *
     * Reference:
     * http://www.codeofaninja.com/2013/04/android-http-client.html
     *
     * @return true if success, false if wrong, or null if error occurred
     */
    public Boolean authorizeUserInfo() {
        try {
            HttpPost request = new HttpPost(Constants.PHP_AUTHORIZE_USER);
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("email", userEmail));
            nameValuePairs.add(new BasicNameValuePair("auth", authorization));
            request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if(entity != null){
                String responseBody = EntityUtils.toString(entity, HTTP.UTF_8);
                if(responseBody.equals("s"))
                    return true;
                else if(responseBody.equals("f"))
                    return false;
                else
                    Log.e(TAG, "Failed to get user authorization. Response: " + responseBody);
            } else
                Log.e(TAG, "Failed to get JSON object, HTTP statusCode: " + statusCode);
        } catch(Exception e){
            Log.e(TAG, "Caught an exception");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get a list for all plugins of current user from server.
     *
     * @return a list for all plugins' info, or null if error occurred
     */
    private ArrayList<PluginItem> getAllPluginsInfoFromServer() {
        try {
            HttpPost request = new HttpPost(Constants.PHP_PLUGIN_GET_LIST);
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("email", userEmail));
            nameValuePairs.add(new BasicNameValuePair("auth", authorization));
            request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                JSONObject jsonObject = convertHTTPResponseEntityToJSON(entity);
                JSONArray array = jsonObject.getJSONArray("result");
                ArrayList<PluginItem> lists = new ArrayList<PluginItem>(array.length());
                for (int i = 0; i < array.length(); i++) {
                    JSONObject innerJSONObject = array.getJSONObject(i);
                    String boardDeviceID = innerJSONObject.getString("board_device_id");
                    String sensorID = innerJSONObject.getString("sensor_id");
                    String nickName = innerJSONObject.getString("nickname");
                    String deviceType = innerJSONObject.getString("device_type");
                    String deviceBrand = innerJSONObject.getString("brand");
                    String deviceModel = innerJSONObject.getString("model");
                    int deviceConsume = innerJSONObject.getInt("consume");
                    lists.add(new PluginItem(boardDeviceID, sensorID, nickName, deviceType,
                            deviceBrand, deviceModel, deviceConsume));
                }
                return lists;
            } else
                Log.e(TAG, "Failed to get JSON object, HTTP statusCode: " + statusCode);
        } catch (Exception e) {
            Log.e(TAG, "Caught an exception");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Delete a plugin for current user.
     *
     * @return the message returned from server, or null if error occurred
     */
    public String deleteAPluginForCurrentUser(PluginItem plugin) {
        try {
            HttpPost request = new HttpPost(Constants.PHP_PLUGIN_DELETE);
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
            nameValuePairs.add(new BasicNameValuePair("email", userEmail));
            nameValuePairs.add(new BasicNameValuePair("auth", authorization));
            nameValuePairs.add(new BasicNameValuePair("deviceID", plugin.getBoardDeviceID()));
            nameValuePairs.add(new BasicNameValuePair("sensorID", plugin.getSensorID()));
            request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toString(entity);
            }else
                Log.e(TAG,
                        "Failed to get deleteAPluginForCurrentUser response entity object, HTTP statusCode: " + statusCode);
        } catch (Exception e) {
            Log.e(TAG, "Caught an exception");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Serialize the object into byte array.
     *
     * Reference:
     * http://stackoverflow.com/questions/3736058/java-object-to-byte-and-byte-to-object-converter-for-tokyo-cabinet
     *
     * @param obj the object need to serialize
     * @return the byte array represent object
     */
    private static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    /**
     * De-serialize the byte array into object.
     *
     * @param data the byte array represent object
     * @return the object been serialized
     */
    private static Object deSerialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    /**
     * Get UTC time stamp from server
     *
     * @return the UTC time string in "yyyyMMddHHmmss" format
     */
    private static String getTimeStampInServerFormat() {
        SimpleDateFormat d = new SimpleDateFormat("yyyyMMddHHmmss");
        d.setTimeZone(TimeZone.getTimeZone("UTC"));
        return d.format(new Date());
    }

    /**
     * Get author's credentials.
     *
     * Reference:
     * SensorLogic_API_5.0_0.6: 3.5.Signature Generation Sample Code (Java)
     *
     * @param currentTimeStampInServerFormat current time stamp in server format
     * @return the UTC time string in "yyyyMMddHHmmss" format
     */
    private String getAuthCredentials(String currentTimeStampInServerFormat)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        String sig = URLEncoder.encode(
                Base64.encodeToString(
                        digest.digest(
                                (Constants.AUTH_ORG + Constants.AUTH_USER +
                                        currentTimeStampInServerFormat + Constants.AUTH_SECRET)
                                        .getBytes("UTF8")), Base64.NO_WRAP), "UTF-8");
        return "&authUser=" + Constants.AUTH_USER + "&authOrg=" + Constants.AUTH_ORG +
                "&authTime=" + currentTimeStampInServerFormat + "&authSig=" + sig;
    }

    /**
     * Convert the UTC format time into milliseconds format which will store in shared preference.
     *
     * @param utcFormatTime the time stamp in UTC format
     * @return the time stamp in milliseconds format
     */
    private long convertTimeFormatFromServerFormatToLong (String utcFormatTime) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date d = format.parse(utcFormatTime);
        return d.getTime();
    }

    /**
     * Convert the milliseconds format time into UTC format.
     *
     * @param seconds the time stamp in milliseconds format
     * @return the time stamp in UTC format
     */
    private String convertTimeFormatFromLongToServerFormat (Long seconds){
        SimpleDateFormat d = new SimpleDateFormat("yyyyMMddHHmmss");
        d.setTimeZone(TimeZone.getTimeZone("UTC"));
        return d.format(seconds);
    }

    /**
     * Get users' comments for a device.
     *
     * @param deviceType type of the device, one of: "AirConditioner", "Microwave",
     *                   or "Refrigerator"
     * @param brand brand of the device
     * @param model model of the device
     * @return a map to contain all comments, or null if error occurred.
     *         Keys are user names and values are comments
     */
    public Map<String, String> getUserComments(String deviceType, String brand, String model) {
        try {
            HttpPost request = new HttpPost(Constants.PHP_COMMENT_GET_FOR_A_DEVICE);
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
            nameValuePairs.add(new BasicNameValuePair("email", userEmail));
            nameValuePairs.add(new BasicNameValuePair("auth", authorization));
            nameValuePairs.add(new BasicNameValuePair("type", deviceType));
            nameValuePairs.add(new BasicNameValuePair("brand", brand));
            nameValuePairs.add(new BasicNameValuePair("model", model));
            request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                JSONObject jsonObject = convertHTTPResponseEntityToJSON(entity);
                HashMap<String, String> list = new HashMap<String, String>();
                int length = jsonObject.getJSONArray("result").length();
                for(int i = 0; i < length; i++){
                    String userEmail = jsonObject.getJSONArray("result")
                            .getJSONObject(i).getString("user_email");
                    String content = jsonObject.getJSONArray("result")
                            .getJSONObject(i).getString("content");
                    list.put(userEmail, content);
                }
                return list;
            }else {
                Log.e(TAG, "Failed to get getUserComments JSON object, HTTP statusCode: " +
                        statusCode);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught an exception.");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get comment for current user for a given device.
     *
     * @param targetPlugin given device info
     * @return A string for user comment content, or null if error occurred
     */
    public String getUserCommentForCurrentUser(PluginItem targetPlugin) {
        try {
            HttpPost request = new HttpPost(Constants.PHP_COMMENT_GET_FOR_A_USER);
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
            nameValuePairs.add(new BasicNameValuePair("email", userEmail));
            nameValuePairs.add(new BasicNameValuePair("auth", authorization));
            nameValuePairs.add(new BasicNameValuePair("type", targetPlugin.getDeviceType()));
            nameValuePairs.add(new BasicNameValuePair("brand", targetPlugin.getDeviceBrand()));
            nameValuePairs.add(new BasicNameValuePair("model", targetPlugin.getDeviceModel()));
            request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                //return convertHTTPResponseEntityToJSON(entity).toString();
                return EntityUtils.toString(entity);
            }else {
                Log.e(TAG,
                        "Failed to get getUserCommentForCurrentUser entity object, HTTP statusCode: " + statusCode);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught an exception");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Edit comment for current user for a given device.
     *
     * @param targetPlugin saved device info
     * @param commentContent content of comment
     * @return A string to show the message received from server, or null if error occurred
     */
    public String editUserComment(PluginItem targetPlugin, String commentContent) {
        try {
            HttpPost request = new HttpPost(Constants.PHP_COMMENT_EDIT);
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
            nameValuePairs.add(new BasicNameValuePair("email", userEmail));
            nameValuePairs.add(new BasicNameValuePair("auth", authorization));
            nameValuePairs.add(new BasicNameValuePair("type", targetPlugin.getDeviceType()));
            nameValuePairs.add(new BasicNameValuePair("brand", targetPlugin.getDeviceBrand()));
            nameValuePairs.add(new BasicNameValuePair("model", targetPlugin.getDeviceModel()));
            // If has new commentContent, add it to entity of POST request.
            if(commentContent != null)
                nameValuePairs.add(new BasicNameValuePair("content", commentContent));
            request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toString(entity);
            }else {
                Log.e(TAG,
                        "Failed to get editUserComment response entity object, HTTP statusCode: " + statusCode);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught an exception");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Use input data to add a plugin in database.
     *
     * @param deviceID input device ID
     * @param sensorID input sensor ID
     * @param nickname input nickname
     * @param deviceType input device type
     * @param brand input brand name
     * @param model input model name
     * @return the message returned from server
     */
    public String addAPlugin(String deviceID, String sensorID, String nickname,
                             String deviceType, String brand, String model) {
        try {
            HttpPost request = new HttpPost(Constants.PHP_PLUGIN_ADD);
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(8);
            nameValuePairs.add(new BasicNameValuePair("email", userEmail));
            nameValuePairs.add(new BasicNameValuePair("auth", authorization));
            nameValuePairs.add(new BasicNameValuePair("deviceID", deviceID));
            nameValuePairs.add(new BasicNameValuePair("sensorID", sensorID));
            nameValuePairs.add(new BasicNameValuePair("nickname", nickname));
            nameValuePairs.add(new BasicNameValuePair("type", deviceType));
            nameValuePairs.add(new BasicNameValuePair("brand", brand));
            nameValuePairs.add(new BasicNameValuePair("model", model));
            request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toString(entity);
            }else {
                Log.e(TAG, "Failed to get addAPlugin JSON object, HTTP statusCode: " + statusCode);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught an exception");
            e.printStackTrace();
        }
        return null;
    }

}
