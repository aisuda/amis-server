# amis 后端功能辅助

目前实现了：

* amis 表单验证，用于实现和 amis 同样的后端验证，避免重复开发

## 使用方法

目前还没发布到 maven center，所以暂时需要通过拷贝源码的方式使用

```java
import com.baidu.amis.validation.Validator;

// 第一个参数是 amis schema，第二个参数是表单 name，第三个参数是提交数据的 json
// 为了方便查看，下面示例使用 java 15 的 text block
List<ConstraintViolation> violations=Validator.validate("""
{
  "type": "page",
  "body": {
    "type": "form",
    "api": "/api/mock2/form/saveForm",
    "name": "myForm",
    "body": [
      {
        "type": "input-text",
        "label": "文本",
        "name": "text",
        "validations": {
          "isNumeric": true
        },
        "validationErrors": {
          "isNumeric": "同学，请输入数字哈"
        }
      }
    ]
  }
}
""","myForm","""
{
  "text": "v"
}
""");

        // violations 的结果应该是
        violations[0].getMessage()=="同学，请输入数字哈"
```

## 开发

目前主要以库的形式对外提供，但还没有 maven 仓库，只能先拷贝代码使用

## 测试

因为需要构造大量 JSON 进行测试，而 Java 在 15 之前不支持多行字符串，构造起来麻烦，所以测试的代码是基于 kotlin，可以直接在 IDE 里运行单个测试，或者使用下面的命令运行所有测试：

```
./gradlew check
```
