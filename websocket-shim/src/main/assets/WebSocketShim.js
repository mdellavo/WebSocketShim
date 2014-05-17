

var log = function() {
    if (window.console) {
        console.log.call(console, arguments);
    }
};

var MessageEvent = function(data) {
    this.data = data;
};

var CloseEvent = function(code, reason, wasClean) {
    this.code = code;
    this.reason = reason;
    this.wasClean = wasClean;
};

var WebSocketShim = function(url) {
    this.URL = url;
    this.socket = WebSocketInterface.newSocket();
    this.readyState = WebSocketShim.CONNECTING;
    log("new socket -> " + this.socket);
    if (this.socket > -1) {
        WebSocketShim.sockets[this.socket] = this;
        log("connecting to " + url);
        WebSocketInterface.connect(this.socket, url);
    }
};

WebSocketShim.prototype.close = function() {
    this.readyState = WebSocketShim.CLOSING;
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
        socket.readyState = WebSocketShim.OPEN;
        socket.onopen();
    }
};

WebSocketShim.onMessage = function(socket, data) {
    var socket = WebSocketShim.getSocket(socket);
    if (socket && socket.onmessage) {
        socket.onmessage(new MessageEvent(data));
    }
};

WebSocketShim.onClose = function(socket, code, reason, wasClean) {
    var socket = WebSocketShim.getSocket(socket);
    if (socket && socket.onclose) {
        socket.readyState = WebSocketShim.CLOSED;
        socket.onclose(new CloseEvent(code, reason, wasClean));
    }
};

window.WebSocketShim = WebSocketShim;

if (!window.WebSocket) {
    window.WebSocket = WebSocketShim;
}

