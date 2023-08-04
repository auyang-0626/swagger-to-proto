package org.example.model;

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
public class MethodModel {

    private String name;

    private String desc;

    private String paramName;

    private String respName;

}
