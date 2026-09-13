package com.example.student_management.controller;

import com.example.student_management.Student;
import com.example.student_management.common.Result;
import com.example.student_management.service.StudentService;
import jakarta.validation.Valid;
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
    public Result findAll(){
       return Result.success(studentService.findAll());
    }
    @PostMapping("/insert")
    public Result insert(@Valid @RequestBody Student student){
        int rows=studentService.insert(student);
        return Result.success(rows);
    }
    @PutMapping("/update")
    public Result update(@Valid @RequestBody Student student){
        int rows=studentService.update(student);
        if(rows==0){
            return Result.error(404,"学生不存在");
        }
        return Result.success(rows);
    }
    @DeleteMapping("/delete/{id}")
    public Result delete(@Valid @PathVariable Integer id){
        int rows=studentService.delete(id);
        if(rows==0){
            return Result.error(404,"学生不存在");
        }
        return Result.success(id);
    }
}
