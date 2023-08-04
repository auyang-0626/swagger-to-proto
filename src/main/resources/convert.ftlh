package ${r.convertPackage};

<#list m.attrs as attr>
    <#if attr.repeat>
import java.util.List;
        <#break>
    </#if>
</#list>

<#list m.getDependRef(r.refModel) as ref>
import ${r.packageName}.${ref};
</#list>

/**
* @author 杨光跃 <yangguangyue@kuaishou.com>
* Created on 2023-08-04
*/
public interface ${m.name}Convert {

<#list m.attrs as attr>
    <#if attr.repeat>List<${attr.typeForConvert}><#else>${attr.typeForConvert}</#if> get${attr.nameForConvert}();
</#list>

    static ${m.name} to${m.name}(${m.name}Convert model) {
        ${m.name}.Builder builder = ${m.name}.newBuilder();
        if (model != null) {
        <#list m.attrs as attr>
            if (model.get${attr.nameForConvert}() != null) {
            <#if attr.repeat>
                builder.addAll${attr.nameForConvert}(model.get${attr.nameForConvert}());
            <#else>
                builder.set${attr.nameForConvert}(model.get${attr.nameForConvert}());
            </#if>
            }
        </#list>
        }
        return builder.build();
    }
}
