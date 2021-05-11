package pro.qsub.bd.entity;


import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @desc  服务器信息 实体类
 */


@AllArgsConstructor
@Data
public class Server {
    //ip
    private  String ip;
    //name
    private  String name;
    //端口
    private  String port;
    // 与主服务连接 的状态
    private int state;


}
