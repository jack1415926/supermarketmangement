/**
 * 会员注册/编辑请求 DTO —— POST/PUT /api/members 的请求体。
 *
 * @author 徐磊
 */
package com.supermarket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MemberRequest {

    /** 会员姓名 */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50位")
    private String name;

    /**
     * 手机号。
     * @Pattern：正则校验。^1[3-9]\\d{9}$ 匹配中国大陆手机号格式。
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 身份证号（可选） */
    @Size(max = 18, message = "身份证号长度不能超过18位")
    private String idCard;

    /** 性别：男/女（可选） */
    private String gender;

    /** 会员卡号（可选，不填则系统自动生成） */
    @Size(max = 20, message = "卡号长度不能超过20位")
    private String cardNo;
}
