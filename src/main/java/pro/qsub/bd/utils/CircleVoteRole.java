package pro.qsub.bd.utils;

import pro.qsub.bd.entity.Server;
import pro.qsub.bd.service.VoteRole;

import java.util.List;

/**
 * @desc  环选举算法 实现类  实现选举算法接口
 */
public class CircleVoteRole implements VoteRole {


    /**
     * @desc
     * @param workingServes
     * @return
     */
    @Override
    public Server getMasterServer(List<Server> workingServes) {
        return workingServes.get(0);
    }
}
