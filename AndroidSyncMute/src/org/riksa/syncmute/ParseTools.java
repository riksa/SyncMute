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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import com.parse.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Parse.com related functionality
 */
public class ParseTools {
    private static final String TAG = "SyncMute-ParseTools";

    /**
     * Singleton instance
     */
    private static ParseTools instance = null;

    /**
     * Class name for objects defining state of a channel
     */
    public static final String CHANNEL_STATE = "ChannelState";

    /**
     * ChannelState objects variable for channel name
     */
    public static final String CHANNEL = "channel";

    /**
     * ChannelState objects variable for channel state
     */
    public static final String STATE = "state";

    /**
     * Lights, Camera,
     */
    public static final String ACTION = "action";

    /**
     * Application context
     */
    private Context context;

    /**
     * Digest algorithm for channel name hash
     */
    private static final String DIGEST_ALGORITHM = "SHA-1";

    /**
     * Salt for hash
     */
    private static final byte[] SALT = {0x6c, 0x6a, 0x59, 0x51, 0x74, 0x5b, 0x68, 0x56, 0x06, 0x71};

    /**
     * Private constructor for singleton
     * @param context
     */
    private ParseTools(Context context) {
        this.context = context;
        // Add your initialization code here
        String applicationId = context.getString(R.string.parse_application_id);
        String clientKey = context.getString(R.string.parse_client_key);
        Log.d(TAG, "applicationId=" + applicationId);
        Log.d(TAG, "clientKey=" + clientKey);
        Parse.initialize(context, applicationId, clientKey);

        ParseUser.enableAutomaticUser();

        ParseACL defaultACL = new ParseACL();
        // Optionally enable public read access by default.
        // defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
    }

    /**
     * Get singleton instance of ParseTools
     *
     * @param context
     * @return
     */
    public static ParseTools getInstance(Context context) {
        if (instance == null) {
            instance = new ParseTools(context.getApplicationContext());
        }

        return instance;
    }

    /**
     * Get channel name. Channel name is a salted SHA-256 hash of username+channename (from settings)
     *
     * @return Base64 encoded SHA-256 salted hash
     */
    protected String getChannel() {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String userName = preferences.getString("preferences_username", "DEFAULT");
            String channelName = preferences.getString("preferences_channel", "DEFAULT");
            Log.d(TAG, "hashing username=" + userName);
            Log.d(TAG, "hashing channelName=" + channelName);

            byte[] digest = doHash(DIGEST_ALGORITHM, SALT, userName.getBytes("UTF-8"), channelName.getBytes("UTF-8"));
            // Base64 encode without padding. Padded channel name is invalid (alphanumerics only)
            String base64Digest = Base64.encodeToString(digest, Base64.NO_WRAP | Base64.NO_PADDING);
            Log.d(TAG, "base64Digest=" + base64Digest);

            return base64Digest;
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return "DEFAULT";
    }

    /**
     * Digest vararg byte arrays with salt
     *
     * @param algo Digest Algorithm
     * @param salt Salt
     * @param vals vararg bytearrays to hash
     * @return hash
     * @throws NoSuchAlgorithmException
     */
    private static byte[] doHash(String algo, byte[] salt, byte[]... vals) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(algo);
        messageDigest.update(salt);
        for (byte[] val : vals) {
            messageDigest.update(val);
        }
        return messageDigest.digest();
    }

    /**
     * Subscribe to events of a channel and start receiving push notifications
     *
     * @param channel
     */
    public void subscribe(String channel) {
        Log.d(TAG, "subscribe to " + channel);
        PushService.subscribe(context, channel, ConfigActivity.class);
    }

    /**
     * Unsubscribe from push notifications of a channel
     *
     * @param channel
     */
    public void unsubscribe(String channel) {
        Log.d(TAG, "unsubscribe from " + channel);
        PushService.unsubscribe(context, channel);
    }

    /**
     * Notifies clients that muting/unmuting was requested by the user
     *
     * @param muted
     * @param channel
     */
    public void notifyClientsMuted(boolean muted, String channel) {
        Log.d(TAG, "notifyClientsMuted " + muted + ", channel=" + channel);

        try {
            ParsePush push = new ParsePush();
            push.setChannel(channel);
            push.setExpirationTimeInterval(86400);
            if (muted) {
                push.setData(new JSONObject().put(ACTION, SyncMuteBroadcastReceiver.MUTE_ON_STATE));
            } else {
                push.setData(new JSONObject().put(ACTION, SyncMuteBroadcastReceiver.MUTE_OFF_STATE));
            }
            push.setPushToAndroid(true);
//            push.setPushToIOS(true);

            push.sendInBackground();
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * Returns current state of muting according to cloud
     *
     * @param channel
     * @return
     */
    public boolean getMuteState(String channel) {
        ParseQuery query = new ParseQuery(CHANNEL_STATE).whereEqualTo(CHANNEL, channel);
        query.setLimit(1);
        try {
            List<ParseObject> parseObjects = query.find();
            if (parseObjects == null || parseObjects.size() == 0) {
                // No state yet for this channel, default to false (unmuted)
                return false;
            }
            ParseObject parseObject = parseObjects.get(0);
            Object value = parseObject.get(STATE);
            if (value instanceof Boolean) {
                return (Boolean) value;
            }

        } catch (ParseException e) {
            Log.d(TAG, "Error: " + e.getMessage());
        }

        // default to false (unmuted)
        return false;
    }

    /**
     * Store mutestate of a channel to cloud
     * @param channel
     * @param muted
     */
    public void setMuteState(String channel, boolean muted) {
        ParseQuery query = new ParseQuery(CHANNEL_STATE).whereEqualTo(CHANNEL, channel);
        ParseObject parseObject = null;
        query.setLimit(1);
        try {
            List<ParseObject> parseObjects = query.find();
            if (parseObjects != null && parseObjects.size() == 1) {
                parseObject = parseObjects.get(0);
            } else {
                parseObject = new ParseObject(CHANNEL_STATE);
                parseObject.put(CHANNEL, channel);
            }

            parseObject.put(STATE, muted);
            parseObject.save();

        } catch (ParseException e) {
            Log.d(TAG, "Error: " + e.getMessage());
        }

    }

}
