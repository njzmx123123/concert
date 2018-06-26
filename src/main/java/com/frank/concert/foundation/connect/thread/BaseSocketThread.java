package com.frank.concert.foundation.connect.thread;

import com.frank.concert.foundation.constants.LogConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;

/**
 * 用于处理Socket底层交互的抽象类，封装了心跳机制
 */
@Slf4j
public abstract class BaseSocketThread implements Runnable
{

    protected Socket socket;
    protected BufferedReader bufferedReader;
    protected PrintWriter printWriter;
    protected HeartBeatThread heartBeatThread;

    /**
     * Initialization the socket of this Thread
     */
    public void init()
    {

        //初始化输入输出流
        InputStreamReader reader;
        OutputStreamWriter writer;

        try
        {
            reader = new InputStreamReader(socket.getInputStream());
            writer = new OutputStreamWriter(socket.getOutputStream());
            this.bufferedReader = new BufferedReader(reader);
            this.printWriter = new PrintWriter(writer);

            //开始心跳
            //TODO:需要实现Thread中对对应Lind的信息注册，此处吧LinkId先行写死
            heartBeatThread = HeartBeatThread.getInstance();
            heartBeatThread.init(socket,"Test123");
            heartBeatThread.startHeartBeat();
        }
        catch (IOException e)
        {
            log.error(LogConstants.EX_ERROR + "There is an IOException occur, exception info: {}", e.getMessage());
            return;
        }

        log.debug("###Init SocketThread success!");
    };

    @Override
    public void run()
    {
        String msg = null;
        String resp = null;
        try
        {
            while((msg=bufferedReader.readLine())!=null)
            {
                resp = receiveMsg(msg);
                //TODO:为方便测试进行间隔延迟
                Thread.sleep(1000);
                sendRespMsg(resp);
            }
        }
        catch (IOException e)
        {
            log.error(LogConstants.EX_ERROR + "There is an IOException occur, exception info: {}", e.getMessage());
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 专用于发送回应的消息
     * @param msg
     */
    private void sendRespMsg(String msg)
    {
        if(StringUtils.isEmpty(msg))
        {
            log.error(LogConstants.EX_ERROR + "The resp msg is null which could not send...");
            return;
        }
        printWriter.println(msg);
        printWriter.flush();
    }

    /**
     * 通过接口的方式将底层消息的接收处理解耦出来
     * @param msg 接收到消息
     * @return
     */
    public abstract String receiveMsg(String msg);

    public void sendMsg(String msg){
        printWriter.println(msg);
        printWriter.flush();
    }
}
