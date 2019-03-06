package com.whv.nettyws.websocket.server;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

public class MessageFrameHandler2  extends SimpleChannelInboundHandler<WebSocketFrame> {
	private static final Logger logger = LoggerFactory.getLogger(MessageFrameHandler2.class);
	private static final ExecutorService threadPool = new ThreadPoolExecutor(5, 20, 60, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(), Executors.defaultThreadFactory(),new ThreadPoolExecutor.AbortPolicy());
	private static  ChannelMap channelMap = new ChannelMap();
	private static final String SUCCESS_FLAG = "1";//成功标识
	@Override
	protected void channelRead0(final ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
		if(frame instanceof TextWebSocketFrame) {
			final String request = ((TextWebSocketFrame) frame).text();
			logger.info("{} received {}", ctx.channel(), request);
			if(request!=null && "ping".equals(request)){//心跳维护
				return;
			}
			threadPool.submit(new Runnable() {
				@Override
				public void run() {
        			JSONObject jo = new JSONObject();
        			try{
        				jo = JSONObject.parseObject(request);
        			}catch(Exception e){
        				 sendData(ctx.channel(),"消息格式不正确");
        				 return;
        			}
        			boolean isPage = jo.get("IS_PAGE")==null?false:(boolean) jo.get("IS_PAGE");
        			isPage = isPage||(boolean)ctx.attr(AttributeKey.valueOf("isPage")).get();
        			ctx.attr(AttributeKey.valueOf("isPage")).set(isPage);
        			if(isPage){
        				if(jo.get("method")==null ){
		            		 sendData(ctx.channel(),"消息格式不正确");
		            		return;
		            	}
		            	sendData(ctx.channel(),SUCCESS_FLAG);
		            	String methodStr = jo.get("method").toString();
        				if("reg".equals(methodStr)){
        					if(jo.get("pageUUID")==null){
   		            		 	sendData(ctx.channel(),"消息格式不正确");
   		            		 	return;
        					}
        					String pageUUID = jo.get("pageUUID").toString();
        					ctx.attr(AttributeKey.valueOf("pageUUID")).set(pageUUID);
        					channelMap.putChannel(ctx, pageUUID);
			            	JSONObject jsonData = new JSONObject();
		            		jsonData.put("method", "reg");
		            		jsonData.put("ctxId", ctx.channel().id().asShortText());
		            		sendData(ctx.channel(),jsonData.toString());
		            	}else if("sendMessage".equals(methodStr)){
			            	JSONObject jsonData = new JSONObject();
		            		jsonData.put("method", "sendMessage");
		            		jsonData.put("ctxId", ctx.channel().id().asShortText());
		            		jsonData.put("nickname", ctx.channel().remoteAddress().toString());
		            		jsonData.put("message", jo.get("message").toString());
		            		sendData2Page(channelMap.get(ctx.attr(AttributeKey.valueOf("pageUUID")).get()),jsonData.toString());
		            	}
        			}else{
 		        	   sendData(ctx.channel(),"消息格式不正确");
 		        	   return;
 		           }
					
				}
			});
		}else if(frame instanceof BinaryWebSocketFrame){
			 logger.info("服务器接收到二进制消息.");  
			 final WebSocketFrame bFrame = frame.copy();
			 threadPool.submit(new Runnable() {
				@Override
				public void run() {
					if((boolean) ctx.attr(AttributeKey.valueOf("isPage")).get()){
						ByteBuf content = bFrame.content(); 
				        content.markReaderIndex();  
				        int flag = content.readInt();  
				        logger.info("标志位:[{}]", flag);  
				        content.resetReaderIndex();  
				  
				        ByteBuf byteBuf = Unpooled.directBuffer(content.capacity());  
				        byteBuf.writeInt(content.capacity());
				        byteBuf.writeBytes(content);  
				        JSONObject jsonData = new JSONObject();
	            		jsonData.put("method", "sendBlobMessage");
	            		jsonData.put("ctxId", ctx.channel().id().asShortText());
	            		jsonData.put("nickname", ctx.channel().remoteAddress().toString());
	            		ByteBufUtil.writeUtf8(byteBuf, jsonData.toJSONString());
	            		sendBlobData2Page(channelMap.get(ctx.attr(AttributeKey.valueOf("pageUUID")).get()),byteBuf);
					}else {
						
					}
					
				}
				 
			 });
		        
		}else {
			
		}
		
	}
	/*public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
	  {
	    try {
	      if (acceptInboundMessage(msg))
	      {
	        Object imsg = msg;
	        channelRead0(ctx, (WebSocketFrame) imsg);
	      } else {
	        ctx.fireChannelRead(msg);
	      }
	    } finally {
	    }
	  }*/
	 /**
     * 批量发送消息
     * @param channelList
     * @param dataStr
     */
    private void sendData2Page(List<Channel> channelList, String dataStr) {
    	if(channelList!=null&&channelList.size()>0){
			for (Iterator<Channel> it = channelList.iterator(); it.hasNext();) {
				Channel channel = it.next();
				if (channel.isActive()) {
					sendData(channel,dataStr);
					logger.info(channel.id()+"发送信息……"+channel.remoteAddress());
				} else {
					channel.close();
					it.remove();
				}
			}
    	}
	}
    /**
     * 发送消息
     * @param channel
     * @param dataStr
     */
    private void sendData(Channel channel,String dataStr){
    	channel.writeAndFlush(new TextWebSocketFrame(dataStr));
    }
    /**
     * 批量发送消息
     * @param channelList
     * @param jsonData
     */
    private void sendBlobData2Page(List<Channel> channelList, ByteBuf obj) {
    	if(channelList!=null&&channelList.size()>0){
			for (Iterator<Channel> it = channelList.iterator(); it.hasNext();) {
				Channel channel = it.next();
				if (channel.isActive()) {
					sendBlobData(channel,obj.copy());
					logger.info(channel.id()+"发送信息……"+channel.remoteAddress());
				} else {
					channel.close();
					it.remove();
				}
			}
    	}
	}
    /**
     * 发送消息
     * @param channel
     * @param dataStr
     */
    private void sendBlobData(Channel channel, ByteBuf obj){
    	channel.writeAndFlush(new BinaryWebSocketFrame(obj));
    }
    /**
	 * 一段时间未进行读写操作 回调
	 */
	
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		super.userEventTriggered(ctx, evt);
	
		if (evt instanceof IdleStateEvent) {

			IdleStateEvent event = (IdleStateEvent) evt;
			
			if (event.state().equals(IdleState.READER_IDLE)) {
				//未进行读操作
				logger.info("READER_IDLE");
				// 超时关闭channel
				 ctx.close();

			} else if (event.state().equals(IdleState.WRITER_IDLE)) {
				

			} else if (event.state().equals(IdleState.ALL_IDLE)) {
				//未进行读写
				logger.info("ALL_IDLE");
				// 发送心跳消息
				sendData(ctx.channel(),"pong");
				
			}

		}
	}
}
