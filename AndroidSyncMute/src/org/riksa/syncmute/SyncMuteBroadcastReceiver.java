/*
 * SyncMute: Application for synchronized muting/unmuting of devices.
 * Copyright (c) 2012 Riku Salkia <riksa@iki.fi>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.riksa.syncmute;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: riksa
 * Date: 26.6.2012
 * Time: 18:42
 * To change this template use File | Settings | File Templates.
 */
public class SyncMuteBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "SyncMute-SyncMuteBroadcastReceiver";

    /**
     * Command sent when user requests a unmuted state
     */
    public static final String MUTE_OFF_COMMAND = "org.riksa.syncmute.MUTE_OFF_COMMAND";

    /**
     * Command sent when user requests muted state
     */
    public static final String MUTE_ON_COMMAND = "org.riksa.syncmute.MUTE_ON_COMMAND";

    /**
     * Message sent when state switches to unmuted
     */
    public static final String MUTE_OFF_STATE = "org.riksa.syncmute.MUTE_OFF_STATE";

    /**
     * Message sent when state switches to muted
     */
    public static final String MUTE_ON_STATE = "org.riksa.syncmute.MUTE_ON_STATE";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive " + intent);
        if (MUTE_ON_COMMAND.equals(intent.getAction())) {
            Intent i = new Intent(MUTE_ON_STATE);
            context.sendBroadcast(i);

            ParseTools p = ParseTools.getInstance(context.getApplicationContext());
            String channel = p.getChannel();
            p.notifyClientsMuted(true, channel);
//            p.setMuteState(channel, true);     // TODO: too slow, implement async
        } else if (MUTE_OFF_COMMAND.equals(intent.getAction())) {
            Intent i = new Intent(MUTE_OFF_STATE);
            context.sendBroadcast(i);

            ParseTools p = ParseTools.getInstance(context.getApplicationContext());
            String channel = p.getChannel();
            p.notifyClientsMuted(false, channel);
//            p.setMuteState(channel, false);
        } else if (MUTE_ON_STATE.equals(intent.getAction())) {
            mute(context, true);
        } else if (MUTE_OFF_STATE.equals(intent.getAction())) {
            mute(context, false);
        }

    }

    /**
     * Mute/unmute this device
     *
     * @param context
     * @param mute    true=mute, false=unmute
     */
    private static void mute(Context context, boolean mute) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(mute ? AudioManager.RINGER_MODE_SILENT : AudioManager.RINGER_MODE_NORMAL);
    }

}
