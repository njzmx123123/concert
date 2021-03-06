package com.frank.concert.foundation.connect.nio.handler;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

@Slf4j
public class FollowerReceiveHandler implements Runnable{

    private SocketChannel socketChannel;
    private SelectionKey sk;
    private Selector selector;

    //标识数字/
    private int flag = 0;
    //缓冲区大小/
    private int BLOCK = 4096;
    //接受数据缓冲区/
    private ByteBuffer sendbuffer = ByteBuffer.allocate(BLOCK);
    //发送数据缓冲区/
    private ByteBuffer receivebuffer = ByteBuffer.allocate(BLOCK);

    private static final int READING = 0;
    private static final int SENDING = 1;

    public FollowerReceiveHandler(Selector selector, SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        this.selector = selector;
    }

    public void init() throws IOException {

        socketChannel.configureBlocking(false);
        sk = socketChannel.register(selector, 0);

        sk.attach(this);

        //同时将SelectionKey标记为可读，以便读取。
        sk.interestOps(SelectionKey.OP_READ);
        selector.wakeup();
    }

    @Override
    public void run() {

        String receiveText;
        int count = 0;

        //将缓冲区清空以备下次读取
        receivebuffer.clear();
        //读取服务器发送来的数据到缓冲区中
        try {
            //TODO：暂不考虑半包的问题
            count = socketChannel.read(receivebuffer);
            if (count > 0) {
                receiveText = new String(receivebuffer.array(), 0, count);
                System.out.println("服务器端接受客户端数据--:" + receiveText);
                respMsg();
                socketChannel.register(selector, SelectionKey.OP_WRITE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void respMsg() throws IOException {
        sendbuffer.clear();
        sendbuffer.put("Leader got message!".getBytes());
        sendbuffer.flip();
        socketChannel.write(sendbuffer);
    }

}
