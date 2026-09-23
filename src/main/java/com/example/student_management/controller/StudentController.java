package com.example.student_management.controller;

import com.example.student_management.Student;
import com.example.student_management.common.Result;
import com.example.student_management.service.StudentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.example.student_management.annotation.RequireRole;

@RestController
@RequestMapping("/students")
@Tag (name="学生管理")
public class StudentController {
    private final StudentService studentService;
    public StudentController(StudentService studentService){
        this.studentService=studentService;
    }

    @Operation (summary = "查询详细学生")
    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id){
       return Result.success(studentService.findById(id));
    }

    @Operation (summary="分页查询学生")
    @GetMapping
    public Result findPage(
            @RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="10") int size,
            @RequestParam(required=false) String name){
                return Result.success(studentService.findPage(page,size,name));
            }

    @RequireRole ("ADMIN")
    @Operation (summary = "新增学生")
    @PostMapping
    public Result insert(@Valid @RequestBody Student student,HttpServletRequest request){
        return Result.success(studentService.insert(student));
    }

    @RequireRole("ADMIN")
    @Operation (summary = "修改学生")
    @PutMapping
    public Result update(@Valid @RequestBody Student student,HttpServletRequest request){
        return Result.success(studentService.update(student));
    }
    
    @RequireRole("ADMIN")
    @Operation (summary = "删除学生")
    @DeleteMapping("/{id}")
    public Result delete(@Valid @PathVariable Integer id,HttpServletRequest request){
        return Result.success(studentService.delete(id));
    }
}
