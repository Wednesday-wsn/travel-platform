package com.travel.controller.common;

import com.travel.common.exception.BusinessException;
import com.travel.common.result.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 通用接口：文件上传（任意已登录角色可访问）
 */
@RestController
@RequestMapping("/api/common")
public class FileController {

    @Value("${travel.upload.path}")
    private String uploadPath;

    @Value("${travel.upload.url-prefix}")
    private String urlPrefix;

    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        String original = file.getOriginalFilename();
        String ext = original != null && original.contains(".")
                ? original.substring(original.lastIndexOf("."))
                : ".jpg";
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
        try {
            Path dir = Paths.get(uploadPath);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            file.transferTo(dir.resolve(fileName));
        } catch (IOException e) {
            throw new BusinessException("文件上传失败");
        }
        return R.ok(urlPrefix + fileName);
    }
}
