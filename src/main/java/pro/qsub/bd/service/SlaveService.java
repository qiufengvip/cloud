package pro.qsub.bd.service;


import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Component;
import pro.qsub.bd.entity.RequestData;
import pro.qsub.bd.entity.Server;
import pro.qsub.bd.utils.CircleVoteRole;
import pro.qsub.bd.utils.HttpRequest;
import pro.qsub.bd.utils.IniUtil;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;

/**
 * @desc 从服务
 * 任务： 心跳  选主
 */

@Component
public class SlaveService {

    // 主 服务器
    private static Server MAIN_MASTER;


    // 当前 主服务器地址    选主完成后需要重新接收
    private static Server MASTER;

    //所有服务器列表《》
    private static List<Server> SLAVE_list;

    // 自己的信息
    private static Server myServer;

    //连接失败次数
    private static int failed = 0;

    //日志写入次数
    private static int logCount = 97;

    //日志文件地址
    private static String logPath;

    /**
     * 初始化
     */
    static {
        System.out.println("==============================开始读取配置文件===============================");
        try {
            String IniPath = SlaveService.class.getClassLoader().getResource("sever.ini").getFile();
            //ip
            String ip = IniUtil.getProfileString(IniPath,"sever","ip","");
            //name
            String name = IniUtil.getProfileString(IniPath,"sever","name","");
            //port
            String port = IniUtil.getProfileString(IniPath,"sever","port","8080");
            //state
            String state = IniUtil.getProfileString(IniPath,"sever","state","1");
            //timestamp
            String timestamp = IniUtil.getProfileString(IniPath,"sever","timestamp","0");
            //logpath
            logPath = IniUtil.getProfileString(IniPath,"sever","logpath","D:/sever.log").toString();
            System.out.println("ip:"+ip);
            System.out.println("name:"+name);
            System.out.println("port:"+port);
            System.out.println("state:"+state);
            System.out.println("timestamp:"+timestamp);
            System.out.println("logPath:"+logPath);
            MAIN_MASTER = new Server(ip, name, port, Integer.parseInt(state),Integer.parseInt(timestamp));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //启动类
    @PostConstruct
    private void starts() {
        // 开机的 主服务器
        MASTER = MAIN_MASTER;
        myServer = new Server(getIp(), getHostName(), "8080", 1, 0);
    }


    /**
     * @Desc 心跳 qz
     */
    public static void heartbeat() {
        //判断主服务器是不是自己  是自己的话就不用在心跳了
        if (myServer.getIp().equals(MASTER.getIp()))
            return;
        System.out.println("===================");
        System.out.println("||心跳开始\t\t||");
        System.out.println("||主服务器IP:"+ MASTER.getIp()+"\t\t||");
        System.out.println("||主服务器port:"+ MASTER.getPort()+"\t\t||");
        System.out.println("--------------------------");
        System.out.println("||当前服务器IP:"+ myServer.getIp()+"\t\t||");
        System.out.println("||当前服务器port:"+ myServer.getPort()+"\t\t||");
        System.out.println("===================");

        String time = "===================="+new Date()+"====================";
        //写入日志
        //写入时间
        IniUtil.writeFile(logPath,time,logCount);
        IniUtil.writeFile(logPath,"===================",logCount);
        IniUtil.writeFile(logPath,"||心跳开始\t\t||",logCount);
        IniUtil.writeFile(logPath,"||主服务器IP:"+ MASTER.getIp()+"\t\t||",logCount);
        IniUtil.writeFile(logPath,"||主服务器port:"+ MASTER.getPort()+"\t\t||",logCount);
        IniUtil.writeFile(logPath,"--------------------------",logCount);
        IniUtil.writeFile(logPath,"||当前服务器IP:"+ myServer.getIp()+"\t\t||",logCount);
        IniUtil.writeFile(logPath,"||当前服务器port:"+ myServer.getPort()+"\t\t||",logCount);
        IniUtil.writeFile(logPath,"===================",logCount);

        String URL = "http://" + MASTER.getIp() + ":" + MASTER.getPort() + "/cloud_war_exploded/heartbeat";
        // 能不能发送请求
        if (myServer == null)
            return;
        //向主服务器传递的参数
        HashMap<String, String> map = new HashMap<>();
        // 本机 ip
        map.put("ip", myServer.getIp());
        // 本机监听的端口
        map.put("port", myServer.getPort());
        // 本机的姓名
        map.put("name", myServer.getName());
        // 本机与主服务器连接的状态
        map.put("state", "1");
        // 请求发送时间
        map.put("timestamp", System.currentTimeMillis() + "");
        // 主服务器异常
        try {
//            System.out.println(URL);
            HttpRequest httpRequest = new HttpRequest(URL, "POST", map);
            // 重连接成功 失败次数 == 0
            SlaveService.failed = 0;
            //更新自身信息
            myServer.setTimestamp(System.currentTimeMillis());
            myServer.setState(1);
            // 存入服务穷链表
            parseJsonToObject(httpRequest.getData());
            // 主没有死 但是大部分从连接不上主
            forceMaster();


        } catch (Exception e) {
            //更新自己的状态 为0
            myServer.setState(0);
            // 与主连接失败3次,找别的从 执行策略
            if (SlaveService.failed >= 3) {
                //让连接失败次数重新归零
                SlaveService.failed = 0;
                // 执行霸道选举 换主策略
                MASTER= Vote.updateMaster(SLAVE_list, new CircleVoteRole());
            }else {
                SlaveService.failed += 1;
                System.err.println("==========");
                System.err.println("|| 与主服务器连接出现异常||");
                System.err.println("|| 异常次数为："+ SlaveService.failed +"\t||");
                System.err.println("==========");

                //写入日志
                IniUtil.writeFile(logPath,"==========",logCount);
                IniUtil.writeFile(logPath,"|| 与主服务器连接出现异常||",logCount);
                IniUtil.writeFile(logPath,"|| 异常次数为："+ SlaveService.failed +"\t||",logCount);
                IniUtil.writeFile(logPath,"==========",logCount);
            }

        }
        finally {
            logCount++;
        }

    }

    /**
     * @Desc 大部分连接不上主
     */
    private static void forceMaster() {
        int item = 0;
        for (Server server : SLAVE_list) {
            if (server.getState() != 1) { // 与主连接异常
                item++;
            }
        }
        if (SLAVE_list.size() / 2 < item) {  //更新主
//            updateMaster();
            MASTER= Vote.updateMaster(SLAVE_list, new CircleVoteRole());
        }

    }



    /**
     * @param obj 接收到的 Json 数据
     * @Desc 存入服务器列表数据
     */
    public static void parseJsonToObject(String obj) {
        SlaveService.SLAVE_list = new Gson().fromJson(obj, RequestData.class).getData();
    }

    /**
     * @desc
     */
    public static Server getmyserverinfo(){
        return myServer;
    }


    /**
     * @return
     * @desc 获取本机IP 地址
     */
    public static String getIp() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return  addr.getHostAddress();
//            return "10.203.10.26";
//            System.out.println("IP地址：" + addr.getHostAddress() + "，主机名：" + addr.getHostName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @desc 获取本机名称
     * @return
     */
    public static String getHostName() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return  addr.getHostName();
//            return "10.203.10.26";
//            System.out.println("IP地址：" + addr.getHostAddress() + "，主机名：" + addr.getHostName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * @Desc 获取自己服务器的信息
     */
    public static Server getMyServer() {
        return myServer;
    }
}
