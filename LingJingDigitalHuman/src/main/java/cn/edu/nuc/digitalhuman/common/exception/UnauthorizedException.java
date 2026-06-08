package cn.edu.nuc.digitalhuman.common.exception;

/**
 * 未授权异常（token 无效或过期）
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
