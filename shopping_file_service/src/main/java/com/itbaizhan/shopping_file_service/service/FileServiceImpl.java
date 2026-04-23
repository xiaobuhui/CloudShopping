package com.itbaizhan.shopping_file_service.service;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.itbaizhan.shopping_common.result.BusException;
import com.itbaizhan.shopping_common.result.CodeEnum;
import com.itbaizhan.shopping_common.service.FileService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;

@DubboService
//我们这个上传图片不上传到数据库，不用开启事务
public class FileServiceImpl implements FileService {
    //FastDFS 官方提供的 Java 客户端工具
    @Autowired
    private FastFileStorageClient fastFileStorageClient;
    // Nginx访问FastDFS中文件的路径，我们在nacos中配置了，
    // 再拼接上文件名即可上传
    @Value("${fdfs.fileUrl}")
    private String fileUrl;
    @Override
    public String uploadImage(byte[] fileBytes, String fileName) {
        if (fileBytes.length != 0) {
            try {
                // 1.将文件的字节数组转为输入流，因为FastDFS要求上传必须传流
                ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);

                // 2.获取文件的后缀名
                /*这个lastIndexOf是返回最后一个.的下标，以cat.png为例子返回3，
                * substring是从输入数字的下标开始截取，包括下标本身
                * cat.png的.下标为3，+1为4，刚好对应p，直接截取文件后缀*/
                String fileSuffix = fileName.substring(fileName.lastIndexOf(".") + 1);

                // 3.上传文件,通过fdfs对象
                /*inputStream,   // 文件流
                  inputStream.available(), // 文件大小
                  fileSuffix,    // 文件后缀
                  null           // 元数据（不用传）
                  返回的StorePath对象包含了文件存在 FastDFS 里的路径*/
                StorePath storePath = fastFileStorageClient.uploadFile(inputStream, inputStream.available(), fileSuffix, null);

                // 4.返回图片路径
                /*fileUrl：你配置的 http://192.168.xx.xx:xx
                storePath.getFullPath()：返回 group1/M00/00/00/xxx.jpg
                拼接后的url能通过nginx直接访问到*/
                String imageUrl = fileUrl + "/"+storePath.getFullPath();
                return imageUrl;
            }catch (Exception e){
                throw new BusException(CodeEnum.UPLOAD_FILE_ERROR);
            }
        } else {
            throw new BusException(CodeEnum.UPLOAD_FILE_ERROR);
        }
    }

    @Override
    public void delete(String filePath) {
        fastFileStorageClient.deleteFile(filePath);
    }
}
