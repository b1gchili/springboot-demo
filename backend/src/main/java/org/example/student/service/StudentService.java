package org.example.student.service;

import org.example.student.exception.BusinessException;
import org.example.student.mapper.StudentMapper;
import org.example.student.model.PageResult;
import org.example.student.model.Student;
import org.example.student.model.StudentQueryRequest;
import org.example.student.util.StudentIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Service
public class StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);

    private final StudentMapper studentMapper;

    public StudentService(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    /**
     * 使用 MyBatis 从 MySQL 分页查询学生列表。
     */
    public PageResult<Student> queryStudents(StudentQueryRequest request) {
        int pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        long total = studentMapper.countStudents(request);
        List<Student> list = studentMapper.queryStudents(request, pageSize, offset);

        logger.info("查询学生列表: 条件={}, 总数={}, 当前页={}", request, total, pageNum);
        return new PageResult<>(list, total, pageNum, pageSize);
    }

    /**
     * 根据学号查询学生详情。
     */
    public Student getStudent(String studentId) {
        Student student = studentMapper.findByStudentId(studentId);
        if (student == null) {
            throw new BusinessException(404, "学生不存在: " + studentId);
        }
        return student;
    }

    /**
     * 新增学生。
     */
    public String addStudent(Student student) {
        validateStudent(student);
        String studentId = StudentIdGenerator.generate();
        Date now = new Date();
        student.setStudentId(studentId);
        student.setCreateTime(now);
        student.setUpdateTime(now);
        studentMapper.insertStudent(student);
        logger.info("新增学生: studentId={}, name={}", studentId, student.getName());
        return studentId;
    }

    /**
     * 更新学生信息。
     */
    public void updateStudent(String studentId, Student student) {
        validateStudent(student);
        student.setStudentId(studentId);
        student.setUpdateTime(new Date());
        int rows = studentMapper.updateStudent(student);
        if (rows == 0) {
            throw new BusinessException(404, "学生不存在: " + studentId);
        }
        logger.info("更新学生: studentId={}", studentId);
    }

    /**
     * 删除学生。
     */
    public void deleteStudent(String studentId) {
        int rows = studentMapper.deleteByStudentId(studentId);
        if (rows == 0) {
            throw new BusinessException(404, "学生不存在: " + studentId);
        }
        logger.info("删除学生: studentId={}", studentId);
    }

    /**
     * 校验学生信息。
     */
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
