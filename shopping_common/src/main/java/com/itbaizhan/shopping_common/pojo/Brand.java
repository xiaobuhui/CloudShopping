package com.itbaizhan.shopping_common.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 品牌
 */
@Data
@TableName("bz_brand")
public class Brand implements Serializable{
    @TableId
    private Long id; // 品牌 id
    private String name; // 品牌名称
}