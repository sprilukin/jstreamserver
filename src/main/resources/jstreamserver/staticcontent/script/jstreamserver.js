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

JStreamServer.dirView = (function() {
    var renderData = function(data) {
        $('#directoryList').html($('#dirListTmpl').tmpl(data.files));
        $('#breadcrumb').html($('#breadcumbTmpl').tmpl(data.breadcrumbs));
    };

    return {
        render: function(data) {
            renderData(data);
        }
    }
}());

JStreamServer.liveStreamView = (function() {
    var renderLivestream = function(data, id) {
        $('ul.folderContent').find("video." + data.cssClass).remove();
        $('#' + id).append($('#livestreamTmpl').tmpl(data));
    };

    return {
        render: function(data) {
            renderLivestream(data.data, data.id);
        }
    }
}());

JStreamServer.controller = (function() {
     var internalData = null;

     var findMeOrUp = function(elem, selector) {
         return $(elem).is(selector) ? elem : $(elem).parents(selector).get(0);
     };

     var clickListeners = {
         "ul.folderContent": function(event) {
             if (!$(event.target).is("a") && !$(event.target).is("span")) {
                 return;
             }

             var li = $(findMeOrUp(event.target, "li"));
             if (li.find("div.file").length > 0) {
                 var index = parseInt(li.get(0).id.substr(8));
                 var dataElement = internalData.files[index];
                 if (dataElement.liveStreamSupported) {
                     var anchor = li.find("a").get(0);
                     var url = anchor.href;

                     $(anchor).hide();
                     li.find(".ajax-loader").removeClass("hidden");

                     $.ajax(url, {
                         dataType: "json",
                         success: function(data) {
                             JStreamServer.liveStreamView.render({data:data, id: li.get(0).id});
                             $(anchor).show();
                             li.find(".ajax-loader").addClass("hidden");
                         }
                     });

                     return false;
                 }
             }

             return true;
         },

         "div.breadcrumb": function(event) {
             return true;
         }
     };

    var attachListeners = function() {
        $('ul.folderContent').bind("click", clickListeners['ul.folderContent']);
        $('div.breadcrumb').bind("click", clickListeners['div.breadcrumb']);
    };

     return {
         init:function (data) {
             attachListeners();
             internalData = data;
             JStreamServer.dirView.render(data);
         }
     }
 }());
