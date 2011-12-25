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
    var supportsTouch = ('createTouch' in document);

    var touchHandler = function (event) {
        var touches = event.changedTouches,
            first = touches[0],
            type = "";

        switch (event.type) {
            case "touchstart":
                type = "mousedown";
                break;
            case "touchmove":
                type = "mousemove";
                break;
            case "touchend":
                type = "mouseup";
                break;
            default:
                return;
        }

        //initMouseEvent(type, canBubble, cancelable, view, clickCount,
        //           screenX, screenY, clientX, clientY, ctrlKey,
        //           altKey, shiftKey, metaKey, button, relatedTarget);

        var simulatedEvent = document.createEvent("MouseEvent");
        simulatedEvent.initMouseEvent(type, true, true, window, 1,
            first.screenX, first.screenY,
            first.clientX, first.clientY, false,
            false, false, false, 0/*left*/, null);

        first.target.dispatchEvent(simulatedEvent);
        event.preventDefault();
    };

    $.fn.extend({
        bindTouchToMouse:function () {
            if (supportsTouch) {
                this.each(function () {
                    this.addEventListener("touchstart", touchHandler, true);
                    this.addEventListener("touchmove", touchHandler, true);
                    this.addEventListener("touchend", touchHandler, true);
                    this.addEventListener("touchcancel", touchHandler, true);
                });
            }
            return this;
        },

        unbindTouchToMouse:function () {
            if (supportsTouch) {
                this.each(function () {
                    this.removeEventListener("touchstart", touchHandler, true);
                    this.removeEventListener("touchmove", touchHandler, true);
                    this.removeEventListener("touchend", touchHandler, true);
                    this.removeEventListener("touchcancel", touchHandler, true);
                });
            }
            return this;
        }
    });
})(jQuery);
