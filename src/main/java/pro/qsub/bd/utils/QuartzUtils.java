package pro.qsub.bd.utils;

import pro.qsub.bd.service.Slave;

/**
 * @DOC
 * @Author tran
 * @Date 2021/5/11 23:26
 */
public class QuartzUtils {

    /**
     * @Desc 心跳类
     */
    public  void  execute(){
        Slave.heartbeat();  //发送心跳
    }

}
