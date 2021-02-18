package doraemon.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 页面响应entity.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result implements Serializable {

    private int code;
    private String msg;

    public static Result ok(Object obj) {
        return ok(200, obj.toString());
    }
    public static Result ok() {
        return ok(200, "运行成功!");
    }

    public static Result ok(String msg) {
        return ok(200, msg);
    }

    public static Result ok(int code, String msg) {
        return new Result(code, msg);
    }

    public static Result error(Object obj) {
        return error(500, obj.toString());
    }

    public static Result error(String msg) {
        return error(500, msg);
    }

    public static Result error(int code, String msg) {
        return new Result(code, msg);
    }

    // /**
    //  * 没有参数就默认未知异常.
    //  */
    // public static Result error() {
    //     return error(500, "未知异常，请联系管理员");
    // }
    // // 感觉不优美，可能要改
    // public static Result error(String msg) {
    //     return error(500, msg);
    // }
    //
    // public static Result error(int code, String msg) {
    //     Result r = new Result();
    //     r.put("code", code);
    //     r.put("msg", msg);
    //     return r;
    // }
    // public static Result error(Object msg) {
    //     Result r = new Result();
    //     r.put("msg", msg);
    //     return r;
    // }
    // public static Result ok(Object msg) {
    //     Result r = new Result();
    //     r.put("msg", msg);
    //     return r;
    // }
    //
    //
    // public static Result ok(Map<String, Object> map) {
    //     Result r = new Result();
    //     r.putAll(map);
    //     return r;
    // }
    //
    // public static Result ok() {
    //     return new Result();
    // }
    //
    // @Override
    // public Result put(String key, Object value) {
    //     super.put(key, value);
    //     return this;
    // }

}
