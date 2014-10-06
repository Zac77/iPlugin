/**
 * A login activity that offers login via email/password.
 *
 * Basic architecture is created by Android Studio when creating a "Login Activity".
 *
 * @author Zac (Qi ZHANG)
 * Create Date: 09/24/2014
 */
package com.dfwexcellerator.iplugin.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dfwexcellerator.iplugin.R;
import com.dfwexcellerator.iplugin.helpers.Constants;
import com.dfwexcellerator.iplugin.helpers.ServerHandle;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity implements LoaderCallbacks<Cursor>{

    // Keep track of the login task to ensure we can cancel it if requested
    private UserLoginTask mAuthTask = null;

    // UI references
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private CheckBox mAutoLogin;
    private View mProgressView;
    private View mLoginFormView;

    // A variable to implement "press back again to quit" feature
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mEmailView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEmailView.setError(null);
                mPasswordView.setError(null);
            }
        });
        populateAutoComplete();
        mPasswordView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEmailView.setError(null);
                mPasswordView.setError(null);
            }
        });
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin(false);
                    return true;
                }
                return false;
            }
        });
        findViewById(R.id.checkBoxShowPassword).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked())
                    mPasswordView.setTransformationMethod(null);
                else
                    mPasswordView.setTransformationMethod(new PasswordTransformationMethod());
            }
        });
        findViewById(R.id.button_log_in).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin(false);
            }
        });
        findViewById(R.id.button_forget_password).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, WebPageActivity.class);
                intent.putExtra(Constants.EXTRA_WEB_PAGE_URL,
                        Constants.WEB_PAGE_ADDRESS_FORGET_PASSWORD);
                startActivity(intent);
            }
        });
        findViewById(R.id.button_register).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, WebPageActivity.class);
                intent.putExtra(Constants.EXTRA_WEB_PAGE_URL, Constants.WEB_PAGE_ADDRESS_REGISTER);
                startActivity(intent);
            }
        });
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.progressBar_login);
        mAutoLogin = (CheckBox) findViewById(R.id.checkBoxAutoLogin);
        mAutoLogin.setChecked(true);

        // Remove force from EditText
        mLoginFormView.requestFocus();

        // Auto Login
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_FILE_NAME, MODE_PRIVATE);
        if (settings.getBoolean(Constants.PREFS_KEY_HAS_SAVED_USER, false)) {
            mEmailView.setText(settings.getString(Constants.PREFS_KEY_SAVED_USER_EMAIL, ""));
            attemptLogin(true);
        }
    }

    /**
     * To implement "press back again to quit" feature.
     *
     * Reference:
     * http://stackoverflow.com/questions/8430805/android-clicking-twice-the-back-button-to-exit-activity
     */
    @Override
    public void onBackPressed() {
        if (mAuthTask != null)
            mAuthTask.cancel(true);
        else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getString(R.string.toast_exit), Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, Constants.TIME_LIMIT_TO_EXIT);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
	 * 
	 * @param useSavedUser set to true if want to use saved user information, otherwise set to false
     */
    public void attemptLogin(boolean useSavedUser) {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        if (!useSavedUser) {
            boolean cancel = false;
            View focusView = null;

            // Check for a valid input
            if (TextUtils.isEmpty(email)) {
                mEmailView.setError(getString(R.string.error_text_view_email_required));
                focusView = mEmailView;
                cancel = true;
            } else if (!isEmailValid(email)) {
                mEmailView.setError(getString(R.string.error_text_view_invalid_email));
                focusView = mEmailView;
                cancel = true;
            } else if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
                mPasswordView.setError(getString(R.string.error_text_view_invalid_password));
                focusView = mPasswordView;
                cancel = true;
            }

            if (cancel) {
                /**
                 * There was an error; don't attempt login and focus the first
                 * form field with an error.
                 */
                focusView.requestFocus();
                return;
            }
        }
        /**
         * Show a progress spinner, and kick off a background task to
         * perform the user login attempt.
         */
        showProgress(true);
        mAuthTask = new UserLoginTask(email, password, useSavedUser);
        mAuthTask.execute((Void) null);
    }

    /**
     * Check if a email address is valid.
     *
     * Reference:
     * http://stackoverflow.com/questions/1819142/how-should-i-validate-an-e-mail-address
     *
     * @param email input email address
     * @return true if valid, or false if not valid
     */
    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Check if a password is valid.
     *
     * A valid password in this app should fellow these rules:
     * At least 8 characters in length.
     *
     * @param password input password
     * @return true if valid, or false if not valid
     */
    private boolean isPasswordValid(String password) {
        return (password.length() >= 8);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     *
     * Authentication reference:
     * http://blog.leocad.io/basic-http-authentication-on-android/
     * and then use URL encoder to encode.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "UserLoginTask";

        private final String mEmail;
        private final String mPassword;
        private final boolean mUseSavedUser;
        private String mAuth;

        UserLoginTask(String email, String password, boolean useSavedUser) {
            mEmail = email;
            mPassword = password;
            mUseSavedUser = useSavedUser;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (mUseSavedUser)
                mAuth = getSharedPreferences(Constants.PREFS_FILE_NAME, MODE_PRIVATE)
                        .getString(Constants.PREFS_KEY_SAVED_USER_AUTH, "");
            else {
                String credentials = mEmail + ":" + mPassword;
                try {
                    mAuth = URLEncoder.encode(Base64.encodeToString(
                            credentials.getBytes(), Base64.NO_WRAP), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "Invalid charSetName.");
                    e.printStackTrace();
                }
            }
            ServerHandle.INSTANCE.setServerHandleAuthorization(mEmail, mAuth);
            return ServerHandle.INSTANCE.authorizeUserInfo();
        }

        @Override
        protected void onPostExecute(final Boolean response) {
            mAuthTask = null;
            showProgress(false);

            if (response == null) {
                Toast.makeText(LoginActivity.this,
                        R.string.toast_internet_error, Toast.LENGTH_LONG).show();
            } else if (response) {
                if (mAutoLogin.isChecked())
                    getSharedPreferences(Constants.PREFS_FILE_NAME, MODE_PRIVATE)
                            .edit()
                            .putBoolean(Constants.PREFS_KEY_HAS_SAVED_USER, true)
                            .putString(Constants.PREFS_KEY_SAVED_USER_AUTH, mAuth)
                            .putString(Constants.PREFS_KEY_SAVED_USER_EMAIL, mEmail)
                            .apply();
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(i);
            } else {
                mPasswordView.setError(getString(R.string.error_text_view_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    /**
     * Below part is generated by Android Studio.
     */
    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }

    // Shows the progress UI and hides the login form
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

}
