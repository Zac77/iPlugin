/**
 * An enum type to initialize spinners to used for a device selector.
 *
 * Used "Singleton pattern" for this enum class.
 * Reference:
 * http://en.wikipedia.org/wiki/Singleton_pattern
 * "The enum way" part
 *
 * @author Zac (Qi ZHANG)
 * Created on 09/27/2014.
 */
package com.dfwexcellerator.iplugin.helpers;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.dfwexcellerator.iplugin.R;

public enum DeviceSelectorInitializer {
    INSTANCE;

    private static final String TAG = "DeviceSelectorInitializer";

    /**
     * Setup all 3 spinners to used for a device selector.
     *
     * @param context the application's environment
     * @param deviceTypeSpinner the spinner used to select device type
     * @param brandSpinner the spinner used to select brand
     * @param modelSpinner the spinner used to select model
     */
    public void setup(final Context context, final Spinner deviceTypeSpinner,
                      final Spinner brandSpinner, final Spinner modelSpinner) {
        deviceTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        setupSpinner(context, brandSpinner, R.array.brands_empty, false);
                        break;
                    case 1:
                        setupSpinner(context, brandSpinner, R.array.brands_air_conditioner, true);
                        break;
                    case 2:
                        setupSpinner(context, brandSpinner, R.array.brands_microwave, true);
                        break;
                    case 3:
                        setupSpinner(context, brandSpinner, R.array.brands_refrigerator, true);
                        break;
                    default:
                        Log.e(TAG,
                                "The OnItemSelectedListener for the device type spinner got " +
                                        "a wrong parameter: position = "
                                        + position);
                }
                setupSpinner(context, modelSpinner, R.array.models_empty, false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        brandSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (deviceTypeSpinner.getSelectedItemPosition()) {
                    case 0:
                        break;
                    case 1:
                        switch (position) {
                            case 0:
                                setupSpinner(context, modelSpinner,
                                        R.array.models_empty, false);
                                break;
                            case 1:
                                setupSpinner(context, modelSpinner,
                                        R.array.models_ac_honeywell, true);
                                break;
                            case 2:
                                setupSpinner(context, modelSpinner,
                                        R.array.models_ac_lg, true);
                                break;
                            case 3:
                                setupSpinner(context, modelSpinner,
                                        R.array.models_ac_whynter, true);
                                break;
                            default:
                                Log.e(TAG,
                                        "The OnItemSelectedListener for the brand spinner got " +
                                                "a wrong parameter: position = "
                                                + position);
                        }
                        break;
                    case 2:
                        switch (position) {
                            case 0:
                                setupSpinner(context, modelSpinner,
                                        R.array.models_empty, false);
                                break;
                            case 1:
                                setupSpinner(context, modelSpinner,
                                        R.array.models_mw_bosch, true);
                                break;
                            case 2:
                                setupSpinner(context, modelSpinner,
                                        R.array.models_mw_electrolux, true);
                                break;
                            case 3:
                                setupSpinner(context, modelSpinner,
                                        R.array.models_mw_ge, true);
                                break;
                            default:
                                Log.e(TAG,
                                        "The OnItemSelectedListener for the brand spinner got " +
                                                "a wrong parameter: position = "
                                                + position);
                        }
                        break;
                    case 3:
                        switch (position) {
                            case 0:
                                setupSpinner(context, modelSpinner,
                                        R.array.models_empty, false);
                                break;
                            case 1:
                                setupSpinner(context, modelSpinner,
                                        R.array.models_r_ge, true);
                                break;
                            case 2:
                                setupSpinner(context, modelSpinner,
                                        R.array.models_r_lg, true);
                                break;
                            case 3:
                                setupSpinner(context, modelSpinner,
                                        R.array.models_r_samsung, true);
                                break;
                            default:
                                Log.e(TAG,
                                        "The OnItemSelectedListener for the brand spinner got " +
                                                "a wrong parameter: position = "
                                                + position);
                        }
                        break;
                    default:
                        Log.e(TAG,
                                "The OnItemSelectedListener for the brand spinner got " +
                                        "a wrong parameter from the device type spinner: " +
                                        "deviceTypeSpinner.getSelectedItemPosition() = "
                                        + deviceTypeSpinner.getSelectedItemPosition());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * Reset the adapter in a spinner using external resources,
     * and set the enable status of the spinner.
     *
     * Reference:
     * http://developer.android.com/guide/topics/ui/controls/spinner.html#Populate
     *
     * @param context the application's environment
     * @param targetSpinner the spinner want to set up
     * @param textArrayResId the identifier of the array to use as the data source
     * @param ifEnable true if enable the spinner, of false to disable
     */
    private void setupSpinner(Context context, Spinner targetSpinner,
                              int textArrayResId, boolean ifEnable) {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> brandAdapter = ArrayAdapter.createFromResource(context,
                textArrayResId, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        targetSpinner.setAdapter(brandAdapter);

        targetSpinner.setEnabled(ifEnable);
    }
}
