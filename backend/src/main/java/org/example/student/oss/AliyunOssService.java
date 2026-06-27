package org.example.student.oss;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ObjectMetadata;
import org.example.student.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

/**
 * 阿里云 OSS 文件上传服务。
 */
@Service
public class AliyunOssService {
    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024L;

    private final AliyunOssProperties properties;

    public AliyunOssService(AliyunOssProperties properties) {
        this.properties = properties;
    }

    /**
     * 上传用户头像，返回可访问的图片 URL。
     */
    public String uploadAvatar(MultipartFile file) {
        validateFile(file);
        validateConfig();

        String objectName = buildObjectName(file.getOriginalFilename());
        OSS ossClient = new OSSClientBuilder().build(
                properties.getEndpoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret()
        );
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            ossClient.putObject(properties.getBucketName(), objectName, file.getInputStream(), metadata);
            return buildPublicUrl(objectName);
        } catch (OSSException | ClientException e) {
            throw new BusinessException(500, "头像上传到阿里云 OSS 失败");
        } catch (IOException e) {
            throw new BusinessException(500, "读取头像文件失败");
        } finally {
            ossClient.shutdown();
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的头像图片");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BusinessException(400, "头像图片不能超过5MB");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BusinessException(400, "头像文件必须是图片格式");
        }
    }

    private void validateConfig() {
        if (!StringUtils.hasText(properties.getEndpoint())
                || !StringUtils.hasText(properties.getAccessKeyId())
                || !StringUtils.hasText(properties.getAccessKeySecret())
                || !StringUtils.hasText(properties.getBucketName())) {
            throw new BusinessException(500, "阿里云 OSS 配置不完整");
        }
    }

    private String buildObjectName(String originalFilename) {
        String suffix = "";
        if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase(Locale.ROOT);
        }
        String dir = StringUtils.hasText(properties.getDir()) ? properties.getDir() : "avatars/";
        if (!dir.endsWith("/")) {
            dir = dir + "/";
        }
        return dir + UUID.randomUUID().toString().replace("-", "") + suffix;
    }

    private String buildPublicUrl(String objectName) {
        if (StringUtils.hasText(properties.getPublicUrlPrefix())) {
            return trimTrailingSlash(properties.getPublicUrlPrefix()) + "/" + objectName;
        }
        String endpoint = properties.getEndpoint()
                .replaceFirst("^https?://", "")
                .replaceFirst("/$", "");
        return "https://" + properties.getBucketName() + "." + endpoint + "/" + objectName;
    }

    private String trimTrailingSlash(String value) {
        return value.replaceFirst("/$", "");
    }
}
