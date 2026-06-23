/**
 * 全局异常处理器 —— 统一拦截所有 Controller 抛出的异常。
 *
 * Spring 的 @RestControllerAdvice 会在每个 @RestController 方法执行前后进行拦截。
 * 如果在 Controller 中抛了异常，就会被这里对应的方法捕获，包装成统一的 Result 格式返回。
 *
 * @author 徐磊
 */
package com.supermarket.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @Slf4j：Lombok 日志注解，自动生成 log 对象，用于记录异常堆栈。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 业务异常处理 ====================

    /** 参数校验失败 —— @Valid 校验失败 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        log.warn("参数校验失败: {}", errors);
        Result<Map<String, String>> result = Result.error(400, "参数校验失败", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /** 资源不存在 */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Result<Void>> handleNotFound(NoSuchElementException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "资源不存在";
        log.warn("资源不存在: {}", message);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.notFound(message));
    }

    /** 权限不足 */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("权限不足: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.forbidden("权限不足，无法访问该资源"));
    }

    /** 认证失败（Spring Security 的 AuthenticationException 及其子类） */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result<Void>> handleAuthentication(AuthenticationException ex) {
        log.warn("认证失败: {}", ex.getMessage());
        // 不暴露具体的认证失败原因（如"用户不存在"），只返回通用消息
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.unauthorized("用户名或密码错误"));
    }

    /** 数据库约束冲突 */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Result<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("数据库约束冲突", ex);
        String message = "数据操作异常，请检查输入信息是否重复或关联数据是否存在";
        String exMsg = ex.getMessage() != null ? ex.getMessage() : "";
        if (exMsg.contains("Duplicate")) {
            message = "数据重复，该记录已存在";
        } else if (exMsg.contains("foreign key") || exMsg.contains("constraint")) {
            message = "存在关联数据，无法删除";
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.badRequest(message));
    }

    /** 业务逻辑参数错误 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleBadRequest(IllegalArgumentException ex) {
        log.warn("业务参数错误: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.badRequest(ex.getMessage()));
    }

    /**
     * 未知异常兜底处理 —— 最后一道防线。
     *
     * 安全要点：
     *   - 客户端只看到通用的"服务器内部错误"，不暴露任何内部实现细节。
     *   - 完整的异常堆栈通过 log.error() 记录到服务器日志，供排查使用。
     *   - 防止攻击者通过异常消息收集系统信息（如数据库结构、文件路径等）。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleGeneral(Exception ex) {
        // 记录完整的异常堆栈到服务器日志
        log.error("未预期的服务器内部错误", ex);
        // 返回通用错误消息，不暴露 ex.getMessage()
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Result.serverError("服务器内部错误，请稍后再试"));
    }
}
