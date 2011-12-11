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

    initialize: function(args) {
        this.el = $("#directoryList");

        this.template = _.template($("#dirListTmpl").html());
        this.liveStreamTemplate = _.template($("#livestreamTmpl").html());

        this.model = new JStreamServer.DirectoryList(args.json);
        this.attachListeners();
        this.render();
    },

    attachListeners: function() {
        this.el.bind("click", this.eventListeners['click'].bind(this));
    },

    render: function() {
        $(this.el).html(this.template({files: this.model.toJSON()}));
    },

    renderLiveStream: function(element, data) {
        this.el.find("video." + data.cssClass).remove();
        $(element).append(this.liveStreamTemplate(data));
    },

    findMeOrUp: function(elem, selector) {
        return $(elem).is(selector) ? elem : $(elem).parents(selector).get(0);
    },

    eventListeners: {
        "click":function (event) {
            if (!$(event.target).is("a") && !$(event.target).is("span")) {
                return;
            }

            var li = $(this.findMeOrUp(event.target, "li"));

            if (li.find("div.file").length > 0) {
                var file = this.model.get(li.get(0).id);

                if (file.get('liveStreamSupported')) {
                    var anchor = li.find("a").get(0);

                    $(anchor).hide();
                    li.find(".ajax-loader").removeClass("hidden");

                    $.ajax(anchor.href, {
                        dataType:"json",
                        success:function (data) {
                            this.renderLiveStream("#" + file.id, data);

                            $(anchor).show();
                            li.find(".ajax-loader").addClass("hidden");
                        }.bind(this)
                    });

                    return false;
                }
            }

            return true;
        }
    }
});

//Define view for breadcrumbs
JStreamServer.BreadCrumbView = Backbone.View.extend({
    initialize: function(args) {
        this.el = $("#breadcrumb");
        this.template = _.template($("#breadcumbTmpl").html());

        this.model = new JStreamServer.BreadCrumbs(args.json);
        this.render();
    },

    render: function() {
        $(this.el).html(this.template({breadCrumbs: this.model.toJSON()}));
    }
});

