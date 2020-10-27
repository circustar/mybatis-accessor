### 主要功能
========
* 实现灵活的增删改查操作，一般实体只需定义属性，即可完成增删改查操作
* 基于简单注解，定义查询条件，无需撰写SQL
* 基于简单注解，可完成关联对象的查询。对于复杂逻辑，也支持通过SQL获取关联对象。
* 支持JSR303校验数据
* 拥抱变化，表结构发生改变时，仅需修改实体类、DTO对象即可快速完成后台业务逻辑修改
* 

### 主要依赖
========
* spring mvc
* mybatis-plus


### 开始
--------
#### 引入依赖
```xml
        <dependency>
            <groupId>org.yxy.circustar</groupId>
            <artifactId>mvc</artifactId>
            <version>1.0.1-release</version>
        </dependency>
```
#### 使用EnableMvcEnhancement注解开启包扫描，并指定实体类，dto类位置
```java
    @SpringBootApplication
    @EnableOpenApi
    @MapperScan("com.example.demo1.mapper")
    @EnableMvcEnhancement(
            relationScan = @RelationScanPackages({"com.example.demo1.entity", "com.example.demo1.dto"})
    )
    public class WebTestApplication {
        public static void main(String[] args) {
            SpringApplication.run(WebTestApplication.class, args);
        }
    }
```
#### 定义mapper和service (mybatis-plus)
```java
public interface PrivilegeMapper extends BaseMapper<Privilege> {
}
public interface IPrivilegeService extends IService<Privilege> {
}
@Service
public class PrivilegeServiceImpl extends ServiceImpl<PrivilegeMapper, Privilege> implements IPrivilegeService {
}
```
#### 定义entity和dto,并在两者之一上加入DtoEntityRelation注解（dto与entity是一对多的关系）
```java
@DtoEntityRelation(entityClass = Privilege.class, service = IPrivilegeService.class)
public class PrivilegeDto {
    private Integer privilegeId;

    private String privilegeName;

    private Integer version;

}
public class Privilege implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer privilegeId;

    private String privilegeName;

    @TableLogic
    private Integer deleted;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
```
#### 定义controller，实现IControllerAdapter接口
```java
@RestController
@RequestMapping("/mvc_test")
public class TestController implements IControllerAdapter {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }

    @GetMapping("/entity/{dto_name}/{id}")
    public IServiceResult customGetById(@PathVariable("dto_name") String dto_name
            , @PathVariable("id") String id
            , @RequestParam(value = "sub_entities", required = false) String sub_entities)  {
        return getById(dto_name , id, sub_entities);
    }

    @PostMapping("/entities_fields/{dto_name}")
    public IServiceResult customGetPages(@PathVariable("dto_name") String dto_name
            , @RequestParam(value = "page_index", required = false) Integer pageIndex
            , @RequestParam(value = "page_size", required = false) Integer pageSize
            , @RequestBody List<QueryFieldModel> queryFiledModelList) {
        return getPagesByQueryFields(dto_name , pageIndex, pageSize, queryFiledModelList);
    }

    @PostMapping("/entities/{dto_name}")
    public IServiceResult customGetPages(@PathVariable("dto_name") String dto_name
            , @RequestParam(value = "page_index", required = false) Integer pageIndex
            , @RequestParam(value = "page_size", required = false) Integer pageSize
            , @RequestBody(required = true) Map map) {
        return getPagesByDtoAnnotation(dto_name , pageIndex, pageSize, map);
    }

    @DeleteMapping("/entity/{dto_name}/{id}")
    public IServiceResult customDeleteById(@PathVariable("dto_name") String dto_name
            , @PathVariable("id") String id
            , @RequestParam(value = "physic", required = false) Boolean physic) throws Exception {
        return deleteById( dto_name , id, physic);
    }

    @DeleteMapping("/entities/{dto_name}")
    public IServiceResult removeByIds(@PathVariable("dto_name") String dto_name
            , @RequestBody List<Serializable> ids
            , @RequestParam(value = "physic", required = false) Boolean physic)  {
        return deleteByIds( dto_name , ids, physic);
    }

    @PostMapping("/entity/{dto_name}")
    public IServiceResult customSaveEntity(@PathVariable("dto_name") String dto_name
            , @RequestBody Map map) {
        return saveEntity(dto_name , map);
    }

    @PostMapping("/entity/{dto_name}/{id}")
    public IServiceResult customUpdateEntity(@PathVariable("dto_name") String dto_name
            , @PathVariable("id") Serializable id
            , @RequestBody Map map
            , @RequestParam(value="sub_entities", required = false)String subEntities) {
        return updateEntity(dto_name , id, map, subEntities);
    }

    @PostMapping("/business/{dto_name}")
    public IServiceResult customUpdate(@PathVariable("dto_name") String dto_name
            , @RequestBody Map map) {
        return businessUpdate(dto_name , map);
    }
}
```

### 效果演示
#### 查询列表
#### 通过ID查询
#### 新增
#### 更新
#### 删除
### 其他功能
#### 级联获取
#### 级联更新
#### 多对多关系
### 目前无法解决的问题
