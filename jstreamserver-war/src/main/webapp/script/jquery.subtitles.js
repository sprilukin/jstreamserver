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
    var REMOVE_FIRST_ZERO_REGEXP = /^0/g;

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

        loadSubtitlesText(metadata, function(text) {
            metadata.subtitles = convertTextToSubtitlesModel(text, metadata.offset);
            metadata.text = null; //free memory;
            metadata.currentSubtitleIndex = 0; //start showing subtitles from the beginning
            createSubtitlesMarkup(metadata);
            $.each(eventListeners, function(eventName, handler) {
                $(metadata.video).bind(eventName, $.proxy(handler, metadata));
            })
        })
    };

    var createSubtitlesMarkup = function (metadata) {
        var videowidth = $(metadata.video).width();

        var fontSize = metadata.fontSize;
        if (videowidth > 400) {
            fontSize = fontSize + Math.ceil((videowidth - 400) / 100);
        }

        var videoWrapper = $(metadata.video).parents(".subtitles-video-wrapper").get(0);
        var subtitlesPanel = $(videoWrapper).find(".subtitles-panel");
        subtitlesPanel.css({
            'width': (videowidth - 50)+'px', //50 is sum of paddings from css: 25 + 25
            'font-size': fontSize +'px'
        });

        metadata.subtitlesPanel = subtitlesPanel;
    };

    //Converts subtitles text to object model
    //offset is time on which subtitles is faster than video
    //it will be subtracted from subtitles time to sync video and audio
    var convertTextToSubtitlesModel = function(text, offset) {
        var splittedSubtitles = text.split(/(\n\n|\r\n\r\n)/g);
        var subtitlesModel = [];
        offset = getTimeInMillis(offset);

        for (var i = 0; i < splittedSubtitles.length; i = i + 2) {
            if (!$.trim(splittedSubtitles[i])) {
                continue;
            }

            var entry = splittedSubtitles[i].split('\n');
            var time = $.trim(entry[1]).split(' --> ');

            subtitlesModel.push({
                startTime: getTimeInMillis(time[0]) - offset,
                endTime: getTimeInMillis(time[1]) - offset,
                text: $.trim(entry.splice(2).join(" "))
            });
        }

        return subtitlesModel;
    };

    var convertTimePartToInt = function(timePartAsString) {
        var normalizedTimePart = timePartAsString;
        while (normalizedTimePart.length > 1 && normalizedTimePart.substr(0, 1) === "0") {
            normalizedTimePart = normalizedTimePart.replace(REMOVE_FIRST_ZERO_REGEXP, "");
        }

        return parseInt(normalizedTimePart);
    };

    var getTimeInMillis = function(timeAsString) {
        var timePartsWithMillis = timeAsString.split(',');
        var timePartsNoMillis = timePartsWithMillis[0].split(':');
        return (convertTimePartToInt(timePartsNoMillis[0]) * 60 * 60 + convertTimePartToInt(timePartsNoMillis[1]) * 60 + convertTimePartToInt(timePartsNoMillis[2])) * 1000 + convertTimePartToInt(timePartsWithMillis[1]);
    };

    var getTimeAsString = function(timeInMillis) {
        var millis = "" + timeInMillis % 1000;
        while (millis.length < 3) {
            millis = "0" + millis;
        }
        timeInMillis = Math.floor(timeInMillis / 1000);

        var seconds = "" + timeInMillis % 60;
        while (seconds.length < 2) {
            seconds = "0" + seconds;
        }
        timeInMillis = Math.floor(timeInMillis / 60);

        var minutes = "" + timeInMillis % 60;
        while (minutes.length < 2) {
            minutes = "0" + minutes;
        }

        var hours = "" + Math.floor(timeInMillis / 60);
        while (hours.length < 2) {
            hours = "0" + hours;
        }

        return [hours, minutes, seconds].join(":") + "," + millis;
    };

    var getSubtitleMetadata = function(element) {
        return {
            video: document.getElementById($(element).attr('data-video')),
            url:$(element).attr('data-src'),
            text:$(element).text()
        };
    };

    var getVideoPosInMsec = function(video) {
        return Math.floor(video.currentTime * 1000);
    };

    var findNextSuitableSubtitle = function(currentTime, currentIndex, subtitles) {
        for (var i = currentIndex; i < subtitles.length; i++) {
            var nextSubtitle = subtitles[i];
            if (currentTime < nextSubtitle.endTime) {
                return i;
            }
        }

        return 0; //Index not found
    };

    var hideSubtitle = function(element) {
        //TODO: remove listeners
        console.log("hideSubtitle: " + element);
    };

    var eventListeners = {
        'play': function(event) {
            var currentTime = getVideoPosInMsec(this.video);
            this.currentSubtitleIndex = findNextSuitableSubtitle(currentTime, 0, this.subtitles);
        },

        'ended': function(event) {
            /* do nothing */
        },

        'seeked': function(event) {
            var currentTime = getVideoPosInMsec(this.video);
            this.currentSubtitleIndex = findNextSuitableSubtitle(currentTime, 0, this.subtitles);
        },

        'timeupdate': function(event) {
            var currentSubtitle = this.subtitles[this.currentSubtitleIndex];
            var currentTime = getVideoPosInMsec(this.video);
            var text = "";

            if (currentTime > currentSubtitle.endTime) {
                //Need to find next suitable subtitle to show
                this.currentSubtitleIndex = findNextSuitableSubtitle(currentTime, this.currentSubtitleIndex + 1, this.subtitles);
                currentSubtitle = this.subtitles[this.currentSubtitleIndex];
            } else if (currentTime < currentSubtitle.startTime) {
                //backward seeking was applied
                this.currentSubtitleIndex = findNextSuitableSubtitle(currentTime, 0, this.subtitles);
                currentSubtitle = this.subtitles[this.currentSubtitleIndex];
            }

            //Current subtitle is suitable to show
            if (currentTime >= currentSubtitle.startTime && currentTime <= currentSubtitle.endTime) {
                text = currentSubtitle.text;
            }

            //console.log("timeupdate: " + currentSubtitle.startTime + " | " + this.video.currentTime + " " + this.video.startTime + " | " + currentSubtitle.endTime + " | " + text);
            this.subtitlesPanel.text(text);
        }
    };

    $.fn.extend({
        showSubtitles:function (opts) {

            var defaults = {
                fontSize:20,
                fontFamily:"Arial",
                fontColor:'#ccc',
                offset: "00:00:00,000"
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

    $.extend({
        convertTimeToMillis: function(timeAsString) {
            return getTimeInMillis(timeAsString);
        },

        convertMillisToString: function(timeInMillis) {
            return getTimeAsString(timeInMillis);
        }
    })
})(jQuery);

