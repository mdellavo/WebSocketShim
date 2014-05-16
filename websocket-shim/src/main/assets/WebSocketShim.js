

var log = function() {
    if (window.console) {
        console.log.call(console, arguments);
    }
}

if (!window.WebSocketInterface) {

    var WebSocketInterfaceMock = {
        newSocket: function() {
            var count = WebSocketInterface.sockets.length;
            log("mocking new socket -> " + count);
            WebSocketInterfaceMock.sockets[count] = null;

        },

        connect: function(socket, url) {
            log("mock connect socket -> " + socket + " : " + url);
            WebSocketInterfaceMock.sockets[socket] = new WebSocket(url);
        },

        close: function(socket) {
            WebSocketInterfaceMock.sockets[socket].close();
        },

        send: function(socket, data) {
            WebSocketInterfaceMock.sockets[socket].send(data);
        }

    }

    WebSocketInterfaceMock.sockets = {};

    window.WebSocketInterface = WebSocketInterfaceMock;
}

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
}

WebSocketShim.prototype.close = function() {
    WebSocketInterface.close(this.socket);
}

WebSocketShim.prototype.send = function(data) {
    WebSocketInterface.send(this.socket, data);
}

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
}

WebSocketShim.onMessage = function(socket, data) {
    var socket = WebSocketShim.getSocket(socket);
    if (socket && socket.onmessage) {
        socket.onmessage(data);
    }
}

WebSocketShim.onClose = function(socket, code, reason, wasClean) {
    var socket = WebSocketShim.getSocket(socket);
    if (socket && socket.onclose) {
        socket.onclose(new CloseEvent(code, reason, wasClean));
    }
}

window.WebSocketShim = WebSocketShim;

if (!window.WebSocket) {
    window.WebSocket = WebSocketShim;
}