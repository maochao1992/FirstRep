package com.websocket.handler;

import io.netty.channel.ChannelHandlerContext;

import com.util.CodecUtil;
import com.websocket.bean.HttpRequstBean;
import com.websocket.bean.HttpResponseBean;
import com.websocket.decoder.WebsocketDecoder;
import com.websocket.decoder.WebsocketEncoder;

public class HttpRequstHandler {

	public void handle(HttpRequstBean requstBean,ChannelHandlerContext ctx) {
		HttpResponseBean responseBean = new HttpResponseBean();
		//是websocket握手请求
		if(requstBean.getHeads().get("Upgrade")!=null&&
			requstBean.getHeads().get("Upgrade").indexOf("websocket")>-1&&
			requstBean.getHeads().get("Connection")!=null&&
			requstBean.getHeads().get("Connection").indexOf("Upgrade")>-1){
			String secWebSocketKey = requstBean.getHeads().get("Sec-WebSocket-Key");
			if(secWebSocketKey!=null){
				responseBean.setProto(requstBean.getProto());
				responseBean.setStatus(101);
				responseBean.setStatusMsg("Switching Protocols");
				responseBean.getHeads().put("Connection","Upgrade");
				responseBean.getHeads().put("Upgrade","websocket");
				/*
				 * RFC6455
				 * 握手响应：BASE64(SHA1(Sec-WebSocket-Key+258EAFA5-E914-47DA-95CA-C5AB0DC85B11))
				 */
				responseBean.getHeads().put("Sec-WebSocket-Accept",CodecUtil.base64(CodecUtil.SHA1(secWebSocketKey+"258EAFA5-E914-47DA-95CA-C5AB0DC85B11")));
				ctx.writeAndFlush(responseBean);
				//握手成功进入websocket双向通信,移除http编、解码器
				ctx.pipeline().remove("http-decoder");
				ctx.pipeline().remove("http-encoder");
				//添加websocket frame编、解码器
				ctx.pipeline().addFirst(new WebsocketDecoder());
				ctx.pipeline().addFirst(new WebsocketEncoder());
			}
		}
	
		
	}

}
