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
       Student student=studentService.findById(id);
       if(student==null){
        return Result.error(404,"学生不存在");
       }
       return Result.success(student);
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
        int rows=studentService.insert(student);
        return Result.success(rows);
    }

    @RequireRole("ADMIN")
    @PutMapping("/update")
    public Result update(@Valid @RequestBody Student student,HttpServletRequest request){
        int rows=studentService.update(student);
        if(rows==0){
            return Result.error(404,"学生不存在");
        }
        return Result.success(rows);
    }
    
    @RequireRole("ADMIN")
    @DeleteMapping("/delete/{id}")
    public Result delete(@Valid @PathVariable Integer id,HttpServletRequest request){
        int rows=studentService.delete(id);
        if(rows==0){
            return Result.error(404,"学生不存在");
        }
        return Result.success(rows);
    }
}
