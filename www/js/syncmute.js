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

var ChannelState = Parse.Object.extend("ChannelState");
//{
//    // Class methods
//    initialize:function (channel, state) {
//        var channelState = new ChannelState();
//        channelState.set("channel", channel);
//        channelState.set("state", !!state);
//        return channelState;
//    }
//}, {
//    // Instance methods
//    getState:function () {
//        return !!this.get("state");
//    }
//});
//
var Syncmute = {
    appId:"",
    key:"",
    state_on:true,
    image_off:"images/off.png",
    image_on:"images/on.png",

    init:function (appId, key) {
        this.appId = appId;
        this.key = key;
        this.state_on = true; //TODO: From parse
        console.log("APPLICATION_ID: " + APPLICATION_ID);
        console.log("JAVASCRIPT_KEY: " + JAVASCRIPT_KEY);
        Parse.initialize(APPLICATION_ID, JAVASCRIPT_KEY);
        this.getChannelState(this.getChannelName(),
            function (channelState) {
                console.log(channelState.get("state"));
            },
            function (channelState, error) {
            }
        );
    },
    getChannelState:function (channel, successCallback, errorCallback) {
        var self = this;
        var query = new Parse.Query(ChannelState);
        query.equalTo("channel", channel);
        query.find({
            success:function (results) {
                if (results.length == 0) {
                    var channelState = new ChannelState();
                    channelState.save({
                            channel:channel,
                            state:true
                        },
                        {
                            success:successCallback,
                            error:errorCallback
                        }
                    );
                } else {
                    successCallback(results[0]);
                }
            },
            error:function (error) {
                alert("Error: " + error.code + " " + error.message);
            }
        });
    },
    setChannelState:function (channelState, newState, successCallback, errorCallback) {
        channelState.set("state", newState);
        channelState.save(null, {
                success:successCallback,
                error:errorCallback
            }
        );
    },
    bind:function (id) {
        this.setState(id);
        $(id).click(Syncmute.toggle);
    },
    setState:function (id) {
        // TODO: set state
        $(id).attr("src", function () {
            var src;
            if (Syncmute.state_on) {
                src = $(this).attr('data-on');
                if (typeof src === 'undefined') src = Syncmute.image_on;
            } else {
                src = $(this).attr('data-off');
                if (typeof src === 'undefined') src = Syncmute.image_off;
            }
            return src;
        });
    },
    toggle:function () {
        var channel = Syncmute.getChannelName();
        Syncmute.getChannelState(channel,
            function (channelState) {
                var state = !!channelState.get("state");
                console.log("state: " + state);
                Syncmute.setChannelState(channelState, !state, function (newChannelState) {
                    var newState = !!newChannelState.get("state");
                    console.log("newState: " + newState);
                    Syncmute.setImagesTo(newState);
                });
            },
            function (channelState, error) {
                console.log(error);
            }
        );
    },
    setImagesTo:function (state) {
        $("img.syncmute").attr("src", function () {
            var src;
            if (!!state) {
                src = $(this).attr('data-on');
                if (typeof src === 'undefined') src = Syncmute.image_on;
            } else {
                src = $(this).attr('data-off');
                if (typeof src === 'undefined') src = Syncmute.image_off;
            }
            return src;
        });
    },
    getChannelName:function () {
        return "JORMA"; // TODO: Salted hash from username+channelname
    }

}

