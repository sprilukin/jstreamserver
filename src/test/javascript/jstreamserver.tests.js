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

    it('should render directoryList after page load', function () {
        new JStreamServer.DirectoryView({json: [
            {"id":"fileList0","name":"c","url":"%2Fc","mimeType":null,"extension":null,"liveStreamSupported":null,"directory":true},
            {"id":"fileList1","name":"d","url":"%2Fd","mimeType":null,"extension":null,"liveStreamSupported":null,"directory":true}]
        });

        expect($("#directoryList").find("li").length).toEqual(2);
    });

    it('should send request to getPlayList and render video tag when clicked on link for media file', function () {

        var data =
            {
                "id":"fileList0",
                "name":"Net (1995) AVC.mkv",
                "url":"%2Fd%2Fmovies%2FNet+%281995%29+AVC.mkv",
                "mimeType":"video/x-matroska",
                "extension":"mkv",
                "liveStreamSupported":true,
                "mediaInfo":{
                    "bitrate":"2769 kb/s",
                    "duration":"01:49:43.00",
                    "audioStreams":[
                        {"id":"1", "language":"rus", "encoder":"aac", "frequency":"48000", "channels":"5.1", "bitrate":null, "defaultStream":true},
                        {"id":"2", "language":"rus", "encoder":"aac", "frequency":"48000", "channels":"stereo", "bitrate":null, "defaultStream":false},
                        {"id":"3", "language":"rus", "encoder":"aac", "frequency":"48000", "channels":"5.1", "bitrate":null, "defaultStream":false},
                        {"id":"4", "language":"eng", "encoder":"aac", "frequency":"48000", "channels":"5.1", "bitrate":null, "defaultStream":false}
                    ],
                    "videoStreams":[
                        {"id":"0", "language":"eng", "encoder":"h264", "resolution":"720x560", "fps":null, "defaultStream":true}
                    ],
                    "metadata":{}
                },
                "directory":false
            };

        var getPlayListCallback = {url: "/test/templates/stream.m3u8"};

        new JStreamServer.DirectoryView({json: [data]});

        spyOn($, "getJSON").andCallFake(function(url, getData, callback) {
            expect(url.substr(url.indexOf("?") + "file=".length + 1)).toEqual(data.url);
            expect(callback).toBeDefined();

            callback(getPlayListCallback);
        });

        var anchor = $("#" + data.id).find("a")[0];
        $(anchor).simulate('click');

        var video = $("#" + data.id).find("video")[0];
        expect(video).toBeDefined();
        expect(video.src.substr(video.src.indexOf("/test"))).toEqual(getPlayListCallback.url);
    });

    it('should render breadcrumbs after page load', function () {
        new JStreamServer.BreadCrumbView({json: [{"name":"","url":"/"},{"name":"d","url":"/d"},{"name":"download","url":"/d/download"}]});

        expect($("#breadcrumb").find("span.breadcrumb-item").length).toEqual(3);
    });
});
