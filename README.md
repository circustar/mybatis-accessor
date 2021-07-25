### 1.目标
* 简化数据库操作，不使用SQL的情况下完成CRUD操作

### 2.依赖
* mybatis-plus

### 3.设计要求
* 表必须设置主键，且主键为单个字段。
* 主键自动生成（数据库自动生成或者使用mybatis-plus注解生成）
* 存在主从关系的表，子表中对应主表主键字段的变量名必须与主表主键的变量名保持一致
* 遵循mybatis-plus的规则，定义service,mapper和entity
* 定义DTO并关联Entity和service

### 4.引入依赖
```xml
    <dependency>
        <groupId>com.circustar</groupId>
        <artifactId>mybatis-accessor</artifactId>
        <version>1.1.0</version>
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

### 8.各种注解使用
#### 8.1.QueryWhere注解,使用在DTO的字段上，可定义查询条件
* 定义DTO
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = Student.class, service = IStudentService.class)
public class StudentDto {
    @QueryWhere(connector = Connector.likeRight)
    private String name;
}
```

* 使用
```java
    StudentDto studentDto = new StudentDto();
    studentDto.setName(studentName);
    // 查询时会自动加上where条件 : name like ? || '%'
    List<StudentDto> list = mybatisAccessorService.getDtoListByAnnotation(studentQueryDto);
```

#### 8.2.QueryOrder注解,使用在DTO的字段上，可定义查询顺序
* 定义DTO
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

* 使用
```java
    StudentDto studentDto = new StudentDto();
    studentDto.setName(studentName);
    // 查询时会自动加上order by name desc
    List<StudentDto> list = mybatisAccessorService.getDtoListByAnnotation(studentQueryDto);
```
#### 8.3.QueryGroupBy注解,使用在DTO的字段上，定义Group By分组字段
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

}
```

* 使用
```java
    StudentGenderGroupDto studentDto = new StudentGenderGroupDto();
    // 查询SQL: select class_id, gender, round(avg(age),2) as average_age from student group by class_id, gender
    List<StudentGenderGroupDto> list = mybatisAccessorService.getDtoListByAnnotation(studentQueryDto);
```
#### 8.4.QuerySelect注解,使用在DTO的字段上，定义SQL中的查询结果字段（参考8.3）

#### 8.5.QueryHaving注解,使用在DTO的字段上，定义SQL中的Having条件
* 定义DTO
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

* 使用
```java
    StudentGenderGroupDto studentDto = new StudentGenderGroupDto();
    // 查询时会自动加上group by class_id, gender having count(*) < 10
    List<StudentGenderGroupDto> list = mybatisAccessorService.getDtoListByAnnotation(studentQueryDto);
```

#### 8.6.QueryJoin注解,使用在DTO的字段上，可实现关联查询（支持左连接LEFT JOIN、内连接INNER JOIN）
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

### 8.7.QueryOrder注解，使用在DTO字段上，实现排序
* 按照updateTime降序获取数据
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@DtoEntityRelation(entityClass = Score.class, service = IScoreService.class)
public class ScoreDto implements Serializable {
    private Integer scoreId;
    private Integer studentId;
    
    @QueryOrder(sortOrder = QueryOrder.ORDER_DESC)
    private Date updateTime;
}
```

#### 8.8.Selector注解,使用在DTO的字段上，查询完成后，再执行一条SQL语句查询关联数据
* 与QueryJoin不同点
  1.QueryJoin通过左连接，内联查查询相关表，只执行一次；Selector在查询完成后，再执行一条SQL查询关联表，需执行多次。
  2.QueryJoin需要再ENTITY中定义DTO中相同名称的变量，Selector只需要再DTO中定义
  3.Selector只能在获取单个对象后，再去查询关联表（对于mybatisAccessorService.getDtoListByXXX,mybatisAccessorService.getEntityListByXXX无效）;QueryJoin无限制。

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
    StudentDto2 entity = (StudentDto2) mybatisAccessorService.getDtoById(StudentDto2.class, 1, false, new String[]{"scoreList"});
```

#### 8.9.分页
```java
    // 获取第一页，每页10条数据
    PageInfo<StudentDto> pageInfo = mybatisAccessorService.getDtoPageByAnnotation(queryDto,1,10);
```

### 9.数据更新
* 前置条件：ID使用自动生成；主表ID在子表中对应的名称相同；
* 支持级联保存/更新/删除（混合）
* 级联保存
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
    Student updateResult = mybatisAccessorService.save(studentDto, true
                , null, false);
```

* 级联更新（可同时保存/更新/删除）
```java
    StudentDto studentDto = mybatisAccessorService.getDtoByAnnotation(queryDto, true, null);
    assert(studentDto.getScoreList().size() == 2);
    assert(studentDto.getCourseList().size() == 2);
    // 更新Course
    studentDto.getCourseList().get(0).setCourseName("数学课");
    // 插入Course
    studentDto.getCourseList().add(StudentCourseDto.builder().courseId(10).courseName("语文课").build());
    // 删除Score（需要再Deleted字段上标注DeleteFlag注解）
    studentDto.getScoreList().get(0).setDeleted(1);
    // 更新Score
    studentDto.getScoreList().get(1).setScore(100);
    mybatisAccessorService.update(studentDto, true, null, false, false);
```

* 级联删除
```java
    // 删除主表Student以及子表Score,Course
    mybatisAccessorService.deleteByIds(StudentDto.class
        , idList, new String[]{"scoreList","courseList"}, false);
```
