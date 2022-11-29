package com.wjb.execl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.WorkbookUtil;
import lombok.Data;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GenerateSqlUtil {

    public static void main(String[] args) throws IOException {
        GenerateSqlUtil generateSqlUtil = new GenerateSqlUtil();
        generateSqlUtil.generateSql();
    }

    /**
     * 生成sql文件
     */
    public void generateSql() throws IOException {
        System.out.println("开始生成SQL=========================");
        //Map<String, Object> map = readExcel(null,null,"用户表"); //读取单个指定sheet数据
        List<Map<String, Object>> result = readExcel();
        System.out.println("==============共"+result.size()+"张表===============");
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> map : result) {
            List<ExcelEntity> list = (List<ExcelEntity>) map.get("list");
            int size = list.size();
            //表名
            String tableName = (String) map.get("tableName");
            //注释
            String tableNotes = (String) map.get("tableNotes");
            sb.append(String.format("-- %s：%s\n", tableName,tableNotes));
            sb.append(String.format("DROP TABLE IF EXISTS %s;\n", tableName));
            sb.append(String.format("CREATE TABLE %s (\n", tableName));
            for (int i = 0; i < size; i++) {
                ExcelEntity entity = list.get(i);
                if(entity.getFiledType()==null || entity.getFiledType()==""){
                    continue;
                }

                String fieldTypeByDesc = FiledTypeEnum.getFieldTypeByDesc(entity.getFiledType().trim(), entity.getLength());
                System.out.println(entity.getFiledName()+" "+entity.getFiledType()+" "+fieldTypeByDesc);

                sb.append(String.format("\t%s %s", entity.getFiledName(),FiledTypeEnum.getFieldTypeByDesc(entity.getFiledType().trim(),entity.getLength())));
                String type = entity.getFiledType();
                String notes = entity.getNotes();
                String constraint = entity.getConstraint();
                //设置主键
                if (notes.contains("主键")){
                    sb.append(" PRIMARY KEY");
                    sb.append(" AUTO_INCREMENT");
                }else {
                    //字符串、文本设置编码
                    //主键类型为字符时，不设置编码
                    if (type.contains("char") || type.contains("text")){
                        sb.append(" CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                    }
                }


                if (constraint.contains("M")){
                    if ("SJGXSJ".equalsIgnoreCase(entity.getFiledName())){
                        sb.append(" NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ");
                    }else if ("SJCJSJ".equalsIgnoreCase(entity.getFiledName())){
                        sb.append(" NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ");
                    }else {
                        sb.append(" NOT NULL COMMENT ");
                    }
                }else {
                    sb.append(" NULL DEFAULT NULL COMMENT ");
                }
                sb.append(String.format("'%s'",entity.getNotes()));
                if (i < size-1){
                    sb.append(",");
                }
                sb.append("\n");
            }
            sb.append(String.format(") ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '%s' ROW_FORMAT = Dynamic;",tableNotes));
            sb.append("\n\n");
        }
        //sql输出的位置
        File file=new File("C:\\Users\\Admin\\Desktop\\sql.sql");
        FileOutputStream fos1=new FileOutputStream(file);
        OutputStreamWriter dos1=new OutputStreamWriter(fos1);
        dos1.write(sb.toString());
        dos1.close();
        System.out.println("SQL生成完成=========================");
    }

    /**
     * 读取指定sheet的数据，获取表名，字段名、字段类型、字段注释、属性
     */
    public Map<String,Object> readExcel(File file,Workbook book,String sheetName) {
        Map<String,Object> map = new HashMap<>();
        List<ExcelEntity> list = new ArrayList<>();
        if (file == null){
            file = new File("C:\\Users\\Admin\\Desktop\\1.xlsx");
        }
        if (book == null){
            //WorkbookUtil hutool的工具类
            book = WorkbookUtil.createBook(file);
        }
        Sheet sheet = book.getSheet(sheetName);
        //第一行是表头，默认是 注释:表名
        Row titleRow = sheet.getRow(0);
        String title = titleRow.getCell(0).toString();
        map.put("tableName",title.split(":")[1]);
        System.out.println("处理表"+title);
        map.put("tableNotes",title.split(":")[0]);
        //第二行是标题，不读取。第三行开始才是数据
        int lastRowNum = sheet.getLastRowNum();
        for(int i = 2; i <= lastRowNum; i++){
            list.add(getEntity(sheet.getRow(i)));
        }
        map.put("list",list);
        return map;
    }

    /**
     * 读取多个sheet的数据
     */
    public List<Map<String,Object>> readExcel() {
        List<Map<String,Object>> result = new ArrayList<>();
        File file = new File("src/main/resources/excel/城市容貌.xlsx");
        //File file = new File("src/main/resources/excel/城市建设-排水信息.xlsx");
        //File file = new File("src/main/resources/excel/环境卫生-建筑垃圾监管.xlsx");
        //File file = new File("src/main/resources/excel/环境卫生-垃圾处理设施.xlsx");
        //File file = new File("src/main/resources/excel/环境卫生-生活垃圾分类监管.xlsx");
        //File file = new File("src/main/resources/excel/环境卫生-城市公厕监管.xlsx");
        Workbook book = WorkbookUtil.createBook(file);
        int sheetNum = book.getNumberOfSheets();
        for (int i = 0; i < sheetNum; i++) {
            String sheetName = book.getSheetName(i);
            result.add(readExcel(file,book,sheetName));
        }
        return result;
    }

    private ExcelEntity getEntity(Row row){

        ExcelEntity entity = new ExcelEntity();
        //列数：6列，分别是字段名称、字段代码、字段类型、字段长度、约束条件、说明
        for (int j = 0; j < 5; j++) {
            if (row!=null && row.getCell(j)!=null){
                String str = row.getCell(j).toString();
                switch (j){
                    case 0:
                        entity.setNotes((str+" "+row.getCell(5).toString()).replace("\r","").replace("\n",""));
                        break;
                    case 1:
                        entity.setFiledName(str);
                        break;
                    case 2:
                        entity.setFiledType(str);
                        break;
                    case 3:
                        entity.setLength((str.trim()!=null && str.trim()!="")? Integer.parseInt(str.trim().replace(".0","")):null);
                        break;
                    case 4:
                        entity.setConstraint(str);
                        break;
                }
            }
        }
        return entity;
    }
}