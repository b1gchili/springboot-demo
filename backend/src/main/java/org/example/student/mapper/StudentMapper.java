package org.example.student.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.student.model.Student;
import org.example.student.model.StudentQueryRequest;

import java.util.List;

@Mapper
public interface StudentMapper {
    List<Student> queryStudents(@Param("request") StudentQueryRequest request);

    Student findByStudentId(@Param("studentId") String studentId);

    int insertStudent(Student student);

    int updateStudent(Student student);

    int deleteByStudentId(@Param("studentId") String studentId);
}
