/*
 * Copyright (c) 2011 Sergey Prilukin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

//Define jstreamserver context
JStreamServer = {};

//Define model for directory list
JStreamServer.Directory = Backbone.Model.extend({});
JStreamServer.DirectoryList = Backbone.Collection.extend({
    model: JStreamServer.Directory
});

//Define model for breadcrumbs
JStreamServer.BreadCrumb = Backbone.Model.extend({});
JStreamServer.BreadCrumbs = Backbone.Collection.extend({
    model: JStreamServer.BreadCrumb
});

//Define view for directory list
JStreamServer.DirectoryView = Backbone.View.extend({

    initialize: function(json) {
        this.el = $("#directoryList");

        this.template = _.template($("#dirListTmpl").html());
        this.videoTemplate = _.template($("#videotagTmpl").html());

        this.model = new JStreamServer.DirectoryList(json);
        this.render();
        this.attachListeners();
    },

    attachListeners: function() {
        $.each(this.eventListeners, $.proxy(function(selector, handler) {
            this.el.find(selector).bind("click", $.proxy(handler, this));
        }, this));
    },

    render: function() {
        $(this.el).html(this.template({files: this.model.toJSON()}));
        $(".ajax-loader, .mediaInfo").hide(); //hide all ajax loaders and media infos
    },

    renderLiveStream: function(element, data) {
        $(element).append(this.videoTemplate(data));
    },

    removeLiveStream: function() {
        var liveStreeamVideos = this.el.find("video");
        $(liveStreeamVideos).each(function() {
            this.pause();
        });
        liveStreeamVideos.remove();
        this.el.find(".slider-panel").unbindTouchToMouse();
        this.el.find(".video-container").remove();
    },

    getPlayListSuccess: function (li, data) {
        //Hide ajax loader
        li.find(".play-links-holder").show();
        li.find(".ajax-loader").hide();

        var file = this.model.get(li.get(0).id);

        //Render html5 video tag
        this.renderLiveStream("#" + file.id, data);
        li.find(".subtitles").showSubtitles();
        li.find(".slider-panel").bindTouchToMouse().slider();
    },

    eventListeners: {
        ".video .play":function (event) {
            var li = $(event.target).parents("li");

            if (li.find("div.file").length > 0) {

                var file = this.model.get(li.get(0).id);

                if (file.get('mimeType').indexOf('video') == 0) {

                    li.find(".play-links-holder").hide();
                    li.find(".ajax-loader").show();

                    //Pause and remove all video elements
                    this.removeLiveStream();

                    //request .m3u8 playlist for specified video
                    $.getJSON($(event.target).get(0).href, null, $.proxy(this.getPlayListSuccess, this, li));

                    //Prevent default click behaviour and return false so click on href will not trigger default behaviour
                    event.preventDefault();
                    return false;
                }
            }

            return true;
        },

        ".info": function(event) {
            $(event.target).parents("li").find(".mediaInfo").slideToggle("fast", function() {});
        }
    }
});

//Define view for breadcrumbs
JStreamServer.BreadCrumbView = Backbone.View.extend({
    initialize: function(json) {
        this.el = $("#breadcrumb");
        this.template = _.template($("#breadcumbTmpl").html());

        this.model = new JStreamServer.BreadCrumbs(json);
        this.render();
    },

    render: function() {
        $(this.el).html(this.template({breadCrumbs: this.model.toJSON()}));
    }
});

