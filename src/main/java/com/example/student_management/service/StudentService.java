package com.example.student_management.service;

import com.example.student_management.Student;
import com.example.student_management.mapper.StudentMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StudentService {
    private final StudentMapper studentMapper;
    public StudentService(StudentMapper studentMapper){
        this.studentMapper=studentMapper;
    }

    public Student findById(Integer id){
        return studentMapper.findById(id);
    }
    
    public List<Student>findAll(){
        return studentMapper.findAll();
    }

    public int insert(Student student){
        return studentMapper.insert(student);
    }

    public int update(Student student){
        return studentMapper.update(student);
    }
}
