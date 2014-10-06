/**
 * A fragment to show "My Plugins" panel.
 *
 * @author Zac (Qi ZHANG)
 * Created on 09/25/2014.
 */
package com.dfwexcellerator.iplugin.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dfwexcellerator.iplugin.R;
import com.dfwexcellerator.iplugin.activities.MainActivity;
import com.dfwexcellerator.iplugin.helpers.PluginItem;
import com.dfwexcellerator.iplugin.helpers.PluginAdapter;
import com.dfwexcellerator.iplugin.helpers.ServerHandle;

import java.util.List;

public class MyPluginsFragment extends BasementFragment {

    // UI references
    private View mProgressView;
    private ProgressBar mProgressBar;
    private View mContentView;
    private Button mRefreshButton;
    private TextView mMessageView;
    private TextView mProgressTextView;
    private ListView mPluginsListView;

    // For the Adapter being used to display the list's data
    PluginAdapter mAdapter;

    public MyPluginsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_my_plugins, container, false);

        mProgressView = rootView.findViewById(R.id.layout_my_plugins_progressBar);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar_my_plugins);
        mContentView = rootView.findViewById(R.id.layout_my_plugins_content);
        mRefreshButton = (Button) rootView.findViewById(R.id.button_refresh_in_my_plugins);
        mMessageView = (TextView) rootView.findViewById(R.id.textView_message_in_my_plugins);
        mPluginsListView = (ListView) rootView.findViewById(R.id.listView_plugins);
        mProgressTextView = (TextView) rootView.findViewById(R.id.textView_progress);

        // Set OnClickListeners to buttons
        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPluginsList();
            }
        });
        rootView.findViewById(R.id.button_add_new_plugin).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFragment dialog = new AddAPluginDialogFragment();
                        dialog.show(getFragmentManager(), AddAPluginDialogFragment.TAG);
                    }
                });

        // Set up the plugins list
        getPluginsList();

        return rootView;
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

            mContentView.setVisibility(show ? View.GONE : View.VISIBLE);
            mContentView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mContentView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mContentView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Get a list for all user's plugins.
     */
    public void getPluginsList() {
        // Set up UI
        showProgress(true);
        mRefreshButton.setVisibility(View.GONE);
        mMessageView.setVisibility(View.GONE);

        // Use an AsyncTask to get data from server
        mTask1 = new AsyncTask<Void, Void, List<PluginItem>>() {
            @Override
            protected List<PluginItem> doInBackground(Void... params) {
                mBackgroundThread1 = Thread.currentThread();
                return ServerHandle.INSTANCE.getPluginsList(getActivity(),
                        mProgressBar, mProgressTextView);
            }

            @Override
            protected void onPostExecute(final List<PluginItem> response) {
                if (response == null) { // Some errors occurred
                    mMessageView.setText(R.string.text_error_in_getting_devices);
                    mMessageView.setVisibility(View.VISIBLE);
                    mRefreshButton.setVisibility(View.VISIBLE);
                    mPluginsListView.setVisibility(View.GONE);
                } else if (response.size() == 0) { // No plugins found
                    mMessageView.setText(R.string.text_no_plugins);
                    mMessageView.setVisibility(View.VISIBLE);
                    mPluginsListView.setVisibility(View.GONE);
                } else { // Set up the list
                    // Set up an adapter
                    mAdapter = new PluginAdapter(getActivity(),
                            response.toArray(new PluginItem[response.size()]));
                    mPluginsListView.setAdapter(mAdapter);

                    // Add OnItemClickListener to the list
                    mPluginsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent,
                                                View view, int position, long id) {
                            ((MainActivity) getActivity()).setNeedBackToMyPluginsFragment(true);
                            getFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.container,
                                            PluginDetailFragment
                                                    .newInstance(
                                                            (PluginItem) parent.getAdapter()
                                                                    .getItem(position),
                                                            getActivity()))
                                    .commit();
                        }
                    });

                    // Set up UI
                    mPluginsListView.setVisibility(View.VISIBLE);
                }
                showProgress(false);
            }
        }.execute((Void) null);
    }

}
