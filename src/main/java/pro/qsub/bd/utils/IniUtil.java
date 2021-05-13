package pro.qsub.bd.utils;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author JZH
 * @desc
 * @time 2021-05-13-8:47
 */
public class IniUtil {
    /**
     * 这是个配置文件操作类，用来读取和设置ini配置文件
     *  @author  由月
     *  @version  2004-08-18
     */
    /**
     * 从ini配置文件中读取变量的值
     *  @param  file 配置文件的路径
     *  @param  section 要获取的变量所在段名称
     *  @param  variable 要获取的变量名称
     *  @param  defaultValue 变量名称不存在时的默认值
     *  @return  变量的值
     *  @throws  IOException 抛出文件操作可能出现的io异常
     */

    public static String getProfileString(String file, String section, String variable, String defaultValue)
            throws IOException {

        String strLine, value = "";
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        boolean isInSection = false ;
        try {
            while((strLine = bufferedReader.readLine()) != null) {
                strLine = strLine.trim();
                strLine = strLine.split("[;]")[0];
                Pattern p;
                Matcher m;
                p = Pattern.compile("file://[//s*.*//s*//]");
                m = p.matcher((strLine));
                if(m.matches()) {
                    p = Pattern.compile("file://[//s* " + section + " file://s*//]");
                    m = p.matcher(strLine);
                    if(m.matches()) {
                        isInSection = true;
                    } else {
                        isInSection = false;
                    }
                }

                if(isInSection == true) {
                    strLine = strLine.trim();
                    String[] strArray = strLine.split("=");
                    if(strArray.length == 1) {
                        value = strArray[0].trim();
                        if(value.equalsIgnoreCase(variable)) {
                            value = "";
                            return value;
                        }
                    } else if(strArray.length == 2) {
                        value = strArray[0].trim();
                        if(value.equalsIgnoreCase(variable)) {
                            value = strArray[1].trim();
                            return value;
                        }
                    } else if(strArray.length > 2) {
                        value = strArray[0].trim();
                        if(value.equalsIgnoreCase(variable)) {
                            value = strLine.substring(strLine.indexOf("=") + 1).trim();
                            return value;
                        }
                    }
                }
            }
        } finally {
            bufferedReader.close();
        }
        return defaultValue;
    }



    /**
     * 修改ini配置文件中变量的值
     *  @param  file 配置文件的路径
     *  @param  section 要修改的变量所在段名称
     *  @param  variable 要修改的变量名称
     *  @param  value 变量的新值
     *  @throws  IOException 抛出文件操作可能出现的io异常
     */
    public static boolean setProfileString(String file, String section, String variable, String value) {
        String fileContent, allLine, strLine, newLine, remarkStr;
        String getValue;
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        boolean isInSection = false ;
        fileContent = "";

        try {
            while((allLine = bufferedReader.readLine()) != null) {
                allLine = allLine.trim();
                if(allLine.split("[;]").length > 1) {
                    remarkStr=";"+ allLine.split(";")[1];
                } else {
                    remarkStr = "";
                }
                strLine = allLine.split(";")[0];

                strLine = allLine.trim();
                Pattern p;
                Matcher m;
                p = Pattern.compile("file://[//s*.*//s*//]");
                m = p.matcher((strLine));

                if(m.matches()) {
                    p = Pattern.compile("file://[//s*" + section + "file://s*//]");
                    m = p.matcher(strLine);
                    if(m.matches()) {
                        isInSection = true;
                    } else{
                        isInSection = false;
                    }
                }

                if(isInSection == true) {
                    strLine = strLine.trim();
                    String[] strArray = strLine.split("=");
                    getValue = strArray[0].trim();
                    if(getValue.equalsIgnoreCase(variable)) {
                        newLine = getValue + "=" + value + " " + remarkStr;
                        fileContent += newLine + "\r\n";
                        while((allLine = bufferedReader.readLine()) != null) {
                            fileContent += allLine + "\r\n";
                        }
                        bufferedReader.close();
                        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, false));
                        bufferedWriter.write(fileContent);
                        bufferedWriter.flush();
                        bufferedWriter.close();

                        return true;
                    }
                }

                fileContent += allLine + "\r\n";
            }
        } catch (IOException ie) {
            ie.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
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

    public static void main(String[] args) {
        IniUtil.writeFile("src/main/resources/sever.log","test",1);
    }

}
