package com.itbaizhan.shopping_common.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 区/县
 */
@Data
@TableName("bz_area")
public class Area implements Serializable{
    @TableId
    private String id; // 区/县Id
    private String area; // 区/县名
    private String cityId; // 城市Id

}