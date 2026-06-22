/**
 * 全局异常处理器 —— 统一拦截所有 Controller 抛出的异常。
 *
 * Spring 的 @RestControllerAdvice 会在每个 @RestController 方法执行前后进行拦截。
 * 如果在 Controller 中抛了异常，就会被这里对应的方法捕获，包装成统一的 Result 格式返回。
 *
 * 为什么要用全局处理？
 * 1. 避免每个 Controller 方法里写 try-catch（代码重复）
 * 2. 保证所有异常都返回统一的 JSON 格式 {code, message, data}
 * 3. 分离业务逻辑和错误处理（Controller 只写正常逻辑，异常统一在这处理）
 *
 * 工作流程：Controller 抛异常 → @RestControllerAdvice 拦截 → 匹配 @ExceptionHandler → 返回 Result
 *
 * 类似 C++ 中的 try-catch 统一放在最外层，而不是每个函数都写一遍。
 *
 * @author 徐磊
 */
package com.supermarket.common;

import org.springframework.dao.DataIntegrityViolationException; // 数据库约束冲突（唯一键、外键等）
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; // Spring Security 权限不足异常
import org.springframework.validation.FieldError;               // 字段校验错误详情
import org.springframework.web.bind.MethodArgumentNotValidException; // @Valid 校验失败异常
import org.springframework.web.bind.annotation.ExceptionHandler;     // 标记方法为异常处理器
import org.springframework.web.bind.annotation.RestControllerAdvice; // 标记类为全局异常拦截器

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException; // 找不到元素异常

/**
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * 表示这个类的所有方法返回值都会自动序列化为 JSON。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 业务异常处理 ====================

    /**
     * 参数校验失败 —— 处理 @Valid 注解触发的校验异常。
     *
     * 触发场景：Controller 中 @Valid 校验请求体时，字段不满足 @NotBlank/@NotNull 等约束。
     * 例如：登录时用户名为空，注册时手机号格式不对。
     *
     * @param ex MethodArgumentNotValidException，包含所有校验失败的字段和错误消息
     * @return 400 状态码 + 字段级别的错误详情 Map<字段名, 错误消息>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        // 构造字段错误映射：{ "username": "用户名不能为空", "password": "密码长度至少6位" }
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        Result<Map<String, String>> result = Result.error(400, "参数校验失败", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 资源不存在 —— 处理 findById().orElseThrow() 抛出的异常。
     *
     * 触发场景：根据 ID 查询时，数据库中找不到对应记录。
     * 例如：GET /api/products/999，但 ID=999 的商品不存在。
     *
     * @param ex NoSuchElementException
     * @return 404 状态码
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Result<Void>> handleNotFound(NoSuchElementException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "资源不存在";
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.notFound(message));
    }

    /**
     * 权限不足 —— 处理 Spring Security 的 @PreAuthorize 或角色校验失败异常。
     *
     * 触发场景：收银员试图访问管理员专属接口。
     *
     * @param ex AccessDeniedException
     * @return 403 状态码
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.forbidden("权限不足，无法访问该资源"));
    }

    /**
     * 数据库约束冲突 —— 处理唯一键重复、外键约束等。
     *
     * 触发场景：
     * 1. 插入重复的用户名/条形码/会员卡号
     * 2. 删除被其他表引用的记录（外键约束）
     *
     * @param ex DataIntegrityViolationException
     * @return 400 状态码
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Result<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        // 解析异常消息，提取有用信息
        String message = "数据操作异常，请检查输入信息是否重复或关联数据是否存在";
        String exMsg = ex.getMessage() != null ? ex.getMessage() : "";
        if (exMsg.contains("Duplicate")) {
            message = "数据重复，该记录已存在";
        } else if (exMsg.contains("foreign key") || exMsg.contains("constraint")) {
            message = "存在关联数据，无法删除";
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.badRequest(message));
    }

    /**
     * 非法参数异常 —— 处理业务逻辑中的参数错误。
     *
     * 触发场景：Service 层手动抛出的 IllegalArgumentException。
     * 例如：结账时库存不足、会员卡已过期等业务校验失败。
     *
     * @param ex IllegalArgumentException
     * @return 400 状态码
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.badRequest(ex.getMessage()));
    }

    /**
     * 未知异常兜底处理 —— 捕获所有未被上面具体处理器匹配的异常。
     *
     * 这是最后一道防线，防止未预期的异常直接暴露给前端（如 NullPointerException）。
     * 生产环境中应该同时打印日志，方便排查问题。
     *
     * @param ex Exception 通用异常
     * @return 500 状态码
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleGeneral(Exception ex) {
        // TODO: 生产环境应使用日志框架记录 ex 堆栈
        String message = "服务器内部错误: " + ex.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.serverError(message));
    }
}
