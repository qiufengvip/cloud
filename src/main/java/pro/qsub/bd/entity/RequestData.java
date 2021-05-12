package pro.qsub.bd.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Desc 心跳返回值解析类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestData {
    private Integer code;  // 状态
    private String msg;  // 信息
    private List<Server> data;  // 服务器信息
}
