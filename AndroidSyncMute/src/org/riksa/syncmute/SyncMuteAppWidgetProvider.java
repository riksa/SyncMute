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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Widget for toggling muting on/off easily
 */
public class SyncMuteAppWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "SyncMute-SyncMuteAppWidgetProvider";
    private boolean muted = false;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        ParseTools parseTools = ParseTools.getInstance(context);
        parseTools.subscribe(parseTools.getChannel());
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);    //To change body of overridden methods use File | Settings | File Templates.
        ParseTools parseTools = ParseTools.getInstance(context);
        parseTools.unsubscribe(parseTools.getChannel());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive " + intent);
        if (SyncMuteBroadcastReceiver.MUTE_ON_STATE.equals(intent.getAction())) {
            muted = true;
        } else if (SyncMuteBroadcastReceiver.MUTE_OFF_STATE.equals(intent.getAction())) {
            muted = false;
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName me = new ComponentName(context.getPackageName(), getClass().getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(me);
        onUpdate(context, appWidgetManager, appWidgetIds);

        super.onReceive(context, intent);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        final int N = appWidgetIds.length;
        Log.d(TAG, "muted=" + muted);


        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            Log.d(TAG, "onUpdate " + appWidgetId);

            // Create an Intent to switch muting on / off
            Intent intent = new Intent();
            intent.setAction(muted ? SyncMuteBroadcastReceiver.MUTE_OFF_COMMAND : SyncMuteBroadcastReceiver.MUTE_ON_COMMAND);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.syncmute_appwidget);
            views.setImageViewResource(R.id.widget_button, muted ? R.drawable.channel_state_off : R.drawable.channel_state_on);
            views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

}
