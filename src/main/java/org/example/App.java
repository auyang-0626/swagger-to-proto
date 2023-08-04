package org.example;

import static org.example.model.DtoModel.parseRef;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.example.model.AttrModel;
import org.example.model.DtoModel;
import org.example.model.GenModel;
import org.example.model.GenParam;
import org.example.model.MethodModel;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.base.CaseFormat;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException, InterruptedException, TemplateException {


        GenParam param = GenParam.builder()
                // swagger的接口地址
                .swaggerUrl("https://ad-crm-internal-cycle-operation.staging.kuaishou.com/rest/api/doc")
                // 要生成哪些接口，模糊匹配
                .pathFilter(List.of(
                        "/rest/crm/internal/cycle/operation/customer/customer/daily/view/list"
                ))
                /*.ignoreFilter(List.of(
                        "/rest/crm/internal/cycle/operation/common/tip/",
                        "/rest/crm/internal/cycle/operation/common/mail/send",
                        "/rest/crm/internal/cycle/operation/common/pdf/download"
                ))*/
                // 跳过哪些类
                .skipType(List.of("PageInfo"))
                // 生成的文件，输出的目录地址
                .outPath("/Users/yang/IdeaProjects/kuaishou-ad-infra-customer-assets-service/ad-infra-customer-assets-service-client/src/main/proto")
                // 生成的class名称
                .className("CustomerAssetsOplog")
                // 包名称
                .packageName("com.kuaishou.ad.infra.customer.assets")
                // 转换
                .convertPackage("com.kuaishou.ad.infra.customer.assets.service.convert")
                .build();

        GenModel genModel = parse(param);
        System.out.println("genModel:" + JSON.toJSONString(genModel));

        generateProto(genModel);

    }


    private static void generateProto(GenModel genModel) throws IOException, TemplateException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassLoaderForTemplateLoading(App.class.getClassLoader(), "");
        cfg.setDefaultEncoding("UTF-8");

        Template temp = cfg.getTemplate("proto.ftlh");

        Writer out = new FileWriter(String.format("%s/%s", genModel.getOutDir(), CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, genModel.getClassName()) + ".proto"));
        temp.process(genModel, out);

        for (DtoModel value : genModel.getRefModel().values()) {
            generateConvert(cfg, genModel, value);
        }
    }

    private static void generateConvert(Configuration cfg, GenModel genModel, DtoModel value) throws IOException, TemplateException {

        Map<String, Object> root = new HashMap<>();
        root.put("r", genModel);
        root.put("m", value);

        Template temp = cfg.getTemplate("convert.ftlh");

        Writer out = new FileWriter(String.format("%s/%s", genModel.getOutDir(), value.getName() + "Convert.java"));
        temp.process(root, out);
    }

    public static GenModel parse(GenParam param) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(20))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(param.getSwaggerUrl()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String swaggerJson = response.body();
        JSONObject swaggerInfo = JSONObject.parse(swaggerJson);
        Map<String, JSONObject> definitions = new HashMap<>();
        swaggerInfo.getObject("definitions", new TypeReference<Map<String, JSONObject>>() {
        }).forEach((k, v) -> definitions.put(renameType(k), v));

        Map<String, DtoModel> refModel = new HashMap<>();
        List<MethodModel> methodModels = new ArrayList<>();

        Map<String, JSONObject> pathMap = swaggerInfo.getObject("paths", new TypeReference<>() {
        });
        List<Map.Entry<String, JSONObject>> paths = pathMap.entrySet().stream()
                .filter(e ->
                        CollectionUtils.isEmpty(param.getPathFilter())
                                || param.getPathFilter().stream().anyMatch(path -> e.getKey().contains(path)))
                .filter(e -> CollectionUtils.isEmpty(param.getIgnoreFilter())
                        || param.getIgnoreFilter().stream().noneMatch(path -> e.getKey().contains(path)))
                .collect(Collectors.toList());

        for (Map.Entry<String, JSONObject> entry : paths) {
            System.out.println("处理接口:" + entry.getKey());

            Map<String, JSONObject> value = entry.getValue().to(new TypeReference<>() {
            });
            value.values().stream().findFirst().ifPresent(methodJson -> {
                MethodModel.MethodModelBuilder builder = MethodModel.builder();
                String methodName = StringUtils.capitalize(methodJson.getString("operationId").split("Using")[0]);
                builder.name(methodName)
                        .desc(methodJson.getString("summary"));

                handleParams(definitions, refModel, methodJson, builder, methodName);

                String respRef = methodJson.getJSONObject("responses")
                        .getJSONObject("200")
                        .getJSONObject("schema")
                        .getString("$ref");
                builder.respName(parseRef(refModel, definitions, respRef));

                methodModels.add(builder.build());
            });
        }

        return GenModel.builder()
                .packageName(param.getPackageName())
                .className(param.getClassName())
                .outDir(param.getOutPath())
                .refModel(refModel)
                .methodModels(methodModels)
                .convertPackage(param.getConvertPackage())
                .skipType(param.getSkipType())
                .build();
    }


    public static String renameType(String value) {
        return Arrays.stream(value.split("(«|»)"))
                .map(StringUtils::capitalize).collect(Collectors.joining());
    }

    private static void handleParams(Map<String, JSONObject> definitions,
                                     Map<String, DtoModel> refModel, JSONObject methodJson,
                                     MethodModel.MethodModelBuilder builder, String methodName) {
        JSONArray params = methodJson.getJSONArray("parameters");
        if (params == null || params.size() == 0) {
            builder.paramName("google.protobuf.Empty");
        } else if (params.size() == 1) {
            String paramName = DtoModel.parse(params.getJSONObject(0), refModel, definitions);
            if (StringUtils.isBlank(paramName)) {
                throw new RuntimeException("解析参数名称失败！");
            }
            builder.paramName(paramName);
        } else {
            String paramName = methodName + "Req";


            List<AttrModel> attrs = params.stream().map(item -> (JSONObject) item).map(item -> {

                return AttrModel.builder()
                        .type(DtoModel.parse(item, refModel, definitions))
                        .name(item.getString("name"))
                        .desc(item.getString("description"))
                        .repeat(false).build();
            }).collect(Collectors.toList());

            DtoModel dtoModel = DtoModel.builder()
                    .name(paramName)
                    .attrs(attrs)
                    .build();
            refModel.put(paramName, dtoModel);

            builder.paramName(paramName);
        }
    }
}
