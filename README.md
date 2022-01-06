### 1.目标
* 简化数据库操作，不使用SQL的情况下完成CRUD操作
* 支持级联保存/更新/删除
* 预定义更新监听器，自动完成查找、统计、分配等功能

### 2.依赖
* mybatis-plus

### 3.设计要求
* 表必须设置主键，且主键自动生成（数据库自动生成或者使用mybatis-plus注解生成）
* 存在外键关系的表，外键的变量名与主键保持一致
* 遵循mybatis-plus的规则，定义service,mapper和entity
* 定义DTO并关联Entity和service

### 4.引入依赖
```xml
    <dependency>
        <groupId>com.circustar</groupId>
        <artifactId>mybatis-accessor</artifactId>
        <version>1.2.2</version>
    </dependency>
```
### 5.定义Mybatis-plus相关类
#### 5.1定义POJO实体类
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Student implements Serializable {
    //POJO
}
```

#### 5.2定义MAPPER(注意是CommonMapper)
```java
public interface  StudentMapper extends CommonMapper<Student> {
}
```

#### 5.3定义ISERVICE
```java
public interface IStudentService extends IService<Student> {
}
```

### 6.定义DTO并关联Entity和service, DtoEntityRelation注解可定义在DTO上或者ENTITY上
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = Student.class, service = IStudentService.class)
public class StudentDto {
    //POJO
}
```

### 7.开启包扫描，扫描CommonMapper类以及DtoEntityRelation注解
```java
@MapperScan("com.test.mybatis_accessor.mapper")
@EnableMybatisAccessor(
        relationScan = @RelationScanPackages({"com.test.mybatis_accessor.entity", "com.test.mybatis_accessor.dto"})
)
public class SpringBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebTestApplication.class, args);
    }
}
```

### 8.快速使用
#### 8.1.查询列表
* 定义DTO
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = Student.class, service = IStudentService.class)
public class StudentDto {
    private Long studentId;

    @QueryWhere(connector = Connector.likeRight)
    private String name;

    @QueryOrder(sortIndex = 1, sortOrder = QueryOrder.ORDER_DESC)
    private Integer grade;
}
```

* 使用
```java
StudentDto studentDto = new StudentDto();
studentDto.setName(studentName);
// 查询时会自动加上where条件 : name like ? || '%'，并按grade降序排序
List<StudentDto> list = mybatisAccessorService.getDtoListByAnnotation(studentQueryDto);
```

#### 8.2.分页查询
```java
StudentDto studentDto = new StudentDto();
studentDto.setName(studentName);
// 分页获取数据
List<StudentDto> list = mybatisAccessorService.getDtoPageByAnnotation(studentQueryDto,1,10);
```

#### 8.3.分组获取数据
* 定义ENTITY
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@TableName("student")
public class StudentGenderGroup {
    private Integer classId;

    private Integer gender;

    @TableField(exist=false)
    private BigDecimal averageAge;
}
```

* 定义DTO
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = StudentGenderGroup.class, service = IStudentGenderGroupService.class)
public class StudentGenderGroupDto {
    @QueryGroupBy
    private Integer classId;

    @QueryGroupBy
    private Integer gender;

    @QuerySelect("round(avg(age),2)")
    private BigDecimal averageAge; // 表中不存在的字段需要在entity中定义相同名称的字段，并标注@TableField(exist=false)
    
    @QueryGroupBy
    @QueryHaving(expression = "count(*) < 10")
    private Integer gender;
}
```

* 使用
```java
StudentGenderGroupDto studentDto = new StudentGenderGroupDto();
// 查询SQL: select class_id, gender, round(avg(age),2) as average_age from student group by class_id, gender
List<StudentGenderGroupDto> list = mybatisAccessorService.getDtoListByAnnotation(studentQueryDto);
```
#### 8.4.获取单条数据
```java
//根据ID获取单条数据并获取所有子项
StudentGenderGroupDto list = getDtoById.getDtoListById(StudentGenderGroupDto.class, 1L, true, null);

//根据queryDto设置的查询条件获取单条数据并获取特定子项（scoreList)
StudentDto result = mybatisAccessorService.getDtoByAnnotation(queryDto, false
                , Arrays.asList("scoreList"));
```

#### 8.5.查询单条数据完成后，再执行一条SQL语句获取子项，实现关联查询
* 与QueryJoin不同点
  1.QueryJoin通过左连接，内联查查询相关表，只执行一次；Selector在查询完成后，再执行一条SQL查询关联表，需执行多次。
  2.QueryJoin需要在ENTITY中定义DTO中相同名称的变量，Selector不需要
  3.Selector只能在获取单个对象后，再去查询关联表（对于mybatisAccessorService.getDtoListXXX,mybatisAccessorService.getEntityListXXX无效）;QueryJoin无限制。
  4.使用主键关联时，Selector注解可省略
* 定义
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = Student.class, service = IStudentService.class)
public class StudentDto2 {
    private Integer studentId;

    @Selector(tableColumn = "student_id", connector = Connector.eq
            , valueExpression = "#{studentId}")
    private List<ScoreDto> scoreList;
}
```

* 使用
```java
StudentDto2 entity = (StudentDto2) mybatisAccessorService.getDtoById(StudentDto2.class, 1, true, null);
```

#### 8.6.使用SQL连接获取子项，实现关联查询（支持左连接LEFT JOIN、内连接INNER JOIN）
* 定义DTO
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = Score.class, service = IScoreService.class)
public class ScoreDto implements Serializable {
    private Integer scoreId;
    private Integer studentId;
    // 使用主键关联,可忽略joinExpression属性
    // 仅关联一次的表，可忽略tableAlias属性
    // join无顺序要求，可忽略Order属性
    @QueryJoin(tableAlias = "student", joinExpression = "student.student_id = score.student_id and score.deleted = 0" , order = 1)
    private StudentDto student;
}
```

* ENTITY中加入对应的字段
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Score implements Serializable {
    private Integer scoreId;
    private Integer studentId;
    // 必须标注TableField(exist = false)， 否则报错
    @TableField(exist = false)
    private Student student;
}
```

* 使用
```java
ScoreDto scoreDto = new ScoreDto();
scoreDto.setStudentId(1);
List<ScoreDto> list = mybatisAccessorService.getDtoListByAnnotation(scoreDto);
```

### 9.数据更新
* 级联新增
```java
StudentDto studentDto = new StudentDto();
studentDto.setName(name);

List<ScoreDto> scoreDtoList = new ArrayList<>();
ScoreDto score1 = ScoreDto.builder().courseId(1).name("c_" + name).score(BigDecimal.valueOf(120)).build();
scoreDtoList.add(score1);
studentDto.setScoreList(scoreDtoList);

List<StudentCourseDto> courseDtoList = new ArrayList<>();
StudentCourseDto course1 = StudentCourseDto.builder().courseId(1).selectDate(new Date()).build();
courseDtoList.add(course1);
studentDto.setCourseList(courseDtoList);

// 保存数据（将保存三个表：Student，Score，StudentCourse）
Student updateResult = mybatisAccessorService.save(studentDto, true, null, false,null);
```

* 级联更新（可同时保存/更新/删除）
```java
StudentDto studentDto = mybatisAccessorService.getDtoByAnnotation(queryDto, true, null);
// 更新Course
studentDto.getCourseList().get(0).setCourseName("数学课");
// 新增Course
studentDto.getCourseList().add(StudentCourseDto.builder().courseId(10).courseName("语文课").build());
// 删除Score（需要再Deleted字段上标注DeleteFlag注解）
studentDto.getScoreList().get(0).setDeleted(1);
// 更新Score
studentDto.getScoreList().get(1).setScore(100);
mybatisAccessorService.update(studentDto, true, null, false, false,null);
```

* 级联删除
```java
// 删除主表Student以及子表Score,Course
mybatisAccessorService.deleteByIds(StudentDto.class, idList, Arrays.asList("scoreList","courseList"), false,null);
```

### 11.更新监听器
* 更新前或更新后，完成数据查找、数字统计、分配、执行特定SQL或者其他Bean的方法
* 三种更新监听器，DecodeEvent完成更新前从其他表获取信息并赋值到待更新DTO
* UpdateEvent与PropertyChangeEvent功能相似，完成数字的统计、分配、执行SQL、执行特定Bean的方法
* PropertyChangeEvent监听字段的变化，在字段产生变化时才会执行。UpdateEvent不监听字段变化。
* PropertyChangeEvent查找原值并与更新值做比较，相对开销比UpdateEvent大。

#### 10.1.DecodeEvent
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = ProductOrderDetail.class, service = IProductOrderDetailService.class)
//更新前根据ProductOrderDetail3Dto中productId的值，从Product表中获取productName，并设置到ProductOrderDetail3Dto中
@DecodeEvent(onExpression = "", targetProperties = "productName", matchProperties = "productId", sourceDtoClass = ProductDto.class)
public class ProductOrderDetail3Dto extends BaseDto implements Serializable {
    private Long orderDetailId;
    private Integer productId;
    private String productName;
}
```

#### 10.2.UpdateEvent
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = ProductOrder.class, service = IProductOrderService.class)
//ProductOrderDto2更新后用orderDetails的个数更新totalCount的值。
@UpdateEvent(onExpression = "#{orderDetails?.size() > 0}", updateEventClass = UpdateCountEvent.class, updateParams = {"totalCount", "orderDetails"})
public class ProductOrderDto2 extends BaseDto implements Serializable {
    private Integer orderId;

    private Integer totalCount;

    private List<ProductOrderDetailDto> orderDetails;
}
```

#### 10.3.PropertyChangeEvent
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = ProductOrderDetail.class, service = IProductOrderDetailService.class)
//weight发生变化（仅新增或者更新，删除时不触发）时，调用executeUpdateBean的test方法,参数为orderDetailId
@PropertyChangeEvent(listenProperties = "weight", updateEventClass = UpdateExecuteBeanMethodEvent.class, updateParams = {"executeUpdateBean", "test", "orderDetailId"}, updateType = {IUpdateCommand.UpdateType.INSERT, IUpdateCommand.UpdateType.UPDATE})
public class ProductOrderDetail3Dto extends BaseDto implements Serializable {
    private Integer orderDetailId;

    private Integer orderId;

    private BigDecimal weight;
}
```

### 11.更新管理MybatisAccessorUpdateManager
* 将需要更新的DTO，加入MybatisAccessorUpdateManager中，需要执行更新时，调用submit方法
* MybatisAccessorUpdateManager会将待更新DTO按照对应Entity中UpdateOrder注解定义的顺序以及Entity的名称排序后依次更新，避免因顺序引起的死锁问题
```java
public class Demo1 {
@Autowired
private MybatisAccessorUpdateManager updateManager;
    public void addDto() {
        updateManager.putDto(dto01);
        updateManager.putDto(dto02);
        updateManager.putDto(dtoList01);
        updateManager.submit();
    }
}
```

### 12.注解详细说明
#### 12.1.设置相关
##### 12.1.1.DtoEntityRelation
* 说明：定义DTO、Entity与Service的关系，作用于Entity类或者Dto类上
* 参数1 - entityClass : 定义与DTO有关系的Entity类，定义在Entity类上时可省略
* 参数2 - dtoClass : 定义与Entity有关系的Dto类，定义在Dto类上时可省略
* 参数3 - service : 定义CRUD的操作类，需要继承MybatisPlus的IService接口
* 范例：
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = Student.class, service = IStudentService.class)
public class StudentDto {
}
```

##### 12.2.2.EnableMybatisAccessor
* 说明：开启MybatisAccessor相关功能,扫描DtoEntityRelation注解
* 参数 - relationScan : 定义DtoEntityRelation注解所在的包
* 范例：
```java
@MapperScan("com.test.mybatis_accessor.mapper")
@EnableMybatisAccessor(
        relationScan = @RelationScanPackages({"com.test.mybatis_accessor.entity", "com.test.mybatis_accessor.dto"})
)
public class SpringBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebTestApplication.class, args);
    }
}
```

#### 12.2.查询相关
##### 12.2.1.QuerySelect
* 说明：作用于Dto的字段上，定义SQL语句的查询项目
* 参数 - value : 定义SQL语句的查询项目
* 范例：
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = StudentStatistics.class, service = IStudentAvgScoreService.class)
public class StudentAverageScoreDto {
    @QuerySelect("name || 'appendString'")
    private String name;
}
```

##### 12.2.2.QueryWhere
* 说明：作用于Dto的字段上，定义SQL语句的筛选条件，筛选条件为tableColumn(字段) + connector(对比条件) + expression(值)
* 参数1 - tableColumn : 定义SQL语句的筛选目标字段，省略时为Dto字段对应的表字段
* 参数2 - connector : 定义SQL语句的对比条件，可使用EQ(默认值)、LIKE、IN、GT、LT、EXIST等
* 参数3 - expression :定义SQL语句对比条件的值，默认为Dto字段的值。支持SPEL表达式
* 范例：
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = Student.class, service = IStudentService.class)
public class StudentQueryDto {
    private Integer studentId;

    //取出表中grade>=gradeMinValue的记录
    @QueryWhere(tableColumn = "grade", connector = Connector.GE)
    private Integer gradeMinValue;
}
```

##### 12.2.3.QueryGroup
* 说明：作用于Dto的字段上，定义SQL语句的分组
* 参数 - value : 定义SQL语句的分组字段
* 范例：
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = StudentGenderGroup.class, service = IStudentGenderGroupService.class)
public class StudentGenderGroupDto {
    @QueryGroupBy
    private Integer classId;

    @QueryGroupBy
    private Integer gender;

    @QuerySelect("round(avg(age),2)")
    private BigDecimal averageAge; // 表中不存在的字段需要在entity中定义相同名称的字段，并标注@TableField(exist=false)

}
```

##### 12.2.4.QueryHaving
* 说明：作用于Dto的字段上，定义SQL语句的分组过滤条件
* 参数 - value : 定义SQL语句的分组过滤条件
* 范例：
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = StudentStatistics.class, service = IStudentAvgScoreService.class)
public class StudentGenderGroupDto {
    @QueryGroupBy
    private Integer classId;

    @QueryGroupBy
    @QueryHaving(expression = "count(*) < 10")
    private Integer gender;
}
```

##### 12.2.5.QueryJoin
* 说明：作用于Dto的字段上，定义SQL语句的连接关系
* 参数1 - tableAlias : 定义SQL连接表的别名
* 参数2 - joinType : 左连接、右连接、内连接、全连接
* 参数3 - joinExpression : 连接表达式，支持SPEL，省略时使用主键关联
* 参数4 - order : 确定连接时的顺序
* 范例：
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = Score.class, service = IScoreService.class)
public class ScoreDto implements Serializable {
    private Integer scoreId;
    private Integer studentId;
    // 使用主键关联,可忽略joinExpression属性
    // 仅关联一次的表，可忽略tableAlias属性
    // join无顺序要求，可忽略Order属性
    @QueryJoin(tableAlias = "student", joinExpression = "student.student_id = score.student_id and score.deleted = 0" , order = 1)
    private StudentDto student;
}
```

##### 12.2.6.Selector
* 说明：作用于Dto的字段上。在获取单个DTO对象后，再根据Selector定义的条件查询相关记录
* 参数1 - tableColumn : 定义SQL语句的筛选目标字段，省略时为Dto字段对应的表字段
* 参数2 - connector : 定义SQL语句的对比条件，可使用EQ(默认值)、LIKE、IN、GT、LT、EXIST等
* 参数3 - valueExpression :定义SQL语句对比条件的值，支持SPEL表达式
* 参数4 - order : 确定查询时的顺序
* 范例：
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = Student.class, service = IStudentService.class)
public class StudentDto2 {
    private Integer studentId;

    @Selector(tableColumn = "student_id", connector = Connector.eq
            , valueExpression = "#{studentId}")
    private List<ScoreDto> scoreList;
}
```

##### 12.2.7.QueryOrder注解
* 说明：作用于Dto的字段上。定义排序。
* 参数1 - expression : 排序表达式，支持SPEL，可省略。默认按作用字段排序
* 参数2 - sortIndex : 排序顺位。
* 参数3 - sortOrder : 升序还是降序。默认升序。
* 范例：
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = Student.class, service = IStudentService.class)
public class StudentDto {
    @QueryOrder(sortIndex = 1, sortOrder = QueryOrder.ORDER_DESC)
    private String name;
}
```

#### 12.3.更新相关
##### 12.3.1.IdReference
* 说明：作用于Entity的字段上，表示该字段的值对应的是本表的一个ID。
* 参数：无
* 范例：
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class PersonInfo extends BaseEntity implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer personId;

    //代表上级的personId
    @IdReference
    private Integer leaderId;

    private String personName;
}
```

##### 12.3.2.UpdateCascade
* 说明：作用于Dto的字段上。定义是否支持字段的级联更新
* 参数1 - value : false时不支持级联更新，默认为true
* 范例：
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = Score.class, service = IScoreService.class)
public class ScoreDto implements Serializable {
    private Integer scoreId;
    private Integer studentId;

    //更新ScoreDto时不会更新StudentDto
    @UpdateCascade(false)
    @QueryJoin(tableAlias = "student", joinExpression = "student.student_id = score.student_id and score.deleted = 0" , order = 1)
    private StudentDto student;
}
```

##### 12.3.3.DeleteFlag
* 说明：作用于Dto的字段上。如果字段值是1,更新或者级联更新时会删除ID对应的记录
* 参数1 - physicDelete : Entity中开启逻辑删除TableLogic时，可在Dto中控制物理删除还是逻辑删除。true时表示物理删除。默认false
* 范例：
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = StudentCourse.class, service = IStudentCourseService.class)
public class StudentCourseDto {
    private Integer studentCourseId;

    @DeleteFlag
    private Integer deleted;

    private Integer version;
}
```

##### 12.3.4.DeleteAndInsertNewOnUpdate
* 说明：作用于Dto的字段上。更新时，使用删除再插入的方式代替更新
* 参数1 - value : true时表示开启，默认为true
* 参数2 - deleteEvenIfEmpty : true时表示即使Dto字段是空的也去删除，默认为false
* 范例：
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = Student.class, service = IStudentService.class)
public class StudentDto6 implements Serializable {
    private Integer studentId;

    @DeleteAndInsertNewOnUpdate(value = true, deleteEvenIfEmpty = true)
    private List<StudentCourseDto> courseList;
}
```

##### 12.3.5.UpdateOrder
* 说明：作用于Entity的字段上，定义更新优先级，与MybatisAccessorUpdateManager一起使用
* 参数 - value : 更新优先级，越小优先级越高

#### 12.4.监听器相关
##### 12.4.1.DecodeEvent
* 说明：作用于Dto类上。在更新前获取其他表信息并设置到DTO中
* 参数1 - onExpression : 定义符合什么条件时开启，支持SPEL。
* 参数2 - sourceDtoClass : 定义数据来源
* 参数3 - sourceProperties : 从sourceDtoClass获取哪些字段，与targetProperties一致时可省略
* 参数4 - targetProperties : 获取的值设置到哪些字段中去
* 参数5 - matchSourceProperties : 从sourceDtoClass获取记录时，过滤条件包含哪些字段，与matchProperties一致时可省略
* 参数6 - matchProperties : 从sourceDtoClass获取记录时，过滤条件的值（从DTO中获取）
* 参数7 - errorWhenNotExist : 找不到数据时是否报错，默认是
* 参数8 - updateType : 更新类型，默认新增/修改
* 参数9 - executeTiming : 更新时机，默认更新前
* 范例：
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = OrderDetail.class, service = IOrderDetailService.class)
//更新时根据productId去Product表中获取productName并设置到OrderDetailDto.productName中
@DecodeEvent(onExpression = "", targetProperties = "productName", matchProperties = "productId"
        , sourceDtoClass = ProductDto.class)
public class OrderDetailDto extends BaseDto implements Serializable {
    private Integer orderDetailId;

    private Integer productId;

    private String productName;

}
```

##### 12.4.2.UpdateEvent
* 说明：作用于Dto类上。表示在更新前后执行一些操作
* 参数1 - onExpression : 定义符合什么条件时开启，支持SPEL。
* 参数2 - updateEventClass : 更新前后执行的操作类，必须实现IUpdateEvent接口
* 参数3 - updateParams : 更新参数，只能是String类型
* 参数4 - UpdateType :更新类型，默认IUpdateEvent接口中getDefaultUpdateTypes方法的值
* 参数5 - executeTiming : 更新时机，默认IUpdateEvent接口中getDefaultExecuteTiming方法的值
* 范例：
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = PersonInfo.class, service = IPersonInfoService.class)
//更新完成后，将personInfoList的条数更新到teamCount对应的字段中去
@UpdateEvent(onExpression = "", updateEventClass = UpdateCountEvent.class, updateParams = {"teamCount", "personInfoList"})
public class PersonInfoDto extends BaseDto implements Serializable {
    private Integer personId;

    private Integer leaderId;

    private String personName;

    private Integer teamCount;

    private Integer deleted;

    private List<PersonInfoDto> personInfoList;
}
```

###### 预定义的IUpdateEvent实现类
* 1.UpdateCountEvent : 更新完成后将子项列表的条数更新到主项的字段中去。
  参数1：主项字段名。参数2：子项列表名。
* 2.UpdateSumEvent : 更新完成后将子项列表指定字段的合计数更新到主项的字段中去。
  参数1：主项字段名。参数2：子项列表名。参数3：子项字段名称。 
* 3.UpdateMaxEvent : 更新完成后将子项列表指定字段的最大值更新到主项的字段中去。
  参数1：主项字段名。参数2：子项列表名。参数3：子项字段名称。 
* 4.UpdateMaxEvent : 更新完成后将子项列表指定字段的最小值更新到主项的字段中去。
  参数1：主项字段名。参数2：子项列表名。参数3：子项字段名称。 
* 5.UpdateAvgEvent : 更新完成后将子项列表指定字段的平均值更新到主项的字段中去。
  参数1：主项字段名。参数2：子项列表名。参数3：子项字段名称。 
* 6.UpdateAssignEvent : 更新完成后将主项字段值根据子项中的权重比例分摊到子项字段上。
  参数1：主项字段名。参数2：子项列表名。参数3：子项字段名称。 参数4：精度。 参数5：子项权重字段名。 
* 7.UpdateAvgAssignEvent : 更新完成后将主项字段值平均地分摊到子项字段上。
  参数1：主项字段名。参数2：子项列表名。参数3：子项字段名称。 参数4：精度。 
* 8.UpdateFillEvent : 更新完成后将主项字段值按一定顺序分配到子项字段上，子项字段达到最大值后再分配下一条记录
  参数1：主项字段名。参数2：分配剩余值设置到哪个字段中。参数3：子项列表名。参数4：分配子项字段名称。 参数5：分配最大值（可使用数值或者子项字段名）。参数6：排序字段。参数7：升序ASC还是降序DESC 
* 9.UpdateExecuteSqlEvent : 更新完成后执行指定SQL
  参数1：SQL语句，支持SPEL表达式。
* 10.UpdateLogEvent : 更新前打印日志
  参数1：打印数据格式,支持SPEL，可省略。默认打印所有字段
* 11.UpdateExecuteBeanMethodEvent : 更新后执行指定Bean的方法
  参数1：bean名称。参数2：执行方法。参数3开始：Dto字段，字段值会作为执行方法的参数。不存在参数3时会将Dto作为参数。

##### 12.4.3.PropertyChangeEvent
* 说明：作用于Dto类上。监听到属性变化时，执行对应的IUpdateEvent实现类
* 参数1 - listenProperties : 监听哪些字段，可省略。
* 参数2 - fromExpression : 定义更新前需符合什么条件，支持SPEL，可省略。
* 参数3 - toExpression : 定义更新后需符合什么条件，支持SPEL，可省略。
* 参数4 - updateEventClass : 更新前后执行的操作类，必须实现IUpdateEvent接口
* 参数5 - updateParams : 更新参数，只能是String类型
* 参数6 - UpdateType :更新类型，默认IUpdateEvent接口中getDefaultUpdateTypes方法的值
* 参数7 - executeTiming : 更新时机，默认IUpdateEvent接口中getDefaultExecuteTiming方法的值
* 范例：
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = ProductOrderDetail.class, service = IProductOrderDetailService.class)
//weight发生变化（新增或者更新时触发，删除不触发）时，调用executeUpdateBean的test方法,参数为orderDetailId的值
@PropertyChangeEvent(listenProperties = "weight", updateEventClass = UpdateExecuteBeanMethodEvent.class, updateParams = {"executeUpdateBean", "test", "orderDetailId"}, updateType = {IUpdateCommand.UpdateType.INSERT, IUpdateCommand.UpdateType.UPDATE})
public class ProductOrderDetail3Dto extends BaseDto implements Serializable {
    private Integer orderDetailId;

    private Integer orderId;

    private BigDecimal weight;
}
```

### 13.主要类说明
#### 13.1.MybatisAccessorService
##### 13.1.1.getEntityById方法
* 说明：通过ID获取Entity
* 参数1 - dtoClass : Dto类。
* 参数2 - id : 主键的值。

##### 13.1.2.getEntityByAnnotation方法
* 说明：通过Dto类注解上的查询条件获取Entity
* 参数1 - Object : Dto类的实例化对象。

##### 13.1.3.getDtoById方法
* 说明：通过ID获取Dto
* 参数1 - dtoClass : Dto类。
* 参数2 - id : 主键的值。
* 参数3 - includeAllChildren : 是否获取所有子项。
* 参数4 - children : includeAllChildren为false时，指定需要获取的子项。

##### 13.1.4.getDtoByAnnotation方法
* 说明：通过Dto类注解上的查询条件获取Dto
* 参数1 - Object : Dto类的实例化对象。
* 参数2 - includeAllChildren : 是否获取所有子项。
* 参数3 - children : includeAllChildren为false时，指定需要获取的子项。

##### 13.1.5.getEntityListByAnnotation方法
* 说明：通过Dto类注解上的查询条件获取Entity的列表
* 参数1 - Object : Dto类的实例化对象。

##### 13.1.6.getDtoListByAnnotation方法
* 说明：通过Dto类注解上的查询条件获取Dto的列表
* 参数1 - Object : Dto类的实例化对象。

##### 13.1.7.getEntityPageByAnnotation方法
* 说明：通过Dto类注解上的查询条件获取Entity的列表
* 参数1 - Object : Dto类的实例化对象。
* 参数2 - pageIndex : 页码。
* 参数3 - pageSize : 每页记录数。

##### 13.1.8.getDtoPageByAnnotation方法
* 说明：通过Dto类注解上的查询条件获取Dto的列表
* 参数1 - Object : Dto类的实例化对象。
* 参数2 - pageIndex : 页码。
* 参数3 - pageSize : 每页记录数。

##### 13.1.9.save方法
* 说明：新增单个Dto，支持子项级联新增
* 参数1 - Object : Dto类的实例化对象。
* 参数2 - includeAllChildren : 是否级联新增所有子项。
* 参数3 - children : includeAllChildren为false时，指定需要保存的子项。
* 参数4 - updateChildrenOnly : 为true时只新增子项。
* 参数5 - updateEventLogId : UpdateLogEvent会将updateEventLogId打印在日志信息中，null时会自动生成新的updateEventLogId。

##### 13.1.10.saveList方法
* 说明：新增Dto列表，支持子项级联新增
* 参数1 - Object : Dto类的实例化对象。
* 参数2 - includeAllChildren : 是否级联新增所有子项。
* 参数3 - children : includeAllChildren为false时，指定需要保存的子项。
* 参数4 - updateChildrenOnly : 为true时只新增子项。
* 参数5 - updateEventLogId : UpdateLogEvent会将updateEventLogId打印在日志信息中，null时会自动生成新的updateEventLogId。

##### 13.1.11.update方法
* 说明：更新单个Dto，支持子项级联新增/修改/删除。子项ID为空时新增。子项ID不为空、无DeleteFlag注解或DeleteFlag注解对应属性的值不为1，修改。子项ID不为空、有DeleteFlag注解且DeleteFlag注解对应属性的值为1，删除。
* 参数1 - Object : Dto类的实例化对象。
* 参数2 - includeAllChildren : 是否级联新增/修改/删除所有子项。
* 参数3 - children : includeAllChildren为false时，指定需要新增/修改/删除的子项。
* 参数4 - updateChildrenOnly : 为true时只新增/修改/删除子项。
* 参数5 - updateEventLogId : UpdateLogEvent会将updateEventLogId打印在日志信息中，null时会自动生成新的updateEventLogId。

##### 13.1.12.updateList方法
* 说明：更新Dto列表，支持子项级联新增/修改/删除。子项ID为空时新增。子项ID不为空、无DeleteFlag注解或DeleteFlag注解对应属性的值不为1，修改。子项ID不为空、有DeleteFlag注解且DeleteFlag注解对应属性的值为1，删除。
* 参数1 - Object : Dto类的实例化对象。
* 参数2 - includeAllChildren : 是否级联新增/修改/删除所有子项。
* 参数3 - children : includeAllChildren为false时，指定需要新增/修改/删除的子项。
* 参数4 - updateChildrenOnly : 为true时只新增/修改/删除子项。
* 参数5 - updateEventLogId : UpdateLogEvent会将updateEventLogId打印在日志信息中，null时会自动生成新的updateEventLogId。

##### 13.1.13.DeleteByIds方法
* 说明：更新Dto列表，支持子项级联删除。
* 参数1 - dtoClass : Dto类。
* 参数2 - ids : 主键值的集合。
* 参数3 - includeAllChildren : 是否级联删除所有子项。
* 参数4 - children : includeAllChildren为false时，指定需要删除的子项。
* 参数5 - updateChildrenOnly : 为true时只删除子项。
* 参数6 - updateEventLogId : UpdateLogEvent会将updateEventLogId打印在日志信息中，null时会自动生成新的updateEventLogId。

#### 13.2.MybatisAccessorUpdateManager
##### 13.2.1.putDto方法
* 说明：将需要更新的对象，加入到MybatisAccessorUpdateManager的更新列表中。
* 参数1 - dto : Dto实例或者Dto实例列表。

##### 13.2.2.submit方法
* 说明：对MybatisAccessorUpdateManager的更新列表进行排序依次更新
