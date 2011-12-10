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

describe('jstreamserver', function () {

    beforeEach(function() {
        $("body").append("<div id=\"testContext\"></div>");

        $.ajax("/test/templates/directory.html", {
            success: function(template) {
                $("#testContext").append(template);
            },
            dataType: "html",
            async: false
        });

        $.ajax("/test/templates/jq-templates.html", {
            success: function(template) {
                $("#testContext").append(template);
            },
            dataType: "html",
            async: false
        });
    });

    afterEach(function() {
        $("#testContext").remove();
    });

    it('should render directoryList and breadcrumbs after page load', function () {
        JStreamServer.controller.init(
            {
                "files":[
                    {"id":"fileList0","name":"c","url":"%2Fc","mimeType":null,"extension":null,"liveStreamSupported":null,"directory":true},
                    {"id":"fileList1","name":"d","url":"%2Fd","mimeType":null,"extension":null,"liveStreamSupported":null,"directory":true}],
                "breadcrumbs":[{"name":"","url":"/"},{"name":"d","url":"/d"},{"name":"download","url":"/d/download"}]
            }
        );

        expect($("#directoryList").find("li").length).toEqual(2);
        expect($("#breadcrumb").find("span.breadcrumb-item").length).toEqual(3);
    });
});
