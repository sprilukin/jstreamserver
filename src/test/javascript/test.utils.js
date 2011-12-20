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

var TestUtils = {};

(function (context) {
    var TEMPLATES_PREFIX = "/test/templates/";
    var TEMPLATES_SUFFIX = ".html";
    var PATH_TO_CSS = "/test/css/jstreamserver.css";

    var loadedTemplates = 0;
    var templatesSize = 0;

    var stylesheetLoaded = false;

    function loadTemplate(container, template, callback) {
        $.ajax(TEMPLATES_PREFIX + template + TEMPLATES_SUFFIX, {
            success:function (template) {
                $(container).append(template);
                callback && callback();
            },
            dataType:"html"
        })
    }

    context.templatesLoaded = function () {
        return loadedTemplates == templatesSize;
    };

    context.stylesheetLoaded = function () {
        return stylesheetLoaded;
    };

    context.loadTemplates = function (container) {
        var templates = [].splice.call(arguments, 1);
        if (templates != null && templates.length > 0) {
            loadedTemplates = 0;
            templatesSize = templates.length;

            var callback = function () {
                loadedTemplates++;
            };

            $.each(templates, function () {
                loadTemplate(container, this, callback);
            })
        }
    };

    context.loadCss = function() {
        $.ajax(PATH_TO_CSS, {
            success:function (stylesheet) {
                stylesheet = stylesheet.replace(/\/img\//g, "/test/img/");
                $("head").append(_.template("<style type='text/css'><%=css%></style>")({css:stylesheet}));
                stylesheetLoaded = true;
            },
            dataType:"text"
        });
    }

})(TestUtils);

