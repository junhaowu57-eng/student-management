package com.example.student_management.mapper;

import com.example.student_management.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface StudentMapper {
    @Select("SELECT * FROM student WHERE id=#{id}")
    Student findById(Integer id);
    @Select("SELECT *FROM student")
    List<Student>findAll();
}
