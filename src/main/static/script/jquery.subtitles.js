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

(function ($) {

    var loadSubtitlesText = function(metadata, callback) {
        var text = $.trim(metadata.text);

        if (text) {
            callback && callback(text);
        } else {
            $.ajax(metadata.url, {
                dataType: "text",
                success: callback
            })
        }
    };

    var showSubtitle = function(metadata) {
        if (!metadata) {
            return;
        }

        if ((typeof metadata.video.addtrack !== "function") || !metadata.video.find("track[kind='subtitle']").first()) {
            loadSubtitlesText(metadata, function(text) {
                metadata.subtitles = convertTextToSubtitlesModel(text);
                metadata.text = null; //free memory;
                metadata.currentSubtitleIndex = 0; //start showing subtitles from the beginning
                createSubtitlesMarkup(metadata);
                $.each(eventListeners, function(eventName, handler) {
                    metadata.video.bind(eventName, $.proxy(handler, metadata));
                })
            })
        }
    };

    var createSubtitlesMarkup = function (metadata) {
        var videowidth = metadata.video.width();

        var fontSize = metadata.fontSize;
        if (videowidth > 400) {
            fontSize = fontSize + Math.ceil((videowidth - 400) / 100);
        }

        var videoWrapper = $(metadata.video).parents(".subtitles-video-wrapper").get(0);
        var subtitlesPanel = $(videoWrapper).find(".subtitles-panel");
        subtitlesPanel.css({
            'width': (videowidth-50)+'px',
            'font-size': fontSize +'px'
        });

        metadata.subtitlesPanel = subtitlesPanel;
    };

    var convertTextToSubtitlesModel = function(text) {
        var splittedSubtitles = text.split(/(\n\n|\r\n\r\n)/g);
        var subtitlesModel = [];

        for (var i = 0; i < splittedSubtitles.length; i = i + 2) {
            if (!$.trim(splittedSubtitles[i])) {
                continue;
            }

            var entry = splittedSubtitles[i].split('\n');
            var time = $.trim(entry[1]).split(' --> ');

            subtitlesModel.push({
                startTime: getTimeInMillis(time[0]),
                endTime: getTimeInMillis(time[1]),
                text: $.trim(entry[2])
            });
        }

        return subtitlesModel;
    };

    var getTimeInMillis = function(timeAsString) {
        var timePartsWithMillis = timeAsString.split(',');
        var timePartsNoMillis = timePartsWithMillis[0].split(':');
        return (parseInt(timePartsNoMillis[0]) * 60 * 60 + parseInt(timePartsNoMillis[1]) * 60 + parseInt(timePartsNoMillis[2])) * 1000 + parseInt(timePartsWithMillis[1]);
    };

    var getSubtitleMetadata = function(element) {
        return $(element).is("video") ? $(element).find("track[kind='subtitle']").map(function () {
            return {
                video: $(element),
                url:$(this).attr('src'),
                text: undefined //<track> tag can not contain inner text
            };
        }).first() : {
            video: $(document.getElementById($(element).attr('data-video'))),
            url:$(element).attr('data-src'),
            text:$(element).text()
        };
    };

    var getVideoPosInMsec = function(video) {
        return ($(video)[0].currentTime * 1000).toFixed()
    };

    var findNextSuitableSubtitle = function(currentTime, currentIndex, subtitles) {
        for (var i = currentIndex; i < subtitles.length; i++) {
            var nextSubtitle = subtitles[i];
            if (currentTime <= nextSubtitle.endTime) {
                return i;
            }
        }

        return -1; //Index not found
    };

    var hideSubtitle = function(element) {
        console.log("hideSubtitle: " + element);
    };

    var eventListeners = {
        'play': function(event) {
            console.log("play: " + this.video[0].currentTime);
            this.currentSubtitleIndex = 0;
        },

        'ended': function(event) {
            console.log("ended: " + this.video[0].currentTime);
            this.currentSubtitleIndex = 0;
        },

        'seeked': function(event) {
            console.log("seeked: " + this.video[0].currentTime);
            var currentTime = getVideoPosInMsec(this.video);
            this.currentSubtitleIndex = findNextSuitableSubtitle(currentTime, 0, this.subtitles);
        },

        'timeupdate': function(event) {
            console.log("timeupdate: " + this.video[0].currentTime);

            var currentSubtitle = this.subtitles[this.currentSubtitleIndex];
            var currentTime = getVideoPosInMsec(this.video);
            var text = "";

            if (currentTime > currentSubtitle.endTime) {
                //Need to find next suitable subtitle to show
                this.currentSubtitleIndex = findNextSuitableSubtitle(currentTime, this.currentSubtitleIndex + 1, this.subtitles);
                currentSubtitle = this.subtitles[this.currentSubtitleIndex];
            }

            //Current subtitle is suitable to show
            if (currentTime >= currentSubtitle.startTime && currentTime <= currentSubtitle.endTime) {
                text = currentSubtitle.text;
            }

            this.subtitlesPanel.text(text);

        }
    };

    $.fn.extend({
        showSubtitles:function (opts) {

            var defaults = {
                fontSize:12,
                fontFamily:"Arial",
                fontColor:'#ccc'
            };

            var options = $.extend(defaults, opts);

            this.each(function () {
                //Assign current element to variable, in this case is UL element
                var subtitleMetadata = $.extend(options, getSubtitleMetadata($(this)));
                showSubtitle(subtitleMetadata);
            });
        },

        hideSubtitles: function() {
            this.each(function () {
                hideSubtitle($(this));
            });
        }
    });
})(jQuery);

