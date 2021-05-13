package pro.qsub.bd.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author JZH
 * @desc
 * @time 2021-05-13-8:47
 */
public class IniUtil {
    /**
     * 去除ini文件中的注释，以";"或"#"开头，顺便去除UTF-8等文件的BOM头
     * @param source
     * @return
     */
    private static String removeIniComments(String source){
        String result = source;

        if(result.contains(";")){
            result = result.substring(0, result.indexOf(";"));
        }

        if(result.contains("#")){
            result = result.substring(0, result.indexOf("#"));
        }

        return result.trim();
    }



    public static Map<String,Object> readIni(String filename){
        Map<String, List<String>> listResult=new HashMap<>();
        Map<String,Object> result=new HashMap();
        String globalSection = "global";

        File file = new File(filename);
        BufferedReader reader=null;
        try {
            reader=new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String str = null;
            String currentSection = globalSection; //处理缺省的section
            List<String> currentProperties = new ArrayList<>();
            boolean lineContinued = false;
            String tempStr = null;

            //一次读入一行（非空），直到读入null为文件结束
            //先全部放到listResult<String, List>中
            while ((str = reader.readLine()) != null) {
                str = removeIniComments(str).trim(); //去掉尾部的注释、去掉首尾空格

                if("".equals(str)||str==null){
                    continue;
                }

                //如果前一行包括了连接符'\'
                if(lineContinued == true){
                    str = tempStr + str;
                }

                //处理行连接符'\'
                if(str.endsWith("\\")){
                    lineContinued = true;
                    tempStr = str.substring(0,str.length()-1);
                    continue;
                }else {
                    lineContinued = false;
                }

                //是否一个新section开始了
                if(str.startsWith("[") && str.endsWith("]")){
                    String newSection = str.substring(1, str.length()-1).trim();

                    //如果新section不是现在的section，则把当前section存进listResult中
                    if(!currentSection.equals(newSection)){
                        listResult.put(currentSection, currentProperties);
                        currentSection = newSection;

                        //新section是否重复的section
                        //如果是，则使用原来的list来存放properties
                        //如果不是，则new一个List来存放properties
                        currentProperties=listResult.get(currentSection);
                        if(currentProperties==null){
                            currentProperties = new ArrayList<>();
                        }
                    }
                }else{
                    currentProperties.add(str);
                }
            }
            //把最后一个section存进listResult中
            listResult.put(currentSection, currentProperties);

            reader.close();


        }catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }


        //整理拆开name=value对，并存放到MAP中：
        //从listResult<String, List>中，看各个list中的元素是否包含等号“=”，如果包含，则拆开并放到Map中
        //整理后，把结果放进result<String, Object>中
        for(String key : listResult.keySet()){
            List<String> tempList = listResult.get(key);

            //空section不放到结果里面
            if(tempList==null||tempList.size()==0){
                continue;
            }

            if(tempList.get(0).contains("=")){ //name=value对，存放在MAP里面
                Map<String, String> properties = new HashMap<>();
                for(String s : tempList){
                    int delimiterPos = s.indexOf("=");
                    //处理等号前后的空格
                    properties.put(s.substring(0,delimiterPos).trim(), s.substring(delimiterPos+1, s.length()).trim());
                }
                result.put(key, properties);
            }else{ //只有value，则获取原来的list
                result.put(key, listResult.get(key));
            }
        }

        return result;


    }

    /**
     * 写入内容
     * @param path
     * @param content
     * @param count
     * @return
     */
    public static void writeFile(String path,String content,int count){
        //如果写入100次
        if(count == 100 ){
            //清空文件
            clearInfoForFile(path);
            return;
        }
        File log;
        try {

            //没有文件就创建一个
            log = new File(path);

            if(!log.exists()){
                log.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(log,true);
            Writer out = new OutputStreamWriter(fileOutputStream,"utf-8");
            out.write(content);
            String newLine = System.getProperty("line.separator");
            //换行
            out.write(newLine);
            out.close();
            fileOutputStream.flush();
            fileOutputStream.close();
        }catch (Exception e){
            System.out.println("日志文件写入有误");
            e.printStackTrace();
        }
    }

    /**
     * 清空文件内容
     * @param fileName
     */
    public static void clearInfoForFile(String fileName) {
        File file =new File(fileName);
        try {
            if(!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter =new FileWriter(file);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
//        IniUtil.writeFile("src/main/resources/sever.log","",1);
        String IniPath = IniUtil.class.getClassLoader().getResource("sever.ini").getFile();
        Map<String, Object> stringObjectMap = IniUtil.readIni(IniPath);
        Map sever = (Map) stringObjectMap.get("sever");
        System.out.println("sever.get = " + sever);
        System.out.println(sever.get("logpath"));
    }

}
