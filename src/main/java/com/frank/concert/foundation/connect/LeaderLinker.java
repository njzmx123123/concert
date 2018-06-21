package com.frank.concert.foundation.connect;

import com.frank.concert.foundation.connect.listener.RollCallListener;
import com.frank.concert.foundation.connect.model.LeaderSocket;
import com.frank.concert.foundation.connect.thread.BaseSocketThread;
import com.frank.concert.foundation.connect.thread.RollCallThread;
import com.frank.concert.foundation.constants.SocketConstants;
import com.frank.concert.foundation.tools.IdTool;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Leader 服务器使用单例模式
 */
@Slf4j
public class LeaderLinker extends BaseLinker implements RollCallListener {

    private static ServerSocket leaderServceSocket;
    private static List<LeaderSocket> leaderSocketList = new ArrayList<>();

    private static ExecutorService leaderSocketThreadPool;

    private static String leaderIp;
    private static int leaderPort;

    //第一步：获得leader socket service实例的 uuid
    public LeaderLinker() {
        linkerId = SocketConstants.LeaderPrefix + IdTool.getUUID();
    }

    //第二步：初始化建立leader所用的ip与端口号
    public synchronized void init(String leaderIp, int leaderPort) {

        this.leaderIp = leaderIp;
        this.leaderPort = leaderPort;
        this.leaderSocketThreadPool = Executors.newCachedThreadPool();

        log.debug("###Initing leaderLinker socket service [ip:{},port:{}]...", leaderIp, leaderPort);

        try {
            leaderServceSocket = new ServerSocket(leaderPort);
            //start the thread of roll call
            new Thread(new RollCallThread(leaderServceSocket, this), linkerId).start();

        } catch (IOException e) {
            log.error("###The serviceSocket of leader init fail, IOException: {}", e.getMessage());
        }

        log.debug("###Init leaderLinker socket service success!");
    }

    @Override
    public void followerReply(LeaderSocket leaderSocket) {
        registerNewFollower(leaderSocket);
    }

    //将建立与leader建立连接follower的socket信息进行注册，并且在线程池中启动一个专用于与follower交互的线程用于处理
    private synchronized void registerNewFollower(LeaderSocket leaderSocket) {
        log.debug("###Registering new follower[HostName:{} HostIp:{} HostPort:{}]",
                leaderSocket.getFollowerHostName(), leaderSocket.getFollowerHostIp(),
                leaderSocket.getFollowerHostPort());

        leaderSocketList.add(leaderSocket);

        leaderSocketThreadPool.execute(leaderSocket.getLeaderSocketThread());
    }

    private static class SinglerHolder {
        private static final LeaderLinker INSTANCE = new LeaderLinker();
    }

    public static final LeaderLinker getInstance() {
        return SinglerHolder.INSTANCE;
    }

}