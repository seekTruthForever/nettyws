/**
*websocket操作类
*@author wuhuawei
*/
var WebSocketFunc = function(setting){
	for (var key in setting){
      		this.__proto__[key] =setting[key];
    	}
}
WebSocketFunc.prototype = {
	socket:null,
	url:"ws://127.0.0.1:8080/websocket",
	onmessage:function(event){
		if(event.data&&event.data.length>0){
			console.log("socket接收到消息："+event.data);
			  
		    }
	},
	onopen:function(event){
		alert("socket已打开");
	},
	onclose:function(event){
		console.log("socket已经断开连接，请检查服务是否启动。");
	/*
		if(confirm("socket已经断开连接，请检查服务是否启动。服务启动后，点击“确定”按钮重新载入页面。")){
			window.location.reload();
		 }
		 */
	},
	init:function(){
		var optObj = this;
		if (!window.WebSocket) {
		  window.WebSocket = window.MozWebSocket;
		}
		if (window.WebSocket) {
		  this.socket = new WebSocket(this.url);
		  this.socket.onmessage = function(event) {
		    optObj.onmessage(event);
		  };
		  this.socket.onopen = function(event) {
		  	optObj.onopen(event);
		  };
		  this.socket.onclose = function(event) {
		   	 optObj.onclose(event);
		  };
		} else {
			console.log("您的浏览器不支持使用websocket，请换用支持html5的浏览器");
		  alert("您的浏览器版本不支持此页面的动态展现，请换用支持html5的浏览器");
		}
	},
	send:function(message){
		var socket = this.socket;
		 if (!window.WebSocket) { return; }
		  if (socket.readyState == WebSocket.OPEN) {
		    socket.send(message);
		  } else {
		  	console.log("socket已经断开连接，请检查服务是否启动。");
		  /*
		    if(confirm("socket已经断开连接，请检查服务是否启动。服务启动后，点击“确定”按钮重新载入页面。")){
			    window.location.reload();
		    }
		    */
		  }
	}
};