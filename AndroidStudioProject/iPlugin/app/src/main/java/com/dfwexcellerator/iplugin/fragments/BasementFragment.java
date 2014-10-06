/**
 * A basement fragment for this project.
 * This fragment is used to set up some variables and methods to cancel AsyncTask.
 *
 * @author Zac (Qi ZHANG)
 * Created on 10/06/2014.
 */
package com.dfwexcellerator.iplugin.fragments;

import android.app.Fragment;
import android.os.AsyncTask;
import android.util.Log;

public class BasementFragment extends Fragment{
    protected AsyncTask mTask1 = null;
    protected AsyncTask mTask2 = null;
    protected Thread mBackgroundThread1 = null;
    protected Thread mBackgroundThread2 = null;

    public void cancelAsyncTask() {
        try {
            mBackgroundThread1.interrupt();
            mTask1.cancel(true);
            if (mTask2 != null) {
                mBackgroundThread2.interrupt();
                mTask2.cancel(true);
            }
        } catch (Exception e) {
            Log.d("BasementFragment", "Caught an expectable exception: " + e.getMessage());
        }
    }

}
