package com.wjb.execl;

import lombok.Data;

@Data
public  class ExcelEntity{
    /** 字段名称 */
    private String filedName;
    /** 字段类型 */
    private String filedType;
    /**字段长度**/
    private Integer length;
    /** 字段注释 */
    private String notes;
    /**
     * 约束条件
     */
    private String constraint;
}