package com.lj.controller;

import com.lj.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

/**
 * 通用操作
 */
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {
    @Value("${reigie.basepath}")
    private String basePath;
    
    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        //生成文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + suffix;
        //判断下载目录是否存在,不存在,创建目录
        File dir = new File(basePath);
        if (!dir.exists()){
            dir.mkdirs();//创建目录
        }
        //将上传的文件保存到目录
        try {
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //上传成功,将生成的文件名返回客户端
        return R.success(fileName);
    }
    
    /**
     * 文件下载
     * @param name
     * @param response
     * @return
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        try {
            //创建输入流
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(basePath + name)));
            //设置响应内容类型
            response.setContentType("image/jpeg");
            //创建输出流
            OutputStream os = response.getOutputStream();
            int len = 0;
            //每次读取的大小 1kb
            byte[] bytes = new byte[1024];
            //拷贝文件
            while((len = bis.read(bytes)) != -1){
                os.write(bytes,0,len);
                os.flush();
            }
            //关闭资源
            os.close();
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
