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
define([],
 function() {

     var findMeOrUp = function(elem, selector) {
         return $(elem).is(selector) ? elem : $(elem).parents(selector)[0];
     };

     var attachListeners = function() {
         $('ul.folderContent').bind("click", listeners.dirlistClick);
         $('div.breadcrumb').bind("click", listeners.breadcrumbClick);
     };

     var renderData = function(data) {
         $('#directoryList').html($('#dirListTmpl').tmpl(data.files));
         $('#breadcrumb').html($('#breadcumbTmpl').tmpl(data.breadcrumbs));
     };

     var listeners = {
         dirlistClick: function(event) {
             event.stopPropagation();

             var li = $(findMeOrUp(event.target, "li"));
             if (li.find("div.directory").length > 0) {
                 var url = li.find("a")[0].href;

                 $.ajax(url, {
                     dataType: "json",
                     success: function(data) {
                         renderData(data);
                     }
                 });

                 return false;
             } else {
                 return true;
             }
         },

         breadcrumbClick: function(event) {
             event.stopPropagation();

             var span = $(findMeOrUp(event.target, "span"));
             var url = span.find("a")[0].href;

             $.ajax(url, {
                 dataType: "json",
                 success: function(data) {
                     renderData(data);
                 }
             });

             return false;
         }
     };

  return {
      init: function(data) {
          attachListeners();
          renderData(data);
      }
  };
});
