package org.example.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 杨光跃 <yangguangyue@kuaishou.com>
 * Created on 2023-08-02
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GenParam {

    // swagger接口的地址，不是页面地址
    private String swaggerUrl;
    // 过滤出符合条件的路径
    private List<String> pathFilter;
    // 排出符合条件的路径
    private List<String> ignoreFilter;
    // 输出的文件存放路径
    private String outPath;

    private String packageName;

    private String className;

    private String convertPackage;


    private List<String> skipType;
}
