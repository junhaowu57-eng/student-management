package com.example.student_management.controller;

import com.example.student_management.Student;
import com.example.student_management.common.Result;
import com.example.student_management.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.example.student_management.annotation.RequireRole;

@RestController
@RequestMapping("/students")
public class StudentController {
    private final StudentService studentService;
    public StudentController(StudentService studentService){
        this.studentService=studentService;
    }

    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id){
       return Result.success(studentService.findById(id));
    }

    @GetMapping
    public Result findPage(
            @RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="10") int size,
            @RequestParam(required=false) String name){
                return Result.success(studentService.findPage(page,size,name));
            }

    @RequireRole ("ADMIN")
    @PostMapping("/insert")
    public Result insert(@Valid @RequestBody Student student,HttpServletRequest request){
        return Result.success(studentService.insert(student));
    }

    @RequireRole("ADMIN")
    @PutMapping("/update")
    public Result update(@Valid @RequestBody Student student,HttpServletRequest request){
        return Result.success(studentService.update(student));
    }
    
    @RequireRole("ADMIN")
    @DeleteMapping("/delete/{id}")
    public Result delete(@Valid @PathVariable Integer id,HttpServletRequest request){
        return Result.success(studentService.delete(id));
    }
}
