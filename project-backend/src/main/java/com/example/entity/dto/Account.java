package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
@TableName("db_account")
public class Account {
    @TableId(type = IdType.AUTO)
    Integer id;
    String username;
    String password;
    String role;
    String email;
    Date registerTime;
}
