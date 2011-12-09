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
 (function(context, undefined) {
     var internalData = null;

     var findMeOrUp = function(elem, selector) {
         return $(elem).is(selector) ? elem : $(elem).parents(selector).get(0);
     };

     var attachListeners = function() {
         $('ul.folderContent').bind("click", listeners.dirlistClick);
         $('div.breadcrumb').bind("click", listeners.breadcrumbClick);
     };

     var renderData = function() {
         $('#directoryList').html($('#dirListTmpl').tmpl(internalData.files));
         $('#breadcrumb').html($('#breadcumbTmpl').tmpl(internalData.breadcrumbs));
     };

     var renderLivestream = function(data, id) {
         $('ul.folderContent').find("video." + data.cssClass).remove();
         $('#' + id).append($('#livestreamTmpl').tmpl(data));
     };

     var listeners = {
         dirlistClick: function(event) {
             if (!$(event.target).is("a") && !$(event.target).is("span")) {
                 return;
             }

             var li = $(findMeOrUp(event.target, "li"));
             if (li.find("div.directory").length > 0) {

                 //Use default behaviour for dir links for now
                 /*var url = li.find("a").get(0).href;

                 $.ajax(url, {
                     dataType: "json",
                     success: function(data) {
                         internalData = data;
                         renderData();
                     }
                 });

                 return false;*/

                 return true;
             } else {
                 var index = parseInt(li.get(0).id.substr(8));
                 var dataElement = internalData.files[index];
                 if (dataElement.liveStreamSupported) {
                     var anchor = li.find("a").get(0);
                     url = anchor.href;

                     $(anchor).hide();
                     li.find(".ajax-loader").removeClass("hidden");

                     $.ajax(url, {
                         dataType: "json",
                         success: function(data) {
                             renderLivestream(data, li.get(0).id);
                             $(anchor).show();
                             li.find(".ajax-loader").addClass("hidden");
                         }
                     });

                     return false;
                 }

                 return true;
             }
         },

         breadcrumbClick: function(event) {
             //Use default behaviour for breadcrumb links for now
             /*var span = $(findMeOrUp(event.target, "span"));
             var url = span.find("a").get(0).href;

             $.ajax(url, {
                 dataType: "json",
                 success: function(data) {
                     internalData = data;
                     renderData();
                 }
             });

             return false;*/

             return true;
         }
     };

     context.jstreamserver = {
         init:function (data) {
             attachListeners();
             internalData = data;
             renderData(data);
         }
     }
 }(window));
