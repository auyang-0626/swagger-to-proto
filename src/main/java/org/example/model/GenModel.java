package org.example.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 杨光跃 <yangguangyue@kuaishou.com>
 * Created on 2023-08-03
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GenModel {

    // 包名称
    private String packageName;

    private Map<String, DtoModel> refModel;
    private List<MethodModel> methodModels;

    private String outDir;

    private String convertPackage;

    private List<String> skipType;

    public boolean isRef(String type) {
        return refModel != null && this.refModel.containsKey(type);
    }

    public boolean isSkip(String type) {
        return skipType != null && skipType.contains(type);
    }
}
