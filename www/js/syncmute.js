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
    },
    bind: function( id ) {
        this.setState( id );
        $(id).click( Syncmute.toggle );
    },
    setState: function( id ) {
        // TODO: set state
        $(id).attr("src", function() {
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
        Syncmute.state_on = !Syncmute.state_on;
        $("img.syncmute").attr("src", function () {
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
    }

}

