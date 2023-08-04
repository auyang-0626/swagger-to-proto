package org.example.model;

import static org.example.App.renameType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;

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
public class DtoModel {

    private String name;

    private List<AttrModel> attrs;

    public static String parse(JSONObject json, Map<String, DtoModel> refModel, Map<String, JSONObject> definitions) {

        String paramName = json.getString("name");
        JSONObject schema = json.getJSONObject("schema");
        if (schema != null) {
            String ref = schema.getString("$ref");
            if (StringUtils.isNotBlank(ref)) {
                return parseRef(refModel, definitions, ref);
            }
            String type = schema.getString("type");
            if (StringUtils.isNotBlank(type)) {
                return parseBaseParam(refModel, type, paramName);
            }
        } else {
            String type = json.getString("type");
            if (StringUtils.isNotBlank(type)) {
                return parseBaseParam(refModel, type, paramName);
            }
        }
        throw new RuntimeException("解析失败！");
    }

    public static String parseRef(Map<String, DtoModel> refModel, Map<String, JSONObject> definitions, String ref) {
        // 引用
        String[] arr = ref.split("/");
        String typeName = renameType(arr[arr.length - 1]);
        if (!refModel.containsKey(typeName)) {
            JSONObject jsonObject = definitions.get(typeName);
            parseDefinition(typeName, jsonObject, definitions, refModel);
        }
        return typeName;
    }

    private static String parseBaseParam(Map<String, DtoModel> refModel, String type, String paramName) {
        String paramTypeName = StringUtils.capitalize(paramName) + "Req";
        refModel.put(paramTypeName, DtoModel.builder().name(paramTypeName).attrs(List.of(
                AttrModel.builder()
                        .type(type)
                        .repeat(false)
                        .name(paramName)
                        .build()
        )).build());
        return paramTypeName;
    }

    private static void parseDefinition(String typeName, JSONObject jsonObject, Map<String, JSONObject> definitions, Map<String, DtoModel> refModel) {

        refModel.put(typeName, null);

        List<AttrModel> attrs = new ArrayList<>();
        Map<String, JSONObject> properties = jsonObject.getObject("properties", new TypeReference<Map<String, JSONObject>>() {
        });
        List<String> ignoreAttr = List.of("traceId", "port", "host", "timestamp", "status");
        if (properties != null && properties.size() > 0) {
            properties.forEach((k, attr) -> {
                if (typeName.startsWith("Message") && ignoreAttr.contains(k)) {
                    return;
                }
                if (typeName.startsWith("PageMessage") && ignoreAttr.contains(k)) {
                    return;
                }
                attrs.add(AttrModel.parse(k, attr, refModel, definitions));
            });
        }

        refModel.put(typeName, DtoModel.builder()
                .name(typeName)
                .attrs(attrs).build()
        );
    }

    public List<String> getDependRef(Map<String, DtoModel> refModel) {
        List<String> refs = new ArrayList<>();
        refs.add(name);
        refs.addAll(this.attrs.stream()
                .map(AttrModel::getTypeForConvert)
                .filter(refModel::containsKey)
                .collect(Collectors.toList()));

        return refs.stream().sorted().distinct().collect(Collectors.toList());
    }
}
