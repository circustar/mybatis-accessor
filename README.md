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
            <version>1.0.3</version>
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

#### 8.6.QueryJoin注解,使用在DTO的字段上，可实现关联查询（支持左连接LEFT JOIN、右连接RIGHT JOIN、内连接INNER JOIN、全连接FULL JOIN）


#### 8.n.Selector注解,使用在DTO的字段上，查询主表完成后（单条数据非列表），再执行一条SQL语句查询子表数据

