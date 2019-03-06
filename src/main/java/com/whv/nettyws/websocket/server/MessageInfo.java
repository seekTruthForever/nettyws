package com.whv.nettyws.websocket.server;

import java.io.Serializable;

import io.netty.buffer.ByteBuf;

public class MessageInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -188021498725788622L;
	private String ctxId;
	private String method;
	private String nickname;
	private ByteBuf fileMessage;
	private String message;
	public String getCtxId() {
		return ctxId;
	}
	public void setCtxId(String ctxId) {
		this.ctxId = ctxId;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public ByteBuf getFileMessage() {
		return fileMessage;
	}
	public void setFileMessage(ByteBuf fileMessage) {
		this.fileMessage = fileMessage;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
