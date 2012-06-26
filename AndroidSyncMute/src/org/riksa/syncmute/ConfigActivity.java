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
import android.content.*;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

/**
 * Activity for accessing configuration
 */
public class ConfigActivity extends Activity {

    /**
     * {@link SharedPreferences} instance
     */
    private SharedPreferences sharedPreferences;
    private static final String TAG = "SyncMute-ConfigActivity";

    /**
     *  State of muting. Toggled via intents from the cloud.
     */
    private boolean muted = false;

    /**
     * {@link BroadcastReceiver} receiving push notifications
     */
    private BroadcastReceiver receiver = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // TODO: show some tutorial on first launch, populate (or prompt) username/channel with some sane value

        ParseTools parseTools = ParseTools.getInstance(this);
        parseTools.subscribe(parseTools.getChannel());

    }

    /**
     * Initializes mute button icon to correct state
     */
    private void toggleMuteIcon() {
        View view = findViewById(R.id.channel_state);
        if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;

            if (muted) {
                imageView.setImageLevel(0);
            } else {
                imageView.setImageLevel(1);
            }

        }
    }

    /**
     * Register receiver, set button to correct state on resume
     */
    @Override
    protected void onResume() {
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
        ParseTools parseTools = ParseTools.getInstance(this);
        String channel = parseTools.getChannel();
        muted = parseTools.getMuteState(channel);
        toggleMuteIcon();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SyncMuteBroadcastReceiver.MUTE_ON_STATE);
        intentFilter.addAction(SyncMuteBroadcastReceiver.MUTE_OFF_STATE);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (SyncMuteBroadcastReceiver.MUTE_ON_STATE.equals(intent.getAction())) {
                    muted = true;
                } else if (SyncMuteBroadcastReceiver.MUTE_OFF_STATE.equals(intent.getAction())) {
                    muted = false;
                }
                toggleMuteIcon();
            }
        };

        registerReceiver(receiver, intentFilter);
    }

    /**
     * Unregister receiver when paused
     */
    @Override
    protected void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
        if( receiver != null ) {
            unregisterReceiver(receiver);
            receiver = null;
        }
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

    /**
     * Handle toggling of muting on/off
     * @param view
     */
    public void onToggleButtonClicked(View view) {
        if (view instanceof ImageButton) {
            Intent intent = new Intent();
            // if device is currently muted, send unmute command. otherwise, send mute command
            intent.setAction(muted ? SyncMuteBroadcastReceiver.MUTE_OFF_COMMAND : SyncMuteBroadcastReceiver.MUTE_ON_COMMAND);
            sendBroadcast(intent);

            Log.d(TAG, "onToggleButtonClicked ");
        }

    }

}
