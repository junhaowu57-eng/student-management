package com.example.student_management.service;

import com.example.student_management.Student;
import com.example.student_management.mapper.StudentMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import com.example.student_management.common.PageResult;
import com.example.student_management.exception.BusinessException;

@Service
public class StudentService {
    private final StudentMapper studentMapper;
    public StudentService(StudentMapper studentMapper){
        this.studentMapper=studentMapper;
    }

    public Student findById(Integer id){
        Student student=studentMapper.findById(id);
        if(student==null){
            throw new BusinessException(404, "学生不存在");
        }
        return student;
    }

    public int insert(Student student){
        int rows=studentMapper.insert(student);
        if(rows==0){
            throw new BusinessException(404, "学生不存在");
        }
        return rows;
    }

    public int update(Student student){
        int rows=studentMapper.update(student);
        if(rows==0){
            throw new BusinessException(404, "学生不存在");
        }
        return rows;
    }

    public int delete(Integer id){
        int rows=studentMapper.delete(id);
        if(rows==0){
            throw new BusinessException(404,"学生不存在");
        }
        return rows;
    }
    public PageResult<Student> findPage(int page,int size,String name){
        int offset=(page-1)*size;
        List<Student>records=studentMapper.findPage(name,offset,size);
        int total=studentMapper.countAll(name);
        return new PageResult<>(records, total, page, size);
    }
}
