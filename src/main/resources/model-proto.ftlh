syntax = "proto3";

package ${packageName};

option java_package = "${packageName}";
option java_outer_classname = "CommonModelProto";
option java_multiple_files = true;


<#list refModel as key,item>
<#if !isSkip(item.name) >
  message ${item.name}{
    <#list item.attrs as attr>
        <#if attr.repeat>repeated</#if> ${attr.typeForProto} ${attr.nameForProto} = ${attr?counter};
    </#list>
  }
</#if>
</#list>
