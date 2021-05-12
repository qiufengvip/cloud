package pro.qsub.bd.service;


import lombok.NonNull;
import org.springframework.stereotype.Component;
import pro.qsub.bd.entity.Server;
import pro.qsub.bd.utils.Putdata;

import java.util.*;

/**
 * @desc 主服务
 *        任务: 监控
 */

@Component
public class MasterService {

    //所有服务器列表《》
    private static  Map<String, Server> SLAVE_list=new HashMap<>();


    /**
     * @desc 接受心跳的
     */
    public static Map<String, Object> acceptHeartbeat(Server server){
        //加入服务器列表
        SLAVE_list.put(server.getIp(),server);

        //组合返回数据
        return Putdata.printf(0,"成功",slave_listToList());
    }

    public static Map<String, Object> getServerList(){
        return Putdata.printf(0,"成功",slave_listToList());
    }


    /**
     * @desc 返回当前服务器列表的
     * @return
     */
    public static List<Server> slave_listToList(){
        ArrayList<Server> servers = new ArrayList<>();
        for ( Map.Entry<String, Server>entry:SLAVE_list.entrySet()) {
            if (System.currentTimeMillis() - entry.getValue().getTimestamp() > 15*1000 ){
                entry.getValue().setState(0);
            }
            servers.add(entry.getValue());
        }
        return servers;

    }
}
