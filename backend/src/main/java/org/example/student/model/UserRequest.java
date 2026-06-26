package org.example.student.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 新增/修改用户请求参数。
 */
public class UserRequest {
    public interface Create {}

    public interface Update {}

    /** 账号。 */
    @NotBlank(message = "账号不能为空", groups = {Create.class, Update.class})
    @Size(max = 50, message = "账号不能超过50个字符", groups = {Create.class, Update.class})
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "账号只能包含字母、数字和下划线", groups = {Create.class, Update.class})
    private String username;

    /** 姓名。 */
    @NotBlank(message = "姓名不能为空", groups = {Create.class, Update.class})
    @Size(max = 50, message = "姓名不能超过50个字符", groups = {Create.class, Update.class})
    private String displayName;

    /** 手机号。 */
    @NotBlank(message = "手机号不能为空", groups = {Create.class, Update.class})
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式错误", groups = {Create.class, Update.class})
    private String phone;

    @Size(max = 500, message = "头像地址不能超过500个字符", groups = {Create.class, Update.class})
    private String avatarUrl;

    /** 密码。新增时必填，修改时可选填。 */
    @NotBlank(message = "密码不能为空", groups = Create.class)
    @Size(max = 100, message = "密码不能超过100个字符", groups = {Create.class, Update.class})
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
