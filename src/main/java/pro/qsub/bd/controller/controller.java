package pro.qsub.bd.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import pro.qsub.bd.entity.Server;
import pro.qsub.bd.service.Master;

import java.util.Map;

@Controller
@ResponseBody
@CrossOrigin("*")
public class controller {


    /**
     *
     * @desc  服务器 接受心跳
     * @param server
     * @return
     */
    @RequestMapping("server")
    public Map<String, Object> heartbeat(Server server){
        return Master.acceptHeartbeat(server);
    }
}
