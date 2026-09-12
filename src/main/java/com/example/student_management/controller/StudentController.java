package com.example.student_management.controller;

import com.example.student_management.Student;
import com.example.student_management.common.Result;
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
    public Result findById(@PathVariable Integer id){
       Student student=studentService.findById(id);
       if(student==null){
        return Result.error(404,"学生不存在");
       }
       return Result.success(student);
    }
    @GetMapping
    public java.util.List<Student>findAll(){
        return studentService.findAll();
    }
    @PostMapping("/insert")
    public int insert(@RequestBody Student student){
        return studentService.insert(student);
    }
    @PutMapping("/update")
    public int update(@RequestBody Student student){
        return studentService.update(student);
    }
    @DeleteMapping("/delete/{id}")
    public int delete(@PathVariable Integer id){
        return studentService.delete(id);
    }
}
