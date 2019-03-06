package com.whv.nettyws.websocket.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class ChannelMap extends ConcurrentHashMap<String,List<Channel>> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2439319073420705706L;
	public ChannelMap() {
		super();
		}
	/**
	 * 放入Channel
	 * @param ctx
	 * @param key
	 */
	public void putChannel(final ChannelHandlerContext ctx,
			String key) {
		//记录通道
    	if(this.containsKey(key)){
    		boolean isNotInflag = isNotIn(ctx, key);
    		if(isNotInflag){
    			this.get(key).add(ctx.channel());
    		}
    	}else{
    		List<Channel> channels = new ArrayList<Channel>();
    		channels.add(ctx.channel());
    		this.put(key, channels);
    	}
	}
	/**
	 * 获取channelMap某项值的大小
	 * @param key 键
	 * @return
	 */
	public int getChannelSize(String key) {
		if(this.containsKey(key)) {
			return this.get(key).size();
		}else {
			return 0;
		}
	}
	/**
	 * 验证是否已存在channel
	 * @param ctx
	 * @param key
	 * @return
	 */
	public boolean isNotIn(final ChannelHandlerContext ctx,
			String key) {
		boolean notinflag=true;
		for(Channel chl:this.get(key)){
			if(chl.remoteAddress().equals(ctx.channel().remoteAddress())){
				notinflag=false;
				break;
			}
		}
		return notinflag;
	}
}
