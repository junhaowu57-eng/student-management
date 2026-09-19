package com.example.student_management.mapper;

import com.example.student_management.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import java.util.List;
import org.apache.ibatis.annotations.Param;

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
    @Select ("""
            <script>
            SELECT * FROM student
            <where>
                <if test="name !=null and name !=''">
                    name LIKE CONCAT('%',#{name},'%')
                </if>
            </where>
            LIMIT #{size} OFFSET #{offset}
            </script>
            """)
    List<Student>findPage(
        @Param ("name") String name,
        @Param("offset") int offset,
        @Param("size") int size);
    @Select ("""
            <script>
            SELECT COUNT(*) FROM student
            <where>
                <if test="name !=null and name!=''">
                    name LIKE CONCAT('%',#{name},'%')
                </if>
            </where>
            </script>
            """)
    int countAll(@Param ("name") String name);
}
