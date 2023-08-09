syntax = "proto3";

package ${packageName};

option java_package = "${packageName}";
option java_outer_classname = "${className}Proto";
option java_multiple_files = true;

<#list methodModels as method>
    <#if method.paramName == 'google.protobuf.Empty' || method.respName == 'google.protobuf.Empty'>
import "google/protobuf/empty.proto";
        <#break />
    </#if>
</#list>

import "common_model.proto";



service ${className}RpcService {
<#list methodModels as method>
    /**
    * ${method.desc}
    */
    rpc ${method.name}(${method.paramName}) returns (${method.respName});
</#list>
}