/**
 * An adapter to show the listView in "My plugins" fragment.
 *
 * Reference:
 * http://www.vogella.com/tutorials/AndroidListView/article.html
 * 7. Tutorial: Implementing your own adapter
 *
 * @author Zac (Qi ZHANG)
 * Created on 09/30/2014.
 */

package com.dfwexcellerator.iplugin.helpers;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dfwexcellerator.iplugin.R;

public class PluginAdapter extends ArrayAdapter<PluginItem> {
    private static final String TAG = "PluginAdapter";

    private final Context context;
    private final PluginItem[]  values;
    private final String[] deviceTypes;

    public PluginAdapter(Context context, PluginItem[] values) {
        super(context, R.layout.list_item_plugin, values);
        this.context = context;
        this.values = values;
        deviceTypes = context.getResources().getStringArray(R.array.device_type);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_plugin, parent, false);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView_device_type);
        TextView textView = (TextView) rowView
                .findViewById(R.id.editText_nick_name_in_plugins_list);
        ProgressBar progressBar = (ProgressBar) rowView.findViewById(R.id.progressBar_plugin);
        imageView.setVisibility(View.VISIBLE);

        textView.setText(values[position].getNickName());
        progressBar.setProgress(values[position].getPercent());
        String s = values[position].getDeviceType();
        for (int i = 0; i < deviceTypes.length; i++)
            if (s.equals(deviceTypes[i]))
                switch(i) {
                    case 1:
                        imageView.setImageResource(R.drawable.ic_device_type_ac);
                        break;
                    case 2:
                        imageView.setImageResource(R.drawable.ic_device_type_microwave);
                        break;
                    case 3:
                        imageView.setImageResource(R.drawable.ic_device_type_refrigerator);
                        break;
                    default:
                        Log.e(TAG, "Got a wrong device type: " + s);
                        imageView.setVisibility(View.GONE);
                }
        return rowView;
    }
}
