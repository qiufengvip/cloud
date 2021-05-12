package pro.qsub.bd.service;

import pro.qsub.bd.entity.Server;

import java.util.List;

/**
 * @author 梦伴
 * @desc VoteRole 选举规则 接口
 * @time 2021-05-12-19:29
 */
public interface VoteRole {

    /**
     * 选举规则
     * @param workingServes
     * @return
     */
    Server getMasterServer(List<Server> workingServes);
}

