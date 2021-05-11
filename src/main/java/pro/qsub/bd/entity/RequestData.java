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
    private Integer code;
    private String msg;
    private List<Server> data;
}
