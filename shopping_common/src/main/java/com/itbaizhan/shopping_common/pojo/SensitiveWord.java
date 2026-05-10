package com.itbaizhan.shopping_common.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

@Data
public class SensitiveWord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String word;
    private String type; // "deny" 或 "allow"
}

