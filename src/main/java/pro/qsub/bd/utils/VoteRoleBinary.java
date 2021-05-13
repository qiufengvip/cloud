package pro.qsub.bd.utils;

import pro.qsub.bd.entity.Server;
import pro.qsub.bd.service.VoteRole;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JZH
 * @desc 选举规则-二进制算法
 * @time 2021-05-12-20:11
 */
public class VoteRoleBinary implements VoteRole {


    /**
     * 二进制算法
     * @param workingServes
     * @return
     */
    @Override
    public Server getMasterServer(List<Server> workingServes) {
        //最终asc大小
        int endName = 0;
        Server server = null;
        //遍历服务器集合
        for (Server workingServe : workingServes) {
            //获取服务器名称
            String name = workingServe.getName();
            //字符asc相加
            int ascLen = 0;
            for(int i = 0 ; i<name.length() ; i++){
                char temp = name.charAt(i);
                ascLen += Integer.valueOf(temp);
            }
            //判断后更改
            if(ascLen > endName){
                endName = ascLen;
                server = workingServe;
            }
        }
        return server;
    }

}
