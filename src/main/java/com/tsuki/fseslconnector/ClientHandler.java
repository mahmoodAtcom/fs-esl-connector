package com.tsuki.fseslconnector;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

public class ClientHandler extends SimpleChannelInboundHandler {

    boolean authRecevied = false;
    boolean authEolRecevied = false;
    boolean authResponseSent = false;
    boolean authValidationReceived = false;
    boolean authValidationStatusReceived = false;
    boolean authSuccess = false;

    boolean sentEventsJsonSubcribe = false;

    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext) {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) {
        cause.printStackTrace();
        channelHandlerContext.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = ((ByteBuf) msg).toString(CharsetUtil.UTF_8);

        if (message.equals("Content-Type: auth/request")) {
            authRecevied = true;
            return;
        }
        if (authRecevied && message.length() == 0 && !authEolRecevied) {
            authEolRecevied = true;
            System.out.println("sending auth response");
            ctx.writeAndFlush(Unpooled.copiedBuffer("auth Hala_1994\n\n", CharsetUtil.UTF_8));
            authResponseSent = true;
            return;
        }
        if (authRecevied && authEolRecevied && authResponseSent && !authValidationReceived
                && message.equals("Content-Type: command/reply")) {
            authValidationReceived = true;
            return;
        }
        if (authRecevied && authEolRecevied && authResponseSent && authValidationReceived
                && !authValidationStatusReceived
                && message.equals("Reply-Text: +OK accepted")) {
            authValidationStatusReceived = true;
            authSuccess = true;
            return;
        }
        if (authSuccess && !sentEventsJsonSubcribe && message.length() == 0) {
            ctx.writeAndFlush(Unpooled.copiedBuffer("events plain all\n\n", CharsetUtil.UTF_8));
            sentEventsJsonSubcribe = true;
            return;
        }

        if (message.startsWith("Content-Type")) {
            System.out.println("============Message Received============");
            return;
        }
        if (message.startsWith("Content-Length")) {
            return;
        }
        if (message.length() == 0) {
            System.out.println("============Message ENDED ============");
            return;
        }
        System.out.println(message);
    }
}