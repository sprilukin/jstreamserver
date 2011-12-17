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
        this.attachListeners();
        this.render();
    },

    attachListeners: function() {
        this.el.bind("click", _.bind(this.eventListeners['click'], this));
    },

    render: function() {
        $(this.el).html(this.template({files: this.model.toJSON()}));
    },

    renderLiveStream: function(element, data) {
        $(element).append(this.videoTemplate(data));
    },

    removeLiveStream: function() {
        var liveStreeamVideos = this.el.find("video");
        $(liveStreeamVideos).each(function(video) {
            video.pause()
        });
        liveStreeamVideos.remove();
        this.el.find("div.subtitles").remove();
    },

    findMeOrUp: function(elem, selector) {
        return $(elem).is(selector) ? elem : $(elem).parents(selector).get(0);
    },

    getPlayListSuccess: function (li, data) {
        var file = this.model.get(li.get(0).id);
        var anchor = li.find("a").get(0);

        //Render html5 video tag
        this.renderLiveStream("#" + file.id, data);
        $("div.subtitles").showSubtitles();

        //Hide ajax loader
        $(anchor).show();
        li.find(".ajax-loader").addClass("hidden");
    },

    eventListeners: {
        "click":function (event) {
            if (!$(event.target).is("a")) {
                return;
            }

            var li = $(this.findMeOrUp(event.target, "li"));

            if (li.find("div.file").length > 0) {

                var file = this.model.get(li.get(0).id);

                if (file.get('mimeType').indexOf('video') == 0) {

                    $(li.find("a").get(0)).hide();
                    li.find(".ajax-loader").removeClass("hidden");

                    //Pause and remove all video elements
                    this.removeLiveStream();

                    //request .m3u8 playlist for specified video
                    $.getJSON($(event.target)[0].href, null, _.bind(this.getPlayListSuccess, this, li));

                    //Prevent default click behaviour and return false so click on href will not trigger default behaviour
                    event.preventDefault();
                    return false;
                }
            }

            return true;
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

