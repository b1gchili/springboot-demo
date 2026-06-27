package org.example.student.model;

/**
 * 文件上传成功后的响应数据。
 */
public class UploadResponse {
    private String url;

    public UploadResponse() {
    }

    public UploadResponse(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
