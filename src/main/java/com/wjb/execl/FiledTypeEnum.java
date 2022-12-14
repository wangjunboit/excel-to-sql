package com.wjb.execl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.bcel.generic.INEG;

/**
 * @author wjb
 */
@AllArgsConstructor
@Getter
public enum FiledTypeEnum {
    VARCHAR("varchar","字符型",10),
    INT("int","整型",11),
    BIGINT("bigint","长整型",20),
    DATETIME("datetime","日期时间型",0),
    DATE("date","日期型",null),
    //DOUBLE("double","浮点型",10),
    DECIMAL("decimal","浮点型",16),
    ;
    /**
     * 字段类型
     */
    private String filedType;

    /**
     * 描述
     */
    private String desc;

    /**
     * 默认长度
     */
    private Integer defaultLength;


    public static String getFieldTypeByDesc(String desc, Integer length){
        for (FiledTypeEnum item : FiledTypeEnum.values()) {
            if (desc.trim().equals(item.desc)) {
                if (length==null){
                    length = item.getDefaultLength();
                }

                if (length==null){
                    return item.getFiledType();
                }
                if (item.getFiledType().equals(DECIMAL.getFiledType())){
                    return item.getFiledType()+"("+length+",6)";
                }else {
                    return item.getFiledType()+"("+length+")";
                }
            }
        }
        return "";
    }
}
