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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @desc  从服务
 *        任务： 心跳  选主
 */

@Component
public class Slave {

    // 主 服务器
    private final Server MAIN_MASTER = new Server("10.203.10.26","","8080",1,0);



    // 当前 主服务器地址    选主完成后需要重新接收
    private static Server MASTER;

    //所有服务器列表《》
    private static List<Server> SLAVE_list;

    // 自己的信息
    private static Server myServer;

    //连接失败次数
    private static int  failed =0;


    //启动类
    @PostConstruct
    private void   starts(){
        // 开机的 主服务器
        MASTER = MAIN_MASTER;
        myServer = new Server(getIp(),"1","8080",1,0);
    }


    //心跳   上qz
    public static void heartbeat(){
        //判断主服务器是不是自己
        if (myServer.getIp().equals(MASTER.getIp()) )
            return;
        String URL = "http://"+MASTER.getIp() +":"+ MASTER.getPort() +"/cloud_war_exploded/heartbeat";
        // 能不能发送请求
        if (myServer==null)
            return;

        HashMap<String, String> map = new HashMap<>();
        map.put("ip",myServer.getIp());
        map.put("port",myServer.getPort());
        map.put("name",myServer.getName());
        map.put("state","1");
        map.put("timestamp",System.currentTimeMillis()+"");

        try { // 主服务器异常
            if (Slave.failed<=3){
                HttpRequest httpRequest = new HttpRequest( URL,"POST", map);
                // 重连接成功 失败次数 == 0
                Slave.failed=0;
                // 存入服务穷链表
                parseJsonToObject(httpRequest.getData());
                // 主没有死 但是大部分从连接不上主
                forceMaster();
            }else {
                // 与主连接失败3次,找别的从 执行策略
                updateMaster();
            }

        }catch (Exception e){
            System.out.println("请求不到主服务器了");
            Slave.failed+=1;
        }

    }

    /**
     * @Desc 大部分连接不上主
     */
    private static void forceMaster() {
        int item = 0;
        for (Server server : SLAVE_list) {
            if (server.getState()!=1){ // 与主连接异常
                item++;
            }
        }
        if (SLAVE_list.size()/2<item){  //更新主
            updateMaster();
        }

    }

    /**
     * @Desc 跟换主服务于器策略
     */
    private static void updateMaster() {

        //正常的服务器
        List<Server> workingServes = new ArrayList<>();

        // 遍历所有的服务器
        for (Server server : SLAVE_list) {
            // 请求的url
            String URL = "http://"+server.getIp() +":"+ MASTER.getPort() +"/cloud_war_exploded/heartbeat";
            try{
                // 给从服务发送请求
                HttpRequest httpRequest = new HttpRequest( URL,"GET");
                //解析返回数据 并加入正常的list中
                workingServes.add(new Gson().fromJson(httpRequest.getData(), Server.class));

            }catch (Exception e){
                e.printStackTrace();
            }

        }



        int item = 0;
        Server server = myServer; // 选出来的主
        // 选主策略
        for (Server workingServe : workingServes) {
            String sal = "";
            int i = workingServe.getIp().lastIndexOf('.');
            if (i > 0) {
                sal = workingServe.getIp().substring(i+1).trim();
            }
            int anInt = Integer.parseInt(sal);
            if (anInt>item) item=anInt;
            server =workingServe;
        }
        // 修改主服务器
        MASTER = server;
    }



    /**
     * @Desc 存入服务器列表数据
     * @param obj 接收到的 Json 数据
     */
    public static void parseJsonToObject(String obj){
        Slave.SLAVE_list= new Gson().fromJson(obj,RequestData.class).getData();
    }



    /**
     * @desc  获取本机IP 地址
     * @return
     */
    public static String getIp(){
        try {
//            InetAddress addr = InetAddress.getLocalHost();
//            return  addr.getHostAddress();
              return "10.203.15.216";
//            System.out.println("IP地址：" + addr.getHostAddress() + "，主机名：" + addr.getHostName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

























}
