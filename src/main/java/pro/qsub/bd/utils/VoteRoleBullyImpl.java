package pro.qsub.bd.utils;

import pro.qsub.bd.entity.Server;
import pro.qsub.bd.service.SlaveService;
import pro.qsub.bd.service.VoteRole;

import java.util.ArrayList;
import java.util.List;

/**
 * @DOC 选举策略 霸道算法
 * @Author tran
 * @Date 2021/5/12 20:37
 */
public class VoteRoleBullyImpl implements VoteRole {

    /**
     * @Desc 选举策略
     */
    @Override
    public Server getMasterServer(List<Server> workingServes) {
        int item = 0;
        Server server = SlaveService.getMyServer(); // 选出来的主
        if (workingServes==null) workingServes = new ArrayList<>();
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
}
