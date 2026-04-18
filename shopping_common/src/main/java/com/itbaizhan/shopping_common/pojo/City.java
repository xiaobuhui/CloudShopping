package com.itbaizhan.shopping_common.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 城市
 */
@Data
@TableName("bz_city")
public class City implements Serializable{
    @TableId
    private String id; // 城市id
    private String city; // 城市名
    private String provinceId; // 省份id
}