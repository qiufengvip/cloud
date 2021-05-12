package pro.qsub.bd.service;


import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Component;
import pro.qsub.bd.entity.RequestData;
import pro.qsub.bd.entity.Server;
import pro.qsub.bd.utils.HttpRequest;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @desc 从服务
 * 任务： 心跳  选主
 */

@Component
public class SlaveService {

    // 主 服务器
    private final Server MAIN_MASTER = new Server("10.2.25.24", "", "8080", 1, 0);


    // 当前 主服务器地址    选主完成后需要重新接收
    private static Server MASTER;

    //所有服务器列表《》
    private static List<Server> SLAVE_list;

    // 自己的信息
    private static Server myServer;

    //连接失败次数
    private static int failed = 0;


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
                updateMaster();
            }else {
                SlaveService.failed += 1;
                System.err.println("==========");
                System.err.println("|| 与主服务器连接出现异常||");
                System.err.println("|| 异常次数为："+ SlaveService.failed +"\t||");
                System.err.println("==========");
            }

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
            updateMaster();
        }

    }

    /**
     * @Desc 更换主服务于器策略
     *        使用霸道选举算法
     */
    private static void updateMaster() {
        System.out.println("=================");
        System.out.println("||开始更换主服务器||");
        System.out.println("=================");
        //正常的服务器
        List<Server> workingServes = new ArrayList<>();
        if (SLAVE_list==null) SLAVE_list = new ArrayList<Server>();

        // 遍历所有的服务器
        for (Server server : SLAVE_list) {
            // 请求的url
            String URL = "http://" + server.getIp() + ":" + MASTER.getPort() + "/cloud_war_exploded/getmyserverinfo";
            try {
                // 给从服务发送请求
                HttpRequest httpRequest = new HttpRequest(URL, "GET");
                Server servesFromJson = new Gson().fromJson(httpRequest.getData(), Server.class);
                //解析返回数据 并加入正常的list中
                workingServes.add(servesFromJson);

                System.out.println("===与其他从服务器通讯===");
                System.out.println("||" +"通讯服务器信息"+ "||");
                System.out.println("||服务器IP:"+ servesFromJson.getIp()+"\t\t||");
                System.out.println("||服务器port:"+ servesFromJson.getPort()+"\t\t||");
                System.out.println("||服务器与主的状态:"+ servesFromJson.getState()+"\t\t(1=与主正常，0=与主断开)||");
                System.out.println("=====================");
            } catch (Exception e) {
//                e.printStackTrace();
                System.err.println("===与其他从服务器通讯===");
                System.err.println("||" +"通讯服务器信息"+ "||");
                System.err.println("||服务器IP:"+ server.getIp()+"\t\t||");
                System.err.println("||服务器port:"+ server.getPort()+"\t\t||");
                System.err.println("||服务器与主的状态:"+ server.getState()+"\t\t(1=与主正常，0=与主断开)||");
                System.err.println("=====================");
            }

        }

        /**
         * 选举规则使用
         */
        Server server = getServer(workingServes);


        // 修改主服务器
        MASTER = server;
        System.out.println("********选举完毕（新主的信息）*********");
        System.out.println("||主服务器IP:"+ MASTER.getIp()+"\t\t||");
        System.out.println("||主务器port:"+ MASTER.getPort()+"\t\t||");
        System.out.println("*********************************");
    }

    /**
     * 选举规则
     * @param workingServes
     * @return
     */
    private static Server getServer(List<Server> workingServes) {
        int item = 0;
        Server server = myServer; // 选出来的主
        // 选主策略
        for (Server workingServe : workingServes) {
            String sal = "";
            int i = workingServe.getIp().lastIndexOf('.');
            if (i > 0) {
                sal = workingServe.getIp().substring(i + 1).trim();
            }
            int anInt = Integer.parseInt(sal);
            if (anInt > item) item = anInt;
            server = workingServe;
        }
        return server;
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




}
