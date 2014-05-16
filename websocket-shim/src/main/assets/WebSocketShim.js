

var log = function() {
    if (window.console) {
        console.log.call(console, arguments);
    }
};

var CloseEvent = function(code, reason, wasClean) {
    this.code = code;
    this.reason = reason;
    this.wasClean = wasClean;
};

var WebSocketShim = function(url) {
    this.URL = url;
    this.socket = WebSocketInterface.newSocket();
    log("new socket -> " + this.socket);
    if (this.socket > -1) {
        WebSocketShim.sockets[this.socket] = this;
        log("connecting to " + url);
        WebSocketInterface.connect(this.socket, url);
    }
};

WebSocketShim.prototype.close = function() {
    WebSocketInterface.close(this.socket);
};

WebSocketShim.prototype.send = function(data) {
    WebSocketInterface.send(this.socket, data);
};

WebSocketShim.CONNECTING = 0;
WebSocketShim.OPEN = 1;
WebSocketShim.CLOSING = 2;
WebSocketShim.CLOSED = 3;

WebSocketShim.sockets = {};

WebSocketShim.getSocket = function (socket) {
    return WebSocketShim.sockets[socket];
};

WebSocketShim.onOpen = function(socket, uri) {
    var socket = WebSocketShim.getSocket(socket);
    if (socket && socket.onopen) {
        socket.onopen();
    }
};

WebSocketShim.onMessage = function(socket, data) {
    var socket = WebSocketShim.getSocket(socket);
    if (socket && socket.onmessage) {
        socket.onmessage(data);
    }
};

WebSocketShim.onClose = function(socket, code, reason, wasClean) {
    var socket = WebSocketShim.getSocket(socket);
    if (socket && socket.onclose) {
        socket.onclose(new CloseEvent(code, reason, wasClean));
    }
};

window.WebSocketShim = WebSocketShim;

if (!window.WebSocket) {
    window.WebSocket = WebSocketShim;
}

function test() {
    var url = "ws://echo.websocket.org";

    log("connecting to " + url);

    var timer;
    var sock = new WebSocketShim(url);

    function ping() {
        sock.send("ping!");
    }

    sock.onopen = function(e) {
        log("onopen -> " + e);
        timer = window.setInterval(ping, 5 * 1000);
    };

    sock.onclose = function(e) {
        log("onclose -> " + e);
        if (timer)
            window.clearInterval(timer);
    };

    sock.onmessage = function(e) {
        log("onmessage -> " + e);
    };

}

test();