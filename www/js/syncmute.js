/*
 * SyncMute: Application for synchronized muting/unmuting of devices.
 * Copyright (c) 2012 Riku Salkia <riksa@iki.fi>
 *
 * TODO: Choose license
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

