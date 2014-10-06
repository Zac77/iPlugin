/**
 * A fragment to show detail information of a plugin.
 *
 * @author Zac (Qi ZHANG) and Trent (Quan ZHANG)
 * Created by Zac on 10/02/2014.
 * Trent helped one part with comment mentioned.
 */
package com.dfwexcellerator.iplugin.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ConfigurationInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dfwexcellerator.iplugin.R;
import com.dfwexcellerator.iplugin.helpers.Constants;
import com.dfwexcellerator.iplugin.helpers.LogItem;
import com.dfwexcellerator.iplugin.helpers.PluginItem;
import com.dfwexcellerator.iplugin.helpers.ServerHandle;
import com.dfwexcellerator.iplugin.trent3d.CubeRenderer;
import com.dfwexcellerator.iplugin.trent3d.CubeSurfaceView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

public class PluginDetailFragment extends BasementFragment {

    private Activity mContext;
    private PluginItem mPluginItem;
    private CubeRenderer mRenderer;
    private CubeSurfaceView mCubeSurfaceView;
    private View mProgressLayout;
    private View mContentLayout;
    private com.github.mikephil.charting.charts.LineChart mChart;
    private Spinner mTimeRangeSpinner;
    private TextView mMessageView;
    private TextView mConsumeTextView;
    private ProgressBar mConsumeProgressBar;
    private Button mRefreshButton;
    private EditText mCommentEditText;
    private Button mSaveCommentButton;

    private boolean drawChartTaskFinish;
    private boolean getCommentTaskFinish;

    /**
     * A static method to create a new instance for this fragment.
     *
     * @param item the PluginItem object which will be used for this fragment
     * @return a PluginDetailFragment fragment
     */
    public static PluginDetailFragment newInstance(PluginItem item, Activity context) {
        PluginDetailFragment f = new PluginDetailFragment();
        f.setPluginItem(item);
        f.setContext(context);
        return f;
    }

    public PluginDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_plugin_detail, container, false);

        mCubeSurfaceView = (CubeSurfaceView) rootView.findViewById(R.id.view_3d);
        mProgressLayout = rootView.findViewById(R.id.progressBar_plugin_detail);
        mContentLayout = rootView.findViewById(R.id.layout_plugin_detail);
        mChart = (com.github.mikephil.charting.charts.LineChart)
                rootView.findViewById(R.id.chart_plugin_detail);
        mTimeRangeSpinner = (Spinner) rootView
                .findViewById(R.id.spinner_select_time_range_in_plugin_detail);
        mMessageView = (TextView) rootView.findViewById(
                R.id.textView_message_in_fragment_for_plugin_detail);
        mRefreshButton = (Button) rootView.findViewById(
                R.id.button_refresh_in_fragment_for_plugin_detail);
        mConsumeTextView = (TextView) rootView.findViewById(
                R.id.textView_consume_in_fragment_for_plugin_detail);
        mConsumeProgressBar = (ProgressBar) rootView.findViewById(
                R.id.progressBar_consume_plugin_detail);
        mCommentEditText = (EditText) rootView.findViewById(R.id.editText_comment);
        mSaveCommentButton = (Button) rootView.findViewById(
                R.id.button_save_comment_in_plugin_detail);

        // Set up the chart
        mChart.setStartAtZero(true);
        mChart.setDrawBorder(false);
        mChart.setDrawLegend(false);
        mChart.setDrawYValues(false);
        mChart.setTouchEnabled(false);
        mChart.setDragScaleEnabled(false);
        mChart.setPinchZoom(false);
        mChart.setDrawGridBackground(false);
        mChart.setDrawVerticalGrid(false);
        mChart.setDescription("");

        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupUI((String) mTimeRangeSpinner.getSelectedItem());
            }
        });
        mTimeRangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setupUI((String) mTimeRangeSpinner.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSaveCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTask1 = new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... params) {
                        mBackgroundThread1 = Thread.currentThread();
                        return ServerHandle.INSTANCE.editUserComment(
                                mPluginItem, mCommentEditText.getText().toString());
                    }

                    @Override
                    protected void onPostExecute(final String response) {
                        if (response == null || !response.contains("Successfully")) {
                            // Some errors occurred
                            Toast.makeText(getActivity(), (response == null) ?
                                    getString(R.string.text_error_in_getting_comments) :
                                    response, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getActivity(), response, Toast.LENGTH_LONG).show();
                            setupUI((String) mTimeRangeSpinner.getSelectedItem());
                        }
                    }
                }.execute((Void) null);
            }
        });
        rootView.findViewById(R.id.button_delete_plugin)
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.alert_dialog_message_delete_plugin)
                        .setCancelable(true)
                        .setPositiveButton(R.string.alert_dialog_button_ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mTask1 = new AsyncTask<Void, Void, String>() {
                                            @Override
                                            protected String doInBackground(Void... params) {
                                                mBackgroundThread1 = Thread.currentThread();
                                                return ServerHandle.INSTANCE.
                                                        deleteAPluginForCurrentUser(mPluginItem);
                                            }

                                            @Override
                                            protected void onPostExecute(final String response) {
                                                if (response == null ||
                                                        !response.contains("Successfully")) {
                                                    // Some errors occurred
                                                    Toast.makeText(getActivity(),
                                                            (response == null) ?
                                                            getString(R
                                                                    .string
                                                                    .text_error_in_getting_comments)
                                                            :
                                                            response,
                                                            Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(getActivity(), response,
                                                            Toast.LENGTH_LONG).show();
                                                    getActivity().
                                                            getSharedPreferences(
                                                                    Constants.PREFS_SENSOR_DATA_FILE_NAME,
                                                                    Context.MODE_PRIVATE)
                                                            .edit()
                                                            .remove(mPluginItem.getBoardDeviceID() +
                                                                    mPluginItem.getSensorID() +
                                                                    Constants.SUFFIX_LAST_TIMESTAMP)
                                                            .remove(mPluginItem.getBoardDeviceID() +
                                                                    mPluginItem.getSensorID() +
                                                                    Constants.SUFFIX_POWER_CONSUME)
                                                            .remove(mPluginItem.getBoardDeviceID() +
                                                                    mPluginItem.getSensorID() +
                                                                    Constants.SUFFIX_SAVED_DATA_SET)
                                                            .apply();
                                                    getFragmentManager()
                                                            .beginTransaction()
                                                            .replace(R.id.container,
                                                                    new MyPluginsFragment())
                                                            .commit();
                                                }
                                            }
                                        }.execute((Void) null);
                                    }
                                })
                        .setNegativeButton(R.string.alert_dialog_button_cancel, null)
                        .show();
            }
        });

        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
        if(supportsEs2){
            // Request an OpenGL ES 2.0 compatible context.
            mCubeSurfaceView.setEGLContextClientVersion(2);

            final DisplayMetrics displayMetrics = new DisplayMetrics();
            mContext.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            // Set the renderer to our demo renderer
            String[] deviceTypeArray = mContext.getResources().getStringArray(R.array.device_type);
            for (int i = 0; i < deviceTypeArray.length; i++)
                if (deviceTypeArray[i].equals(mPluginItem.getDeviceType())) {
                    mRenderer = new CubeRenderer(mContext, i);
                    break;
                }
            mCubeSurfaceView.setRenderer(mRenderer, displayMetrics.density);

            /**
             * Set the height of our 3D view to 1/3 screen height
             *
             * Reference:
             * http://stackoverflow.com/questions/6465680/how-to-determine-the-screen-width-in-terms-of-dp-or-dip-at-runtime-in-android
             */
            Display display = mContext.getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);
            float density  = getResources().getDisplayMetrics().density;
            int dpHeight = (int) (outMetrics.heightPixels / density / 3.0);
            mCubeSurfaceView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpHeight));
        } else {
            // This is where you could create an OpenGL ES 1.x compatible
            // renderer if you wanted to support both ES 1 and ES 2.
            mCubeSurfaceView.setVisibility(View.GONE);
        }

        setupUI((String) mTimeRangeSpinner.getSelectedItem());

        return rootView;
    }

    private void setPluginItem(PluginItem item) {
        this.mPluginItem = item;
    }

    public void setContext(Activity mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onResume() {
        // The activity must call the GL surface view's onResume() on activity onResume().
        super.onResume();
        mCubeSurfaceView.onResume();
    }

    @Override
    public void onPause() {
        // The activity must call the GL surface view's onPause() on activity onPause().
        super.onPause();
        mCubeSurfaceView.onPause();
    }

    /**
     * Shows the progress UI and hides the other UIs.
     *
     * Reference:
     * Auto-generated code from creating a "Login Activity" in Android Studio
     *
     * @param show true if want to show the process bar, or false not to show
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mContentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            mContentLayout.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mContentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressLayout.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressLayout.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressLayout.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressLayout.setVisibility(show ? View.VISIBLE : View.GONE);
            mContentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void setupUI(final String timeRange) {
        showProgress(true);
        drawChartTaskFinish = false;
        getCommentTaskFinish = false;
        mMessageView.setVisibility(View.GONE);
        mRefreshButton.setVisibility(View.GONE);
        mTask1 = new AsyncTask<Void, Void, List<LogItem>>() {
            @Override
            protected List<LogItem> doInBackground(Void... params) {
                mBackgroundThread1 = Thread.currentThread();
                return ServerHandle.INSTANCE
                        .getChartDataForAPlugin(mPluginItem, timeRange, getActivity());
            }

            @Override
            protected void onPostExecute(final List<LogItem> response) {
                if (response == null) { // Some errors occurred
                    mMessageView.setText(R.string.text_error_try_again);
                    mMessageView.setVisibility(View.VISIBLE);
                    mRefreshButton.setVisibility(View.VISIBLE);
                    mChart.setVisibility(View.GONE);
                    mTimeRangeSpinner.setVisibility(View.GONE);
                    mConsumeTextView.setVisibility(View.GONE);
                    mConsumeProgressBar.setVisibility(View.GONE);
                    mCommentEditText.setVisibility(View.GONE);
                    mSaveCommentButton.setVisibility(View.GONE);
                } else {
                    ArrayList<String> xVals = new ArrayList<String>(response.size());
                    ArrayList<Entry> vals1 = new ArrayList<Entry>(response.size());

                    /**
                     * Convert time format.
                     *
                     * This part is coded by Trent.
                     */
                    for (int i = 0; i < response.size(); i++) {
                        try {
                            Calendar c = Calendar.getInstance();
                            c.setTimeZone(TimeZone.getTimeZone("UTC"));
                            c.setTimeInMillis(response.get(i).getTimeStamp());
                            SimpleDateFormat sdfu = new SimpleDateFormat("hhmm");
                            sdfu.setTimeZone(TimeZone.getDefault());
                            String tempLocalTime = sdfu.format(c.getTime());

                            String localTime = tempLocalTime.substring(0, 2) + ':' +
                                    tempLocalTime.substring(2, 4);
                            xVals.add(" " + localTime);
                        } catch (Exception e) {
                            Log.e("PluginDetailFragment", "Parse format exception");
                            e.printStackTrace();
                        }

                        float val = response.get(i).getValue();
                        vals1.add(new Entry(val, i));
                    }

                    // create a dataset and give it a type
                    LineDataSet set1 = new LineDataSet(vals1, "");
                    set1.setDrawCubic(true);
                    set1.setCubicIntensity(0.2f);
                    set1.setDrawFilled(true);
                    set1.setDrawCircles(false);
                    set1.setLineWidth(2f);
                    set1.setCircleSize(5f);
                    set1.setHighLightColor(Color.rgb(244, 117, 117));
                    set1.setColor(Color.rgb(104, 241, 175));

                    ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
                    dataSets.add(set1);

                    // create a data object with the datasets
                    LineData data = new LineData(xVals, dataSets);

                    // set data
                    mChart.setData(data);

                    // Re-draw the chart
                    mChart.invalidate();
                }
                // Set up UI
                if (mMessageView.getVisibility() == View.GONE) {
                    mChart.setVisibility(View.VISIBLE);
                    mTimeRangeSpinner.setVisibility(View.VISIBLE);
                    mConsumeTextView.setVisibility(View.VISIBLE);
                    mConsumeProgressBar.setVisibility(View.VISIBLE);
                    mCommentEditText.setVisibility(View.VISIBLE);
                    mSaveCommentButton.setVisibility(View.VISIBLE);
                }
                drawChartTaskFinish = true;
                if (getCommentTaskFinish)
                    showProgress(false);
            }
        }.execute((Void) null);
        mTask2 = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                mBackgroundThread2 = Thread.currentThread();
                return ServerHandle.INSTANCE.getUserCommentForCurrentUser(mPluginItem);
            }

            @Override
            protected void onPostExecute(final String response) {
                if (response == null) { // Some errors occurred
                    mMessageView.setText(R.string.text_error_try_again);
                    mMessageView.setVisibility(View.VISIBLE);
                    mRefreshButton.setVisibility(View.VISIBLE);
                    mChart.setVisibility(View.GONE);
                    mTimeRangeSpinner.setVisibility(View.GONE);
                    mConsumeTextView.setVisibility(View.GONE);
                    mConsumeProgressBar.setVisibility(View.GONE);
                    mCommentEditText.setVisibility(View.GONE);
                    mSaveCommentButton.setVisibility(View.GONE);
                } else {
                    if (!response.equals(""))
                        mCommentEditText.setText(response);
                    else
                        mCommentEditText.setText(null);

                    /**
                     * The rate for "Your annual electricity consumption beats ?% users" should be
                     * calculated from data. But we don't have enough data now, so we just create
                     * a random rate to use.
                     */
                    Random randomGenerator = new Random(System.currentTimeMillis());
                    int rate = randomGenerator.nextInt(100);

                    mConsumeTextView.setText(
                            getResources().getString(R.string.text_consume_for_a_plugin_begin) +
                                    " " + rate +
                                    getResources()
                                            .getString(R.string.text_consume_for_a_plugin_end));
                    mConsumeProgressBar.setProgress(rate);
                }

                // Set up UI
                if (mMessageView.getVisibility() == View.GONE) {
                    mChart.setVisibility(View.VISIBLE);
                    mTimeRangeSpinner.setVisibility(View.VISIBLE);
                    mConsumeTextView.setVisibility(View.VISIBLE);
                    mConsumeProgressBar.setVisibility(View.VISIBLE);
                    mCommentEditText.setVisibility(View.VISIBLE);
                    mSaveCommentButton.setVisibility(View.VISIBLE);
                }

                getCommentTaskFinish = true;
                if (drawChartTaskFinish)
                    showProgress(false);
            }
        }.execute((Void) null);
    }

}
