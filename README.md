### 主要功能
========
* 实现灵活的增删改查操作，一般实体只需定义属性，即可完成增删改查操作
* 基于简单注解，定义查询条件，无需撰写SQL
* 基于简单注解，可完成关联对象的查询。对于复杂逻辑，也支持通过SQL获取关联对象。
* 支持JSR303校验数据
* 拥抱变化，表结构发生改变时，仅需修改实体类、DTO对象即可快速完成后台业务逻辑修改

### 主要依赖
========
* springmvc
* mybatis-plus

### 设计要求
* 表设计时必须设置主键，且主键为单个字段。
* 子表中对应主表主键的字段名必须与主表中的名称保持一致
* 遵循mybatis-plus的规则，定义service,mapper和entity
* 定义dto类负责响应HttpRequest数据
* 使用注解DtoRelation维护dto,entity,service的关系(可加在DTO或者ENTITY上)
* 定义Controller继承IControllerAdapter,根据实际需要调用IControllerAdapter定义的方法

### 快速开始
--------
#### 引入依赖
```xml
        <dependency>
            <groupId>com.circustar</groupId>
            <artifactId>mybatis-plus-mvc-enhance</artifactId>
            <version>1.0.0</version>
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
#### 定义mybatis-plus的entity,mapper和service(可使用mybatis-plus的code generator自动生成)
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Student implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer studentId;
    private String name;
    private Integer grade;

    @TableLogic
    private Integer deleted;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
public interface  StudentMapper extends MybatisPlusMapper<Student> {
}
public interface IStudentService extends IService<Student> {
}
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements IStudentService {
}
```
#### 定义dto类,并加入DtoEntityRelation注解，维护dto与entity以及service的关系
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = Student.class, service = IStudentService.class)
public class StudentDto {
    private Integer studentId;

    private String name;

    private Integer grade;

    private Integer version;

    private List<ScoreDto> scoreList;
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

    /*
     *** 通过ID获取实体类，转换成dto后返回
     *** 指定sub_entities参数可返回关联的子项(多个子项已逗号分隔)
     */
    @GetMapping("/entity/{dto_name}/{id}")
    public IServiceResult testGetById(@PathVariable("dto_name") String dto_name
            , @PathVariable("id") String id
            , @RequestParam(value = "sub_entities", required = false) String sub_entities) throws Exception  {
        return getById(dto_name , id, sub_entities);
    }

    /*
     *** QueryFieldModel作为查询条件，查询数据并转化dto列表后返回
     *** page_index、page_size指定分页信息
     */
    @PostMapping("/query_field/entities/{dto_name}")
    public IServiceResult testGetPages(@PathVariable("dto_name") String dto_name
            , @RequestParam(value = "page_index", required = false) Integer pageIndex
            , @RequestParam(value = "page_size", required = false) Integer pageSize
            , @RequestBody List<QueryFieldModel> queryFiledModelList) throws Exception {
        return getPagesByQueryFields(dto_name , pageIndex, pageSize, queryFiledModelList);
    }

    /*
     *** 读取dto中QueryField注解信息，组装成查询条件后查询实体列表，转化dto列表后返回
     *** page_index、page_size指定分页信息
     */
    @PostMapping("/annotation/entities/{dto_name}")
    public IServiceResult testGetPages(@PathVariable("dto_name") String dto_name
            , @RequestParam(value = "page_index", required = false) Integer pageIndex
            , @RequestParam(value = "page_size", required = false) Integer pageSize
            , @RequestBody(required = true) Map map) throws Exception {
        return getPagesByDtoAnnotation(dto_name , pageIndex, pageSize, map);
    }

    /*
     *** 通过ID删除指定实体
     *** 如果在mybatis-plus开启逻辑删除后，仍要执行物理删除，可将physic_delete置为true
     */
    @DeleteMapping("/entity/{dto_name}/{id}")
    public IServiceResult testDeleteById(@PathVariable("dto_name") String dto_name
            , @PathVariable("id") String id
            , @RequestParam(value = "sub_entities", required = false) String sub_entities
            , @RequestParam(value = "physic_delete", required = false) Boolean physic_delete) throws Exception {
        return deleteById( dto_name ,id, sub_entities, physic_delete== null?false:physic_delete);
    }

    /*
     *** 通过ID列表删除多个实体
     *** 如果在mybatis-plus开启逻辑删除后，仍要执行物理删除，可将physic_delete置为true
     */
    @DeleteMapping("/entities/{dto_name}")
    public IServiceResult removeByIds(@PathVariable("dto_name") String dto_name
            , @RequestBody List<Serializable> ids
            , @RequestParam(value = "sub_entities", required = false) String sub_entities
            , @RequestParam(value = "physic_delete", required = false) Boolean physic_delete) throws Exception  {
        return deleteByIds( dto_name ,ids, sub_entities, physic_delete== null?false:physic_delete);
    }

    /*
     *** 保存一个实体，可保存关联的子项（通过sub_entities设置）
     */
    @PostMapping("/entity/{dto_name}")
    public IServiceResult testSave(@PathVariable("dto_name") String dto_name
            , @RequestBody Map map
            , @RequestParam(value = "sub_entities", required = false) String sub_entities) throws Exception {
        return save(dto_name , map, sub_entities);
    }

    /*
     *** 修改一个实体
     *** 指定sub_entities，可将关联的子项实体删除并重新插入新的记录
     */
    @PostMapping("/entity/{dto_name}/{id}")
    public IServiceResult testUpdate(@PathVariable("dto_name") String dto_name
            , @PathVariable("id") Serializable id
            , @RequestBody Map map
            , @RequestParam(value = "sub_entities", required = false)String subEntities
            , @RequestParam(value = "remove_and_insert", required = false) Boolean remove_and_insert
            , @RequestParam(value = "physic_delete", required = false) Boolean physic_delete) throws Exception {
        return update(dto_name , id, map, subEntities
                , remove_and_insert == null?false:remove_and_insert
                , physic_delete== null?false:physic_delete);
    }

    /*
     *** 新增、修改或删除多个实体
     *** physicDelete为true时物理删除
     */
    @PostMapping("/entities/{dto_name}")
    public IServiceResult testuSaveOrUpdateOrDeleteList(@PathVariable("dto_name") String dto_name
            , @RequestBody List<Map> mapList
            , @RequestParam(value = "physic_delete", required = false) Boolean physicDelete) throws Exception {
        return saveOrUpdateOrDeleteList(dto_name
                , mapList
                , physicDelete);
    }

    /*
     *** 只更新一个实体（通过id指定）的级联对象updateSubEntityList
     *** subEntityName指定更新级联对象
     *** subEntityRemoveAndInsert为true时级联对象先删除再插入
     *** subEntityPhysicDelete为true时物理删除
     */
    @PostMapping("/entity/{dto_name}/{id}/subentity")
    public IServiceResult testuUdateSubEntities(@PathVariable("dto_name") String dto_name
            , @PathVariable("id") Serializable id
            , @RequestBody List<Map> mapList
            , @RequestParam(value = "subEntity", required = false)String subEntity
            , @RequestParam(value = "remove_and_insert", required = false) Boolean remove_and_insert
            , @RequestParam(value = "physic_delete", required = false) Boolean physic_delete) throws Exception {
        return updateSubEntities(dto_name
                , id
                , mapList
                , subEntity
                , remove_and_insert
                , physic_delete);
    }

    /*
     *** 执行其他业务逻辑
     *** 需要预先实现IUpdateObjectProvider接口,并在DtoEntityRelation注解中设置
     *** 比如 1.预订定义TestUpdate类，实现IUpdateObjectProvider接口
     ***     2.定义TestDto类， 加入注解@DtoEntityRelation(entityClass=Test.class, updateObjectProvider=TestUpdate.class)
     ***     3.外部调用接口/business/TestDto时，根据IUpdateObjectProvider接口方法createUpdateEntities创建UpdateEntity类，并自动更新数据库
     */
    @PostMapping("/business/{dto_name}")
    public IServiceResult testbusinessUpdate(@PathVariable("dto_name") String dto_name
            , @RequestBody Map map) throws Exception {
        return businessUpdate(dto_name , map);
    }
}
```

### 效果演示
#### 通过ID查询(可关联查询子项信息)
请求：
响应：
#### 查询列表(仅主表信息)
##### 模式1：使用QueryFieldModel
请求：
响应：
##### 模式2：在DTO上中使用注解
请求：
响应：
#### 删除(可级联删除子项信息)
删除前数据：
请求：
删除后数据：
#### 删除列表(可级联删除子项信息)
删除前数据：
请求：
删除后数据：
#### 新增(可级联新增子项信息)
请求：
响应：
新增数据：
#### 更新()
请求：
响应：
### 其他功能
#### 级联获取
#### 级联更新
#### 多对多关系
### 注意点
### 待解决的问题
