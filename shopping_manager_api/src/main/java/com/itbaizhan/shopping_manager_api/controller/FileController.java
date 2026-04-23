package com.itbaizhan.shopping_manager_api.controller;

import com.itbaizhan.shopping_common.result.BaseResult;
import com.itbaizhan.shopping_common.service.FileService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件
 */
@RestController
@RequestMapping("/file")
public class FileController {
    @DubboReference
    private FileService fileService;

    /**
     * 上传文件
     * @param file 文件
     * @return 文件路径
     * @throws IOException
     */
    @PostMapping("/uploadImage")
    public BaseResult<String> upload(MultipartFile file) throws IOException {
        //前端传来的必定是MulpartFile对象
        //但MultipartFile对象不能在dubbo服务间传递，必须转为byte数组
        byte[] bytes = file.getBytes();
        String url = fileService.uploadImage(bytes, file.getOriginalFilename());
        return BaseResult.ok(url);
    }

    /**
     * 删除文件
     * @param filePath 文件路径
     * @return 操作结果
     */
    @DeleteMapping("/delete")
    public BaseResult delete(String filePath){
        fileService.delete(filePath);
        return BaseResult.ok();
    }
}
