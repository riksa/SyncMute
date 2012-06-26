/*
 * Copyright 2012 Riku Salkia <riksa@iki.fi>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.riksa.syncmute;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import com.parse.*;

import java.util.List;

public class ConfigActivity extends Activity {

    /**
     * Preferences instance
     */
    private SharedPreferences sharedPreferences;
    private static final String TAG = "SyncMute_ConfigActivity";
    private static final long POLL_DELAY = 1000;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Parse.initialize(this, getString(R.string.parse_application_id), getString(R.string.parse_client_key));

//        ParseObject testObject = new ParseObject("SyncMuteTest");
//        testObject.put("mute", "on");
//        testObject.saveInBackground();
    }

    private Runnable pollRunnable;
    private Handler handler = new Handler();

    /**
     * Polling for debug purposes, TODO: push version
     */
    private void debugStartPoller() {
        final ConfigActivity configActivity = this;

        if (pollRunnable == null) {
            pollRunnable = new Runnable() {
                @Override
                public void run() {
                    ParseQuery query = new ParseQuery("ChannelState").whereEqualTo("channel", getChannel());
                    query.setLimit(1);
                    query.findInBackground(new FindCallback() {
                        @Override
                        public void done(List<ParseObject> parseObjects, ParseException e) {
                            //To change body of implemented methods use File | Settings | File Templates.
                            if (e == null) {
                                for (ParseObject parseObject : parseObjects) {
                                    Object value = parseObject.get("state");
                                    if (value instanceof Boolean) {
                                        Boolean state = (Boolean)value;
                                        if( state == null ) state = false;
                                        setChannelState(state.booleanValue());
                                    }
                                }
                            } else {
                                Log.d(TAG, "Error: " + e.getMessage());
                            }
                            handler.postDelayed(pollRunnable, POLL_DELAY);
                        }
                    });
                }
            };
            handler.post(pollRunnable);
        }
    }

    private void setChannelState(boolean stateOn) {
        View view = findViewById(R.id.channel_state);
        if( view instanceof ImageView ) {
            ImageView imageView = (ImageView) view;

            if( stateOn ) {
                imageView.setImageLevel(1);
            } else {
                imageView.setImageLevel(0);
            }

        }
    }

    private void debugStopPoller() {
        if (pollRunnable != null) {
            handler.removeCallbacks( pollRunnable );
            pollRunnable = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
        debugStartPoller();
    }

    @Override
    protected void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
        debugStopPoller();
    }

    private String getChannel() {
        return "JORMA";  //To change body of created methods use File | Settings | File Templates.
    }

    /**
     * Creates options menu
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Handle selection of a menu item
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_preferences:
                startActivity(new Intent(this, SyncMutePreferencesActivity.class));
                break;
            default:
                break;
        }
        return true;    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Handle change in configuration
     *
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
