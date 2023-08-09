# swagger-to-proto
在main函数里面，修改参数，直接运行就可以；



## 生成的文件
    1. common_model.proto 所有的入参出参定义
    2. 每个controller 下的接口，会生成对应的 xxx_service文件，自动去掉 ·-controller·

## 注意点
    1. 因为swagger数值类型不区分 long 和int,所以统一处理为了 Long类型，
    大部分情况是没关系的；但是也可以自己手动改成 int32类型，比如PageInfo的字段
    
    2. 不一定兼容所有swagger场景哈,生成proto文件后，运行maven 插件， 如果运行有报错，可以根据错误信息自行解决
