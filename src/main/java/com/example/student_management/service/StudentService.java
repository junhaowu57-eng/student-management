package com.example.student_management.service;

import com.example.student_management.Student;
import com.example.student_management.mapper.StudentMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import com.example.student_management.common.PageResult;

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

    public int delete(Integer id){
        return studentMapper.delete(id);
    }
    public PageResult<Student> findPage(int page,int size,String name){
        int offset=(page-1)*size;
        List<Student>records=studentMapper.findPage(name,offset,size);
        int total=studentMapper.countAll(name);
        return new PageResult<>(records, total, page, size);
    }
}
