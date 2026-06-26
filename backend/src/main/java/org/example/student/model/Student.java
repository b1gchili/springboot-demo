package org.example.student.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 学生实体类。
 */
@TableName("student")
public class Student {

    /** 数据库主键。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 学号。 */
    @TableField("student_id")
    private String studentId;

    /** 姓名。 */
    @NotBlank(message = "学生姓名不能为空")
    @Size(max = 50, message = "学生姓名不能超过50个字符")
    private String name;

    /** 年龄。 */
    @NotNull(message = "学生年龄不能为空")
    @Min(value = 0, message = "学生年龄不能小于0")
    @Max(value = 150, message = "学生年龄不能超过150")
    private Integer age;

    /** 性别。 */
    @NotBlank(message = "学生性别不能为空")
    @Pattern(regexp = "^(男|女)$", message = "学生性别只能为男或女")
    private String gender;

    /** 描述。 */
    @Size(max = 200, message = "描述不能超过200个字符")
    private String description;

    /** 创建时间。 */
    @TableField("create_time")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间。 */
    @TableField("update_time")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public Student() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
