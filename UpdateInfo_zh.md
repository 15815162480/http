### 1.6.0
* 优化: 将设置移动到一个新的标签页面
* 新功能: 新设置-可以添加一个自定义 controller 注解(设置好不要移动该注解)

### 1.5.6

* 新功能: 支持 241.*
* 新功能: 新设置-选择是否开启 SearchEverywhere 功能(因为 IDEA IU 2024.1 支持端点搜索了)

### 1.5.5

* bug 修复: 请求头表格添加时如果值在编辑后为空会删除该行

### 1.5.4

* bug 修复: 请求头表格在生成数据后，点击"+"未填写数据失焦后空白行不会自动删除
* 优化: 请求历史面板固定编号和请求方式两列的宽度, 并可以调整剩余两列的宽度
* 优化: 查看请求历史-响应体添加响应体类型选择
* 优化: 查看请求历史-移除所有按钮
* bug 修复: IDEA 新UI紧凑模式下, 切换至请求体或响应体标签页时, 窗口抖动(老UI和非紧凑模式下不会出现), 整个窗口左边框多出1px

### 1.5.3

* bug 修复: 请求历史记录生成请求报错——File must be not null

### 1.5.2

* bug 修复: 装订区方法处 alt + Enter 文案错误
* bug 修复: 装订区方法处点击，如果窗口面板标签没有选中 api 标签, 生成时不会自动切换到 api 标签页面
* 新功能: 请求历史(最多 50 条), 可以重新生成选中的请求

### 1.5.1

* 优化: 添加 SearchEverywhere ApiTool 面板快捷键(ctrl \\)。如果剪切板中的内容以 "/" 开头, 会自动将内容填充到输入框中
* 优化: 添加 ApiTool 窗口快捷键(ctrl shift q)

### 1.5.0

* 优化: 如果字段上有 @JsonProperty 修饰, 会将对应的字段自动转成 @JsonProperty value 属性对应的值(
  不对请求类型进行分类处理)
* bug 修复: 修复一系列报错 Read access is allowed from inside read-action
* 优化: 可以将路径只有一个字符串常量引用转成对应的值。案例:
   ```java
   private static final String A = "/test";
   
   @GetMapping(A)
   public String testGet() {
       return "test";
   }
   ```
* 优化: 支持 @RequestMapping method 属性,
  例如:`@RequestMapping(value = "/test", method = {RequestMethod.PUT, RequestMethod.DELETE})`, 将是一个 Put 的请求

### 1.4.10

* 优化: 移除未使用的代码
* bug 修复: 快速切换选中方法项时报错
* 求助: IDEA 新UI紧凑模式下, 切换至请求体或响应体标签页时, 窗口抖动(老UI和非紧凑模式下不会出现), 整个窗口左边框多出1px

### 1.4.9

* 新功能: 支持 233.*

### 1.4.8

* 新功能: 新设置-可以设置是否在切换分支时刷新(由于不同分支可能存在不同的接口)

### 1.4.7

* 优化: 选中环境不允许删除
* bug 修复: 移除 Feign 相关接口的装订区域

### 1.4.6

* 优化: 添加请求成功耗时统计

### 1.4.5

* bug 修复: Controller 上没有请求路径时, 路径生成错误
* 优化: 放开发起请求的条件: 必须选择节点(现可向第三方发起请求)

### 1.4.4

* 优化: 如果不链接根模块(即项目没有引入, 如 maven 不引用根模块或 pom 中的 artifactId 跟项目名不一致),
  会以项目名称作为根目录, 但不会扫描接口(接口都是在模块中, 需要引入)

### 1.4.3

* bug 修复: 特定情况下请求路径生成错误
* 新功能: 支持上传文件, 只支持单个参数`void a(MultipartFile[] file)`或 `void a(MultipartFile file)`,
  不支持`void a(MultipartFile[] file, MultipartFile[] file2)`等两个参数的文件上传方法(没找到合适的UI进行展示)

### 1.4.2

* bug 修复: Collection/数组数据生成错误
* bug 修复: Spring mvc 参数类型: ModelMap 和 ModelAndView 生成错误数据
* bug 修复: 新编写的请求方法点击左边的图标没有生成对应的数据

### 1.4.1

* 优化: 对枚举参数进行特殊处理，只显示枚举值
* bug 修复: 编辑请求头数据功能异常
* bug 修复: 装订图标区域设置是否显示, 图标丢失
* bug 修复: 选中环境配置在编辑完成时, 地址栏、请求头不会随之切换

### 1.4.0

* 新功能: 往 Search Everywhere(随处搜索, 双击 Shift)添加 ApiTool, 可根据 / 分割对接口进行匹配
* 新功能: 添加一个图标在请求方法的左边(IDEA官方称为装订区域), 点击生成对应的请求和将树展开到对应的结点
* 新功能: 请求头和请求参数能以一个 properties 文件的形式进行编辑
* bug 修复: 切换主题时窗口主题没有同步刷新
* 优化: 适配浅色模式(以新UI为准)
* bug 修复: 选中节点时，切换环境, 请求头没有发生变化
* bug 修复: Api 标签页面添加环境功能异常

### 1.3.0

* bug 修复: 刷新没有清空请求头、参数、请求体、返回体
* 优化: 请求体支持泛型参数(不要互相嵌套参数, 会造成 Stack Overflow)
* bug 修复: java.lang.Boolean 生成类型错误

### 1.2.3

* 优化: 移动"环境列表"到一个新的标签页面

### 1.2.2

* 新功能: 能够设置是否生成默认环境并删除生成的默认环境
* 优化: 修改没有 server.port 配置时的默认端口为 8080
* 优化: 节点过滤功能整合
* bug修复: 导出 postman 数据编码问题
* bug修复: 导出 postman 数据请求协议、请求地址、请求路径丢失
* 优化: 将窗口名修改为 ApiTool

### 1.2.1

* 新功能: 能够选择是否展示包结点或类结点
* 新功能: 响应体能在一个新的对话框中查看, 但修改不会回显到原位置
* bug 修复: 移除请求头禁止重复
* bug 修复: 没有选中节点仍然能够发起请求

### 1.2.0

* 新功能: 支持导出 Postman 环境配置、分模块 API 列表导出
* 新功能: 展开、收起功能优化, 如果有选中节点, 则只会展开/收起选中的节点
* 新功能: 模块结点、包结点、类结点添加右键菜单功能: 展开所有子节点
* bug 修复: 发送一个新请求时, 旧请求的结果没有被清空

### 1.1.0

* 新功能: 请求方式过滤
* 新功能: 方法结点添加右键菜单: 跳转到方法、复制完整路径、复制 API
* 新功能: 请求体内容能打开在一个新的对话框中编辑
* bug 修复: 切换环境时地址栏、请求头不会随之切换
* bug 修复: 选中环境配置在编辑完成时, 地址栏、请求头不会随之切换

### 1.0.2

* bug 修复: 包和类处于同一级别，类节点丢失
* bug 修复: 选择一个新节点, 旧节点的请求结果没有清除

### 1.0.1

* 更改插件名为 ApiTool

### 1.0.0

* 读取 Spring MVC 中所有注解请求接口方法
* 多环境配置, 默认读取每个模块的 context-path 和 port 并为每个模块生成本地环境(删除后刷新仍会生效)
* 如果项目引用了 Swagger2 和 Swagger3 注解, 将鼠标悬浮在对应的结点上会显示对应的说明
* 双击方法结点可以跳转到指定方法处
