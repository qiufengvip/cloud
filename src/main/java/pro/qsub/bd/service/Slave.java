package pro.qsub.bd.service;


import org.springframework.stereotype.Component;
import pro.qsub.bd.entity.Server;
import pro.qsub.bd.utils.HttpRequest;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;

/**
 * @desc  从服务
 *        任务： 心跳  选主
 */

@Component
public class Slave {

    // 主 服务器
    private final Server MAIN_MASTER = new Server("10.203.10.26","","8080",1);


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
        myServer = new Server(getIp(),"1","8080",1);
    }


    //心跳   上qz
    public static void heartbeat(){
        //判断主服务器是不是自己

        String URL = "http://"+MASTER.getIp() +":"+ MASTER.getPort() +"/cloud_war_exploded/heartbeat";
        // 能不能发送请求
        if (myServer==null)
            return;

        HashMap<String, String> map = new HashMap<>();
        map.put("ip",myServer.getIp());
        map.put("port",myServer.getPort());
        map.put("name",myServer.getName());
        map.put("state","1");
        HttpRequest httpRequest = new HttpRequest( URL,"POST", map);

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
