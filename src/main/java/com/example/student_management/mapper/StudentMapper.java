package com.example.student_management.mapper;

import com.example.student_management.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import java.util.List;

@Mapper
public interface StudentMapper {
    @Select("SELECT * FROM student WHERE id=#{id}")
    Student findById(Integer id);
    @Select("SELECT *FROM student")
    List<Student>findAll();
    @Insert("INSERT INTO student(name,age,gender,email) VALUES(#{name},#{age},#{gender},#{email})")
    int insert(Student student);
    @Update("UPDATE student SET name=#{name},age=#{age},gender=#{gender},email=#{email} WHERE id=#{id}")
    int update(Student student);
    @Delete("DELETE FROM student WHERE id=#{id}")
    int delete(Integer id);
}
