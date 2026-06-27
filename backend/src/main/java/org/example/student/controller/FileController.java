package org.example.student.controller;

import org.example.student.model.Result;
import org.example.student.model.UploadResponse;
import org.example.student.oss.AliyunOssService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final AliyunOssService aliyunOssService;

    public FileController(AliyunOssService aliyunOssService) {
        this.aliyunOssService = aliyunOssService;
    }

    /**
     * 上传用户头像到阿里云 OSS。
     */
    @PostMapping("/avatar")
    public Result<UploadResponse> uploadAvatar(@RequestPart("file") MultipartFile file) {
        String url = aliyunOssService.uploadAvatar(file);
        return Result.success(new UploadResponse(url));
    }
}
