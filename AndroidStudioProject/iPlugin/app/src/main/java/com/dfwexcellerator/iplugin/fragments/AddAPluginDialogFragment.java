/**
 * A dialog fragment to show "Add a Plugin" alert dialog.
 *
 * Reference:
 * http://developer.android.com/guide/topics/ui/dialogs.html
 *
 * @author Zac (Qi ZHANG)
 * Created on 09/29/2014.
 */
package com.dfwexcellerator.iplugin.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.dfwexcellerator.iplugin.R;
import com.dfwexcellerator.iplugin.helpers.DeviceSelectorInitializer;
import com.dfwexcellerator.iplugin.helpers.ServerHandle;

public class AddAPluginDialogFragment extends DialogFragment {
    public static final String TAG = "AddAPluginDialogFragment";

    // UI references
    EditText mDeviceIDEditText;
    EditText mSensorIDEditText;
    EditText mNicknameEditText;
    Spinner mDeviceTypeSpinner;
    Spinner mBrandSpinner;
    Spinner mModelSpinner;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View view = getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_add_a_plugin, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.alert_dialog_title_add_a_plugin)
                .setView(view)
                .setCancelable(true)
                .setPositiveButton(R.string.alert_dialog_button_ok,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton(R.string.alert_dialog_button_cancel,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return builder.create();
    }

    /**
     * Recode the OnClickListener in this dialog to prevent
     * the dialog from closing when a button is clicked.
     *
     * Reference:
     * http://stackoverflow.com/questions/2620444/how-to-prevent-a-dialog-from-closing-when-a-button-is-clicked/15619098#15619098
     */
    @Override
    public void onStart() {
        super.onStart();
        AlertDialog d = (AlertDialog) getDialog();
        if(d != null) {
            mDeviceIDEditText = (EditText) d.findViewById(R.id.editText_device_id);
            mSensorIDEditText = (EditText) d.findViewById(R.id.editText_sensor_id);
            mNicknameEditText = (EditText) d.findViewById(R.id.editText_nick_name_in_add_a_plugin);
            mDeviceTypeSpinner = (Spinner) d.findViewById(R.id.spinner_add_device_type);
            mBrandSpinner = (Spinner) d.findViewById(R.id.spinner_add_brand);
            mModelSpinner = (Spinner) d.findViewById(R.id.spinner_add_model);

            // Set up all 3 spinners
            DeviceSelectorInitializer.INSTANCE.setup(getActivity(),
                    mDeviceTypeSpinner, mBrandSpinner, mModelSpinner);

            // Set up "OK" button
            d.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    if (checkInputValidation()) {
                        // Use an AsyncTask to add this new plugin to database
                        new AsyncTask<Void, Void, String>() {
                            @Override
                            protected String doInBackground(Void... params) {
                                return ServerHandle.INSTANCE.addAPlugin(
                                        mDeviceIDEditText.getText().toString(),
                                        mSensorIDEditText.getText().toString(),
                                        mNicknameEditText.getText().toString(),
                                        (String) mDeviceTypeSpinner.getSelectedItem(),
                                        (String) mBrandSpinner.getSelectedItem(),
                                        (String) mModelSpinner.getSelectedItem());
                            }

                            @Override
                            protected void onPostExecute(final String response) {
                                if (response == null) {
                                    Toast.makeText(getActivity(),
                                            R.string.toast_internet_error, Toast.LENGTH_LONG)
                                            .show();
                                } else if (response.contains("Successfully")) {
                                    Toast.makeText(getActivity(),
                                            R.string.toast_add_a_plugin_success, Toast.LENGTH_LONG)
                                            .show();

                                    // Refresh the list for all plugins
                                    ((MyPluginsFragment) getFragmentManager()
                                            .findFragmentById(R.id.container))
                                            .getPluginsList();

                                    // Dismiss this dialog
                                    dismiss();
                                } else {
                                    Toast.makeText(getActivity(), response, Toast.LENGTH_LONG)
                                            .show();
                                    mDeviceIDEditText.setError(
                                            getString(R.string.error_text_view_incorrect_device));
                                    mDeviceIDEditText.requestFocus();
                                    mSensorIDEditText.setError(
                                            getString(R.string.error_text_view_incorrect_device));
                                }
                            }
                        }.execute((Void) null);
                    }
                }
            });
        }

    }

    /**
     * Check if user input data is valid.
     *
     * @return true if valid, or false if invalid
     */
    private boolean checkInputValidation() {
        if (mDeviceIDEditText.getText().toString().length() == 0) {
            mDeviceIDEditText.setError(getString(R.string.error_text_view_device_id_required));
            return false;
        }
        if (mSensorIDEditText.getText().toString().length() == 0) {
            mSensorIDEditText.setError(getString(R.string.error_text_view_sensor_id_required));
            return false;
        }
        if (mNicknameEditText.getText().toString().length() == 0) {
            mNicknameEditText.setError(getString(R.string.error_text_view_nick_name_required));
            return false;
        }
        if (mDeviceTypeSpinner.getSelectedItemPosition() == 0 ||
                mBrandSpinner.getSelectedItemPosition() == 0 ||
                mModelSpinner.getSelectedItemPosition() == 0) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.alert_dialog_message_invalid_device)
                    .setCancelable(true)
                    .setPositiveButton(R.string.alert_dialog_button_ok, null)
                    .show();
            return false;
        }
        return true;
    }

}
