package com.chennan.cloud.base.vo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 页面响应的类
 * @author chennan
 */
public class R extends LinkedHashMap<String, Object> {

    private static final long serialVersionUID = 1L;
    private static final String ERR_CODE   = "errcode";
    private static final String ERR_MSG    = "errmsg";
    private static final String ERR_DETAIL = "errdetail";

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 私有化构造函数,提供静态的"返回错误"和"返回成功"方法
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    private R() {
        super(8);
        //initialCapacity 默认16,用不了那么多. 根据建议为2的N次方,此处按照规范一般最多为6个,所以可设置为8.
    }
    public static R err(String errmsg) {
        return Objects.requireNonNull(Objects.requireNonNull(new R().put(ERR_CODE, -1))
                .put(ERR_MSG, StringUtils.isBlank(errmsg) ? "未知错误" : errmsg))
                .put(ERR_DETAIL, "");
    }
    public static R ok() {
        return Objects.requireNonNull(Objects.requireNonNull(new R().put(ERR_CODE, 0))
                .put(ERR_MSG, ""))
                .put(ERR_DETAIL, "");
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 取错误代码,错误信息,错误详细信息
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public int getErrCode() {
        return (int) this.get(ERR_CODE);
    }

    public String getErrMsg() {
        return (String) this.get(ERR_MSG);
    }

    public String getErrDetail() {
        return (String) this.get(ERR_DETAIL);
    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 增加错误代码,错误信息,错误详细信息,数据和枚举代码
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public R setErrCode(int errcode) {
        return this.put(ERR_CODE, errcode);
    }

    public R setErrMsg(String errmsg) {
        return this.put(ERR_MSG, errmsg);
    }

    public R setErrDetail(String errdetail) {
        return this.put(ERR_DETAIL, errdetail);
    }

    public <T> R  addData(T data) {
        if(data instanceof Page) {
            @SuppressWarnings("rawtypes")
            Page page = (Page) data;
            Map<String,Long> map = new LinkedHashMap<>();
            map.put("current", page.getCurrent());
            map.put("size", page.getSize());
            map.put("pages", page.getPages());
            map.put("total", page.getTotal());
            return Objects.requireNonNull(this.put("data", page.getRecords()))
                    .put("page", map);
        }else {
            return this.put("data", data);
        }
    }

    public R addCode(Map<String,Map<String,String>> codeMap) {
        if(codeMap == null) return this;
        return this.put("code", codeMap);
    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // 增加其他: 按照统一规范默认外部不应该调用这个接口,因此暂时添加过时接口作为警告提示
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    @Override
    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
