package com.itbaizhan.shopping_common.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 省份
 */
@Data
@TableName("bz_province")
public class Province implements Serializable{
    @TableId
    private String id; // 省份id
    private String provinceName; // 省份名
}