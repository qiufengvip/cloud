package pro.qsub.bd.utils;

import pro.qsub.bd.entity.Server;
import pro.qsub.bd.service.VoteRole;

import java.util.List;

public class CircleVoteRole implements VoteRole {


    @Override
    public Server getMasterServer(List<Server> workingServes) {
        return null;
    }
}
