package org.example.student.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.example.student.exception.BusinessException;
import org.example.student.mapper.StudentMapper;
import org.example.student.model.PageResult;
import org.example.student.model.Student;
import org.example.student.model.StudentQueryRequest;
import org.example.student.util.StudentIdGenerator;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class StudentService {

    private final StudentMapper studentMapper;

    public StudentService(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    /**
     * 复杂查询保留 MyBatis XML，分页使用 PageHelper。
     */
    public PageResult<Student> queryStudents(StudentQueryRequest request) {
        int pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();

        PageHelper.startPage(pageNum, pageSize);
        List<Student> list = studentMapper.queryStudents(request);
        PageInfo<Student> pageInfo = new PageInfo<>(list);

        log.info("查询学生列表: 条件={}, 总数={}, 当前页={}", request, pageInfo.getTotal(), pageNum);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), pageNum, pageSize);
    }

    /**
     * 简单查询使用 MyBatis-Plus Wrapper。
     */
    public Student getStudent(String studentId) {
        Student student = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getStudentId, studentId));
        if (student == null) {
            throw new BusinessException(404, "学生不存在: " + studentId);
        }
        return student;
    }

    /**
     * 新增学生使用 MyBatis-Plus insert。
     */
    public String addStudent(Student student) {
        validateStudent(student);
        String studentId = StudentIdGenerator.generate();
        Date now = new Date();
        student.setStudentId(studentId);
        student.setCreateTime(now);
        student.setUpdateTime(now);
        try {
            studentMapper.insert(student);
        } catch (DataAccessException e) {
            throw new BusinessException(500, "新增学生失败，请稍后再试");
        }
        log.info("新增学生: studentId={}, name={}", studentId, student.getName());
        return studentId;
    }

    /**
     * 更新学生使用 MyBatis-Plus update + Wrapper。
     */
    public void updateStudent(String studentId, Student student) {
        validateStudent(student);
        Date now = new Date();
        int rows;
        try {
            rows = studentMapper.update(null, new LambdaUpdateWrapper<Student>()
                    .eq(Student::getStudentId, studentId)
                    .set(Student::getName, student.getName())
                    .set(Student::getAge, student.getAge())
                    .set(Student::getGender, student.getGender())
                    .set(Student::getDescription, student.getDescription())
                    .set(Student::getUpdateTime, now));
        } catch (DataAccessException e) {
            throw new BusinessException(500, "更新学生失败，请稍后再试");
        }
        if (rows == 0) {
            throw new BusinessException(404, "学生不存在: " + studentId);
        }
        log.info("更新学生: studentId={}", studentId);
    }

    /**
     * 删除学生使用 MyBatis-Plus delete + Wrapper。
     */
    public void deleteStudent(String studentId) {
        int rows;
        try {
            rows = studentMapper.delete(new LambdaQueryWrapper<Student>()
                    .eq(Student::getStudentId, studentId));
        } catch (DataAccessException e) {
            throw new BusinessException(500, "删除学生失败，请稍后再试");
        }
        if (rows == 0) {
            throw new BusinessException(404, "学生不存在: " + studentId);
        }
        log.info("删除学生: studentId={}", studentId);
    }

    private void validateStudent(Student student) {
        if (!StringUtils.hasText(student.getName())) {
            throw new BusinessException(400, "学生姓名不能为空");
        }
        if (student.getName().length() > 50) {
            throw new BusinessException(400, "学生姓名不能超过50个字符");
        }
        if (student.getAge() == null || student.getAge() < 0) {
            throw new BusinessException(400, "学生年龄不能为空且不能小于0");
        }
        if (student.getAge() > 150) {
            throw new BusinessException(400, "学生年龄不能超过150");
        }
        if (!StringUtils.hasText(student.getGender())) {
            throw new BusinessException(400, "学生性别不能为空");
        }
        if (!"男".equals(student.getGender()) && !"女".equals(student.getGender())) {
            throw new BusinessException(400, "学生性别只能为男或女");
        }
        if (student.getDescription() != null && student.getDescription().length() > 200) {
            throw new BusinessException(400, "描述不能超过200个字符");
        }
    }
}
