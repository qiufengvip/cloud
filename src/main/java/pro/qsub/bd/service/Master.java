package pro.qsub.bd.service;


import lombok.NonNull;
import org.springframework.stereotype.Component;
import pro.qsub.bd.entity.Server;
import pro.qsub.bd.utils.Putdata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @desc 主服务
 *        任务: 监控
 */

@Component
public class Master {

    //所有服务器列表《》
    private static  List<Server> SLAVE_list=new ArrayList<>();


    /**
     * @desc 接受心跳的
     */
    public static Map<String, Object> acceptHeartbeat(@NonNull Server server){
        //加入服务器列表
        SLAVE_list.add(server);

        //组合返回数据
        return Putdata.printf(0,"成功",SLAVE_list);
    }

    public static Map<String, Object> getServerList(){
        return Putdata.printf(0,"成功",SLAVE_list);
    }
}
