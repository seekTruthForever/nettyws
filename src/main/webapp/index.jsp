<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<%
//socketPort端口号
String socketPort=application.getInitParameter("socketPort");
socketPort = socketPort==null?"8080":socketPort;
StringBuffer sb = new StringBuffer();
sb.append(request.getScheme());
sb.append("://");
sb.append(request.getServerName());
sb.append(":");
sb.append(request.getServerPort());
sb.append(request.getContextPath());
String skinsPre = sb.toString()+"/skins";
%>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge;IE=10;IE=9;IE=8;IE=7">
    <title>首页</title>
    <script type="text/javascript" src="<%=skinsPre %>/js/jquery1.9.js" ></script>
	<script type="text/javascript" src="<%=skinsPre %>/js/webSocketFunc.js" ></script>
	 <style type="text/css">
	body {
	    background-color: #008C7B;
	    color:#FFFFFF;
	    width: 1255px;
	    height: 505px;
	}
	
	div.messageShowDiv{width:80%;height:300px;margin:0 auto;overflow-y:auto;}
	div.messageInputDiv{width:50%;height:50px;margin:0 auto;}
	div.messageInputDiv textarea{width:80%;height:100%;}
	#sendMessage{height:100%;vertical-align:top;}
	div.selfMessage{
		width:40%;
		height:auto;
		margin-left:50%;
		margin-right:10px;
		margin-top:10px;
		text-align:right;
	}
	div.selfMessage>.nickname{
		display:inline-block;
		margin-left:10px;
		float:right;
		background-color:red;
	}
	div.selfMessage>.message{
		display:inline-block;
		padding:5px;
		max-width:40%;
		background-color:blue;
		word-break:normal;
		white-space:pre-wrap;
		word-wrap : break-word ;
		overflow: hidden ;
		text-align:left;
	}
	div.otherMessage{
		width:40%;
		height:auto;
		margin-left:10px;
		margin-top:10px;
	}
	div.otherMessage>.nickname{
		display:inline-block;
		margin-right:10px;
		float:left;
		background-color:red;
	}
	div.otherMessage>.message{
		display:inline-block;
		padding:5px;
		max-width:40%;
		background-color:gray;
		word-break:normal;
		white-space:pre-wrap;
		word-wrap : break-word ;
		overflow: hidden ;
	}
	</style>
<script type=text/javascript>
var pageParam = {'IS_PAGE':true,'pageUUID':'testMessage'};
        $(function(){
        	//websocket相关方法初始化
        	var webSocketFunc = new WebSocketFunc({
				"url":"ws://<%=request.getServerName()+":"+socketPort%>/websocket"
			});
			// 打开Socket 
			webSocketFunc.onopen = function(event){
			  	//握手 发送消息
			   this.send("{IS_PAGE:"+pageParam.IS_PAGE+",pageUUID:'"+pageParam.pageUUID+"',method:'reg'}");
			};
			// 监听消息
			webSocketFunc.onmessage = function(event){
				if(event.data){
				   try{
					   if(typeof  event.data === "string"){
					    	var jsonData = JSON.parse(event.data);
					    	getData(jsonData);
					   }else{
						   getObjData(event);
					   }
				    }catch(e){
				    	if(event.data == "1"){
								console.log("success");
							}else if(event.data == "pong"){
								 this.send("ping");//心跳维护
							}else{
								console.log("exception");
							}
				    }
			    }
			 };
			//初始化并打开websocket
			webSocketFunc.init();
			var opt = {'webSocketFunc':webSocketFunc,'pageParam':pageParam};	
			sendData(opt);
			// 发送图片  
	        $('#send-pic').on('change', function (ev) {
	            var files = this.files;  
	            if (files && files.length) {  
	                var file = files[0];  
	                var fileType = file.type;  
	                // 表示传递的是 图片  
	                var dataType = 20;  
	                if (!/^image/.test(fileType)) {  
	                	// 表示传递的是 非图片  
	                    dataType = 10;  
	                    return;  
	                }  
	                var fileReader = new FileReader();  
	                fileReader.readAsArrayBuffer(file);  
	                fileReader.onload = function (e) {  
	                    // 获取到文件对象  
	                    var result = e.target.result;  
	                    // 创建一个 4个 字节的数组缓冲区  
	                    var arrayBuffer = new ArrayBuffer(4);  
	                    var dataView = new DataView(arrayBuffer);  
	                    // 从第0个字节开始，写一个 int 类型的数据(dataType)，占4个字节  
	                    dataView.setInt32(0, dataType);  
	                    // 组装成 blob 对象  
	                    var blob = new Blob([arrayBuffer, result]);  
	                    // 发送到 webSocket 服务器端  
	                    webSocketFunc.send(blob);  
	                }  
	            }  
	        }); 
        });
        function sendData(obj){
	        var webSocketFunc = obj.webSocketFunc;
	        var pageParam = obj.pageParam;
	        $("#sendMessage").on("click",function(){
		        var message = $("#inputMessageText").val();
		        webSocketFunc.send("{method:'sendMessage',message:'"+message+"'}");
	        });
        }
        function getData(obj){
        	
	        var methodFlag = obj.method;
	        if(methodFlag=='reg'){
	        	var ctxId = obj.ctxId;
	        	$("#ctxId").val(ctxId);
	        }else if(methodFlag=='sendMessage'){
	        	var message = obj.message;
	        	var ctxId = obj.ctxId;
	        	var nickname = obj.nickname;
	        	var pageCtxId = $("#ctxId").val();
	        	if(pageCtxId==null || pageCtxId==""){
	        		return;
	        	}
	        	var className = "selfMessage";
	        	if(ctxId == pageCtxId){
	        		className = "selfMessage";
	        	}else{
	        		className = "otherMessage";
	        	}
	        	$("div.messageShowDiv").append("<div class='"+className+"'><span class='nickname'>"+nickname+"</span><span class='message'>"+message+"</span></div>");
	        }
        }
        function getObjData(obj){
        	
        	var result = obj.data;
        	 var flagReader = new FileReader();  
             flagReader.readAsArrayBuffer(result.slice(0,4)); 
           	//flagReader.readAsText(result); 
             flagReader.onload = function (flagTarget) {  
           		var fileSize = new DataView(flagTarget.target.result).getInt32(0);
           		var jsonReader = new FileReader(); 
           		jsonReader.readAsText(result.slice(4+fileSize,result.size)); 
           		jsonReader.onload = function (jsonTarget) {
           			var jsonData = JSON.parse(jsonTarget.target.result);
    	           	var message = jsonData.fileMessage;
    	        	var ctxId = jsonData.ctxId;
    	        	var nickname = jsonData.nickname;
    	        	var pageCtxId = $("#ctxId").val();
    	        	if(pageCtxId==null || pageCtxId==""){
    	        		return;
    	        	}
    	        	var className = "selfMessage";
    	        	if(ctxId == pageCtxId){
    	        		className = "selfMessage";
    	        	}else{
    	        		className = "otherMessage";
    	        	}
    	        	 var fileReader = new FileReader();  
    	             fileReader.readAsArrayBuffer(result.slice(4,8)); 
    	             fileReader.onload = function (fileTarget) { 
    	            	 if (new DataView(fileTarget.target.result).getInt32(0) === 20) {  
    	                     var imageReader = new FileReader();  
    	                     imageReader.readAsDataURL(result.slice(8,fileSize));  
    	                     imageReader.onload = function (img) {  
    	                         var imgHtml = "<img src='" + img.target.result + "' style='width: 100%;height: auto;'>";  
    	                         imgHtml = imgHtml.replace("data:application/octet-stream;", "data:image/png;")
    	                         + "<br />";  
    	                         $("div.messageShowDiv").append("<div class='"+className+"'><span class='nickname'>"+nickname+"</span><span class='message'>"+imgHtml+"</span></div>");
    	                     }  
    	                 } else {  
    	                     alert("后端返回的是非图片类型数据，无法显示。");  
    	                 }   
    	             }
           		}
	           
	        	
                
             }
        }
         
</script>
</head>
<body class="padding10 no-padding-top no-padding-bottom">
<input type="hidden" id="ctxId" value="">
<div class="messageShowDiv">

</div>
<div class="messageInputDiv">
<textarea rows="10" cols="50" id="inputMessageText"></textarea>
<input type="button" value="发送" id="sendMessage"><br/>
选择图片： <input type="file" id="send-pic">  
</div>
</body>
</html>