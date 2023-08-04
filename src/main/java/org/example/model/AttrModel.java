package org.example.model;

import static org.example.model.DtoModel.parseRef;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson2.JSONObject;
import com.google.common.base.CaseFormat;

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
public class AttrModel {

    private static final Map<String, String> typeMap = Map.of(
            "integer", "int64",
            "long", "int64",
            "boolean", "bool"
    );

    private static final Map<String, String> javaTypeMap = Map.of(
            "integer", "Long",
            "long", "Long",
            "boolean", "Boolean",
            "string", "String"
    );

    private String type;

    private boolean repeat;

    private String name;

    private String desc;

    public String getTypeForProto() {
        return typeMap.getOrDefault(type, type);
    }

    public String getTypeForConvert() {
        return javaTypeMap.getOrDefault(type, type);
    }

    public String getNameForProto() {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
    }

    public String getNameForConvert() {
        return StringUtils.capitalize(name);
    }

    public static AttrModel parse(String k, JSONObject attr,
                                  Map<String, DtoModel> refModel,
                                  Map<String, JSONObject> definitions) {

        String type = attr.getString("type");
        boolean repeat = false;

        if (StringUtils.equals(type, "array")) {
            repeat = true;
            JSONObject items = attr.getJSONObject("items");
            type = items.getString("type");
            if (StringUtils.isBlank(type)) {
                type = parseRef(refModel, definitions, items.getString("$ref"));
            }
        }

        String ref = attr.getString("$ref");
        if (StringUtils.isNotBlank(ref)) {
            type = parseRef(refModel, definitions, ref);
        }
        return AttrModel.builder()
                .type(type)
                .name(k)
                .repeat(repeat)
                .desc(attr.getString("description"))
                .build();
    }
}
