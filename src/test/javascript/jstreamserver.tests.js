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

    // DATA FIXTURES
    var data =
        {
            "id":"fileList0",
            "name":"Net (1995) AVC.mkv",
            "url":"/livestream?file=%2Fd%2Fmovies%2FNet+%281995%29+AVC.mkv",
            "path":"/?path=%2Fd%2Fmovies%2FNet+%281995%29+AVC.mkv",
            "mimeType":"video/x-matroska",
            "extension":"mkv",
            "video": true,
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

    var getPlayListCallback = {
        subtitle:"1\r\n00:00:00,146 --> 00:00:01,808\nРанее в 90210:\n\n2\n00:00:01,875 --> 00:00:03,709\nЯ нашла чудесный реабилитационный центр.",
        sources:[
            {"type":"application/x-mpegURL", "url":"/test/templates/stream.m3u8"},
            {"type":"application/octet-stream", "url":"/test/templates/stream.m3u8"}
        ],
        starttime: "00:00:00"
    };

    beforeEach(function() {
        runs(function () {
            TestUtils.loadTemplates($("body"), "directory", "dirlisttmpl", "videotagtmpl", "breadcrumbtmpl");
            TestUtils.loadCss();
        });

        waitsFor(TestUtils.templatesLoaded, "Timeout reached while loading templates", 500);
        waitsFor(TestUtils.stylesheetLoaded, "Timeout reached while loading css", 500);
    });

    afterEach(function() {
        $("body").html("");
    });

    it('should render directoryList after page load', function () {
        runs(function () {
            expect($("#dirListTmpl")).toBeDefined();
            expect($("#videotagTmpl")).toBeDefined();

            new JStreamServer.DirectoryView([
                {"id":"fileList0", "name":"c", "url":"/?path=%2Fc", "mimeType":null, "extension":null, "directory":true, "mediaInfo":null},
                {"id":"fileList1", "name":"d", "url":"/?path=%2Fd", "mimeType":null, "extension":null, "directory":true, "mediaInfo":null},
                data
            ]
            );

            expect($("#directoryList").find("li").length).toEqual(3);
            var mediaListItem = $($("#directoryList").find("li").get(2));
            expect(mediaListItem.find("div.directory").get(0)).toBeUndefined();
            expect(mediaListItem.find("div.file")).toBeDefined();
            expect(mediaListItem.find(".play-links-holder a").length).toEqual(2);
        });
    });

    it('should send request to getPlayList and render video tag when clicked on link for media file', function () {
        runs(function () {
            new JStreamServer.DirectoryView([data]);

            spyOn($, "getJSON").andCallFake(function (url, getData, callback) {
                expect(url.indexOf(data.url) >= 0).toBeTruthy();
                expect(callback).toBeDefined();

                //ajax loader should be shown
                expect($("#" + data.id).find(".ajax-loader").is(":visible")).toBeTruthy();
                callback(getPlayListCallback);
            });

            var anchor = $("#" + data.id).find("a")[0];
            $(anchor).simulate('click'); //somewhy doesn't always fires click event, maybe because of subsequent eqceptions
            //$(anchor).click();

            expect($("#" + data.id).find(".ajax-loader").is(":visible")).toBeFalsy(); //ajax loader should be hidden
            var video = $("#" + data.id).find("video")[0];
            expect(video).toBeDefined();

            var sources = $(video).find("source");
            expect(sources.length).toEqual(2);
            $.each([0, 1], function (index) {
                expect(sources[index].src.indexOf(getPlayListCallback.sources[index].url) >= 0).toBeTruthy();
                expect(sources[index].type).toEqual(getPlayListCallback.sources[index].type);
            });

            var subtitles = $("#" + data.id).find("div.subtitles")[0];
            expect(subtitles).toBeDefined();
            expect($(subtitles).attr("data-video")).toEqual(video.id);
            expect($(subtitles).text().replace(/[\r\n\s]+/g, "")).toEqual(getPlayListCallback.subtitle.replace(/[\r\n\s]+/g, ""));
        });
    });

    it('should hide media info element right after rendering and toggle its visibility after a click on info image', function () {
        runs(function () {
            new JStreamServer.DirectoryView([data]);

            var mediaInfo = $("#" + data.id).find(".mediaInfo");

            spyOn($.fn, "slideToggle").andCallFake(function (speed, easing, callback) {
                mediaInfo.is(":visible") ? mediaInfo.hide() : mediaInfo.show();
            });

            expect(mediaInfo.is(":visible")).toBeFalsy();

            $($("#" + data.id).find(".icon.info")).simulate('click');
            expect(mediaInfo.is(":visible")).toBeTruthy();

            $($("#" + data.id).find(".icon.info")).simulate('click');
            expect(mediaInfo.is(":visible")).toBeFalsy();
        });
    });

    it('should render breadcrumbs after page load', function () {
        runs(function () {
            new JStreamServer.BreadCrumbView([
                {"name":"", "url":"/"},
                {"name":"d", "url":"/d"},
                {"name":"download", "url":"/d/download"}
            ]);

            expect($("#breadcrumb").find("span.breadcrumb-item").length).toEqual(3);
        })
    });
});
