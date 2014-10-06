/**
 * An activity to open web pages.
 * Web page URL is from the extended data of the intent.
 *
 * @author Zac (Qi ZHANG)
 * Created on 09/24/2014.
 */
package com.dfwexcellerator.iplugin.activities;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.dfwexcellerator.iplugin.R;
import com.dfwexcellerator.iplugin.helpers.Constants;

public class WebPageActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_page);

        ((WebView) findViewById(R.id.webView)).loadUrl(getIntent()
                .getStringExtra(Constants.EXTRA_WEB_PAGE_URL));
    }

}
