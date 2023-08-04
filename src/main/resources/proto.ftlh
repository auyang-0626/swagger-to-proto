syntax = "proto3";

package ${packageName};

option java_package = "${packageName}";
option java_outer_classname = "${className}Proto";
option java_multiple_files = true;

import "google/protobuf/empty.proto";



<#list refModel as key,item>
<#if !isSkip(item.name) >
  message ${item.name}{
    <#list item.attrs as attr>
        <#if attr.repeat>repeated</#if> ${attr.typeForProto} ${attr.nameForProto} = ${attr?counter};
    </#list>
  }
</#if>
</#list>


service ${className}RpcService {
<#list methodModels as method>
    /**
    * ${method.desc}
    */
    rpc ${method.name}(${method.paramName}) returns (${method.respName});
</#list>
}