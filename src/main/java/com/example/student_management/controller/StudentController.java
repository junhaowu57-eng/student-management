package com.example.student_management.controller;

import com.example.student_management.Student;
import com.example.student_management.service.StudentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/students")
public class StudentController {
    private final StudentService studentService;
    public StudentController(StudentService studentService){
        this.studentService=studentService;
    }
    @GetMapping("/{id}")
    public Student findById(@PathVariable Integer id){
        return studentService.findById(id);
    }
    @GetMapping
    public java.util.List<Student>findAll(){
        return studentService.findAll();
    }
}
