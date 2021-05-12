package pro.qsub.bd.service;

import com.google.gson.Gson;
import pro.qsub.bd.entity.Server;
import pro.qsub.bd.utils.HttpRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * @desc 选举方法
 */
public class Vote {

    public static Server updateMaster(List<Server> SLAVE_list,VoteRole voteRole) {
        System.out.println("=================");
        System.out.println("||开始更换主服务器||");
        System.out.println("=================");
        //正常的服务器
        List<Server> workingServes = new ArrayList<>();
        if (SLAVE_list==null) SLAVE_list = new ArrayList<Server>();

        // 遍历所有的服务器
        for (Server server : SLAVE_list) {
            // 请求的url
            String URL = "http://" + server.getIp() + ":" + server.getPort() + "/cloud_war_exploded/getmyserverinfo";
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
        Server server = voteRole.getMasterServer(workingServes);

        System.out.println("********选举完毕（新主的信息）*********");
        System.out.println("||主服务器IP:"+ server.getIp()+"\t\t||");
        System.out.println("||主务器port:"+ server.getPort()+"\t\t||");
        System.out.println("*********************************");
        // 修改主服务器
        return server;
    }


}
