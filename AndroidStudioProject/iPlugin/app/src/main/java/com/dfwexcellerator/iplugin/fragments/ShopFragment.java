/**
 * A fragment to show "Shop" panel.
 *
 * @author Zac (Qi ZHANG)
 * Created on 09/25/2014.
 */
package com.dfwexcellerator.iplugin.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.dfwexcellerator.iplugin.R;
import com.dfwexcellerator.iplugin.activities.WebPageActivity;
import com.dfwexcellerator.iplugin.helpers.Constants;
import com.dfwexcellerator.iplugin.helpers.DeviceSelectorInitializer;
import com.dfwexcellerator.iplugin.helpers.ServerHandle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ShopFragment extends BasementFragment {

    // UI references
    private Spinner mDeviceTypeSpinner;
    private Spinner mBrandSpinner;
    private Spinner mModelSpinner;
    private View mProgressView;
    private ListView mListView;
    private Button mRefreshButton;
    private TextView mMessageView;
    private View mCommentsLayout;

    // For the Adapter being used to display the list's data
    SimpleAdapter mAdapter;
    private static final String COLUMNS_NAME_1 = "userName";
    private static final String COLUMNS_NAME_2 = "comment";
    private static final String[] FROM_COLUMNS = {COLUMNS_NAME_1, COLUMNS_NAME_2};
    private static final int[] TO_VIEWS = {R.id.textView_user_name, R.id.textView_comment};

    public ShopFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_shop, container, false);

        mDeviceTypeSpinner = (Spinner) rootView.findViewById(R.id.spinner_device_type);
        mBrandSpinner = (Spinner) rootView.findViewById(R.id.spinner_brand);
        mModelSpinner = (Spinner) rootView.findViewById(R.id.spinner_model);
        mProgressView = rootView.findViewById(R.id.progressBar_shop);
        mListView = (ListView) rootView.findViewById(R.id.listView_comments);
        mRefreshButton = (Button) rootView.findViewById(R.id.button_refresh_in_shop);
        mMessageView = (TextView) rootView.findViewById(R.id.textView_message_in_shop);
        mCommentsLayout = rootView.findViewById(R.id.layout_shop_content);

        // Setup OnItemSelectedListeners for all 3 spinners
        DeviceSelectorInitializer.INSTANCE.setup(getActivity(),
                mDeviceTypeSpinner, mBrandSpinner, mModelSpinner);
        mModelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    showProgress(false);
                    mCommentsLayout.setVisibility(View.GONE);
                    mMessageView.setVisibility(View.GONE);
                    mRefreshButton.setVisibility(View.GONE);
                } else
                    prepareListView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Set input data to the spinners
        mDeviceTypeSpinner.setSelection(0);

        // Set OnClickListeners to buttons
        rootView.findViewById(R.id.button_go_to_amazon).
                setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder sb;
                if (mDeviceTypeSpinner.getSelectedItemPosition() != 0) {
                    sb = new StringBuilder(Constants.AMAZON_PREFIX);
                    sb.append((String) mDeviceTypeSpinner.getSelectedItem());
                    if (mBrandSpinner.getSelectedItemPosition() != 0) {
                        sb.append('+').append((String) mBrandSpinner.getSelectedItem());
                        if (mModelSpinner.getSelectedItemPosition() != 0) {
                            sb.append('+').append((String) mModelSpinner.getSelectedItem());
                        }
                    }
                    sb.append(Constants.AMAZON_SUFFIX);
                } else
                    sb = new StringBuilder(Constants.AMAZON);
                Intent intent = new Intent(getActivity(), WebPageActivity.class);
                intent.putExtra(Constants.EXTRA_WEB_PAGE_URL, sb.toString());
                startActivity(intent);
            }
        });
        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareListView();
            }
        });

        return rootView;
    }

    /**
     * Shows the progress UI and hides the comments list.
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

            mCommentsLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            mCommentsLayout.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCommentsLayout.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mCommentsLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Get users' comments from server and prepare the listView to show them.
     *
     * Reference:
     * http://www.vogella.com/tutorials/AndroidListView/article.html
     * 13. Tutorial: How to display two items in a ListView
     */
    private void prepareListView() {
        // Set up UI
        showProgress(true);
        mRefreshButton.setVisibility(View.GONE);
        mMessageView.setVisibility(View.GONE);

        // Use an AsyncTask to get data from server
        mTask1 = new AsyncTask<Void, Void, Map<String, String>>() {
            @Override
            protected Map<String, String> doInBackground(Void... params) {
                mBackgroundThread1 = Thread.currentThread();
                return ServerHandle.INSTANCE.getUserComments(
                        (String) mDeviceTypeSpinner.getSelectedItem(),
                        (String) mBrandSpinner.getSelectedItem(),
                        (String) mModelSpinner.getSelectedItem()
                );
            }

            @Override
            protected void onPostExecute(final Map<String, String> response) {
                if (response == null) { // Some errors occurred
                    mListView.setVisibility(View.GONE);
                    mMessageView.setText(R.string.text_error_in_getting_comments);
                    mMessageView.setVisibility(View.VISIBLE);
                    mRefreshButton.setVisibility(View.VISIBLE);
                } else if (response.size() == 0) { // No comments found
                    mListView.setVisibility(View.GONE);
                    mMessageView.setText(R.string.text_no_comments);
                    mMessageView.setVisibility(View.VISIBLE);
                } else { // Set up the listView to show comments
                    // Set up adapter
                    ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
                    for(Entry<String, String> entry : response.entrySet()) {
                        HashMap<String, String> item = new HashMap<String, String>(2);
                        item.put(COLUMNS_NAME_1, entry.getKey());
                        item.put(COLUMNS_NAME_2, entry.getValue());
                        list.add(item);
                    }
                    mAdapter = new SimpleAdapter(getActivity(), list,
                            R.layout.list_item_comments_in_shop_fragment, FROM_COLUMNS, TO_VIEWS);
                    mListView.setAdapter(mAdapter);

                    // Show the list
                    mListView.setVisibility(View.VISIBLE);
                }
                showProgress(false);
            }
        }.execute((Void) null);
    }

}
