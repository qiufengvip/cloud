package pro.qsub.bd.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import pro.qsub.bd.entity.Server;
import pro.qsub.bd.service.MasterService;
import pro.qsub.bd.service.SlaveService;

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
    @RequestMapping("/heartbeat")
    public Map<String, Object> heartbeat(Server server){
        return MasterService.acceptHeartbeat(server);
    }

    @RequestMapping("/getserverlist")
    public Map<String, Object> getServerList(){
        return MasterService.getServerList();
    }


    /**
     * @desc 获取自身信息
     * @return
     */
    @RequestMapping("/getmyserverinfo")
    public Server getmyserverinfo(){
        return SlaveService.getmyserverinfo();
    }


    /**
     * @desc 获取主服务器信息
     * @return
     */
    @RequestMapping("/getmymasterserverinfo")
    public Server getmymasterserverinfo(){
        return SlaveService.getmymasterserverinfo();
    }

    /**
     * @desc 获取log日志
     * @return
     */
    @RequestMapping("/getlog")
    public Map<String, Object> getlog(){
        return SlaveService.getlog();
    }




}
