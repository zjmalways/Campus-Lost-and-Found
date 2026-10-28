# 物品状态更新异常问题总结（STAR）

## S — Situation（情境）

将物品状态修改为“已找回/已归还”时，前端提示请求失败。Network 面板显示 HTTP 请求返回 `200`，但响应体为：

```json
{"code":500,"message":null,"data":null}
```

后端日志显示 JWT 校验成功，状态更新服务开启事务后回滚。调试确认自动填充切面正常执行，但服务方法没有执行到 `return true`。

## T — Task（任务）

定位状态更新失败的实际原因，并以最小改动修复，使 MyBatis-Plus 能正常执行物品更新，同时保留现有 `updateTime` 自动填充逻辑。

## A — Action（行动）

查看全局异常处理器捕获的异常及完整堆栈后，发现 `MyBatisSystemException` 包装的底层异常为：

```text
org.apache.ibatis.reflection.ReflectionException:
There is no getter for property named 'et' in 'class com.zhangjiaming.entity.Item'
```

原因是 `ItemMapper` 重写了 MyBatis-Plus 的 `updateById` 方法，但参数没有标注 MyBatis-Plus 动态 SQL 约定使用的实体参数名。动态 SQL 尝试读取 `et` 时，实际拿到的是 `Item` 对象，因而找不到 `et` 属性。

最小修复是在 `updateById` 参数上添加 `@Param(Constants.ENTITY)`：

```java
int updateById(@Param(Constants.ENTITY) Item entity);
```

该修改位于 [ItemMapper.java](./compus-lost-find-main/src/main/java/com/zhangjiaming/mapper/ItemMapper.java)，不需要改动 AOP 自动填充或状态更新业务逻辑。

## R — Result（结果）

MyBatis-Plus 动态 SQL 现在可以通过约定参数 `et` 访问待更新的 `Item` 实体，避免因参数名不匹配导致的 `ReflectionException`。异常不再向外抛出后，状态更新事务也不会因该问题回滚。

运行以下命令完成编译验证，构建成功：

```text
mvn -pl compus-lost-find-main -am -DskipTests compile
```

编译验证通过；实际数据库更新需在重新启动后端后通过状态更新接口确认。
