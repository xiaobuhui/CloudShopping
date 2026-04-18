package com.itbaizhan.shopping_common.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品图片
 */
@Data
@TableName("bz_goods_image")
public class GoodsImage implements Serializable {
    @TableId
    private Long id; // 图片id
    private String imageTitle; // 图片标题
    private String imageUrl; // 图片路径
    private Long goodsId; // 商品id
}
