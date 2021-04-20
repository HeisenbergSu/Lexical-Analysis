import java.io.*;
import java.util.ArrayList;
/*
处理了转义字符
 */
public class AnalysisTokens {


    public static String[] keywords = new String[]{"struct","float","boolean","short","long","double","int8","int16","int32","int64","unit8",
            "unit16","unit32","unit64","char","unsigned"};
    public static String[] booleans = new String[]{"TRUE","FALSE"};
    //开始，ID，NUM,STRING,0,带下划线的ID，结束，错误，右移位，左移位
    public static enum STATE {START,INID,INNUM,STRING,ZERO,IN_ID,DONE,ERROR,GT,LT};
    public static enum TokenType {ID,LETTER,STRING,DIGIT,INTEGER,BOOLEAN,KEYWORD,ASSIGN};


    /**
     *
     * @param c
     *            char
     * @return if char is letter
     */
    public static Boolean isLetter(char c){
        //[a-z]|[A-Z]
        if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))
            return true;
        return false;
    }
    /**
     *
     * @param c
     *            char
     * @return if char is digit
     */
    public static Boolean isDigit_with_0(char c){

        if (c >= '0' && c <= '9')
            return true;
        return false;
    }
    /**
     *
     * @param c
     *            char
     * @return if char is digit excpet 0
     */
    public static Boolean isDigit_without_0(char c){

        if (c >= '1' && c <= '9')
            return true;
        return false;
    }
    /**
     *
     * @param c
     *          char
     * @return if it is ESCAPE_SEQUENCE
     */
    public static Boolean isESCAPE_SEQUENCE(char c){

        if (c == 'b' || c == 't' || c == 'n' || c == 'f' || c == 'r' || c == '"' || c == '\\')
            return true;
        return false;
    }
    /**
     *
     * @param c
     *          char
     * @return if it is char or digit
     */
    public static Boolean isLetterOrDigit(char c){

        if(isDigit_with_0(c) || isLetter(c)){
            return true;
        }
        return  false;
    }
    /**
     *
     * @param c
     *          char
     * @return if it is assign
     */
    public static Boolean isAssign(char c){

        if (c == '{' || c == '}' || c == ';' || c == '[' || c == ']' || c == '*' || c == '+' || c == '-' || c == '~'
                || c == '/' || c == '%' || c == '&' || c == '^' || c == '|' || c == ',')
            return true;
        return false;
    }
    /**
     * main
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException{
        //init();
        TokenType tokenType = null;
        String filename = "test_9_complex_with_escape.txt";

        FileWriter writer = new FileWriter("./answer_output/test_9_output.txt");
        //读取输入的文件

        File file = new File(filename);
        FileReader filereader = new FileReader(file);
        StringBuffer buffer = new StringBuffer();
        // 向output.txt写，\r\n是Windows下txt换行
        //writer.write("read file" + input + "\r\n");
        BufferedReader reader = new BufferedReader(filereader);
        //行号
        int lineNum = 0;
        String currentLine = new String();

        while((currentLine  = reader.readLine()) != null){
            System.out.println(currentLine);
            lineNum++;
            //writer.write("Line : " + lineNum +"\r\n");
            //writer.flush();
            //字符串转换成字符数组
            char[] charArr = currentLine.toCharArray();
            //遍历字符数组
            int i = 0;
            System.out.println("Line Length:"+currentLine.length());
            //开始处理一行的字符
            while(i < currentLine.length()){

                STATE state = STATE.START;
                //状态不为DONE
                while(!state.equals(STATE.DONE)){
                    switch (state){

                        case ERROR:
                            //输出错误行号
                            System.out.println("ERROR");
                            writer.write("#########错误出现在第" + lineNum + "行#########\r\n");
                            writer.flush();
                            state = STATE.DONE;

                        case START:
                            if(i < currentLine.length()){
                                if (charArr[i] == ' ' || charArr[i] == '\t' ) {
                                    // start state
                                    System.out.print("START -> ");
                                    state = STATE.START;
                                }else if(isLetter(charArr[i])){
                                    System.out.print("START -> ");
                                    buffer.append(charArr[i]);
                                    state = STATE.INID;
                                    tokenType = TokenType.ID;
                                }else if(charArr[i] == '0'){
                                    buffer.append(charArr[i]);
                                    state = STATE.ZERO;
                                    tokenType = TokenType.INTEGER;
                                }else if(isDigit_without_0(charArr[i])){
                                    buffer.append(charArr[i]);
                                    state = STATE.INNUM;
                                    tokenType = TokenType.INTEGER;
                                }else if(charArr[i] == '"'){
                                    buffer.append(charArr[i]);
                                    state = STATE.STRING;
                                    tokenType = TokenType.STRING;
                                }else if(isAssign(charArr[i])){
                                    //buffer.append(charArr[i]);
                                    state = STATE.DONE;
                                    tokenType = TokenType.ASSIGN;
                                    writer.write(charArr[i]+"\t\t: "+tokenType+"\r\n");
                                    writer.flush();
                                }else if(charArr[i] == '<'){
                                    buffer.append(charArr[i]);
                                    state = STATE.LT;
                                    tokenType = TokenType.ASSIGN;
                                }else if(charArr[i] == '>'){
                                    buffer.append(charArr[i]);
                                    state = STATE.GT;
                                    tokenType = TokenType.ASSIGN;
                                }else{
                                    writer.write("error type : unknown symbol\r\n");
                                    state = STATE.ERROR;
                                }
                                i++;
                            }else
                                break;
                            break;
                        case INID:
                            if (i < currentLine.length()){
                                if(charArr[i] == '_'){
                                    System.out.print("INID -> ");
                                    buffer.append("_");
                                    state = STATE.IN_ID;
                                }else if(isLetterOrDigit(charArr[i])){
                                    System.out.print("INID -> ");
                                    buffer.append(charArr[i]);
                                    state = STATE.INID;
                                }else {
                                    //根据最长匹配，认定前文字符为正确字符，所以将token输出
                                    String word = String.valueOf(buffer);
                                    //System.out.print(word);
                                    boolean isKeyword = false;
                                    boolean isBoolean = false;
                                    for(String s : keywords){
                                        if(s.equalsIgnoreCase(word)){
                                            isKeyword = true;
                                            writer.write(word+"\t\t: KEYWORD"   + "\r\n");
                                            writer.flush();
                                            break;
                                        }
                                    }
                                    for(String s : booleans){
                                        if(s.equalsIgnoreCase(word)){
                                            isBoolean = true;
                                            writer.write(s+"\t\t: BOOLEAN"   + "\r\n");
                                            writer.flush();
                                            break;
                                        }
                                    }
                                    if(!isKeyword && !isBoolean){
                                        writer.write(String.valueOf(buffer)+"\t\t: " + tokenType + "\r\n");
                                        writer.flush();
                                    }
                                    buffer = new StringBuffer();
                                    state = STATE.DONE;
                                    i--;
                                }
                                i++;
                            }else{
                                break;
                            }
                            break;
                        case IN_ID:
                            if (i < currentLine.length()){
                                if (isLetterOrDigit(charArr[i])){
                                    System.out.print("IN_ID -> ");
                                    buffer.append(charArr[i]);
                                    state = STATE.INID;
                                }else{
                                    //如果下划线后接的不是字母或数字则报错
                                    buffer = new StringBuffer();
                                    writer.write(
                                            "error type : there must be at least one letter or digit behind underline \r\n");
                                    writer.flush();
                                    state = STATE.ERROR;
                                }
                                i++;
                            }else
                                break;
                            break;
                        case INNUM:
                            if (i < currentLine.length()){

                                if(isDigit_with_0(charArr[i])){
                                    buffer.append(charArr[i]);
                                    state = STATE.INNUM;
                                }else if(charArr[i] == 'l' || charArr[i] == 'L'){
                                    buffer.append(charArr[i]);
                                    writer.write(String.valueOf(buffer) + "\t\t: " + tokenType+"\r\n");
                                    writer.flush();
                                    state = STATE.DONE;
                                }else{
                                    //根据最长匹配，认定前文字符为正确字符，所以将token输出
                                    String word = String.valueOf(buffer);
                                    writer.write(String.valueOf(buffer)+"\t\t: " + tokenType + "\r\n");
                                    writer.flush();
                                    buffer = new StringBuffer();
                                    state = STATE.DONE;
                                    i--;
                                }
                                i++;
                            }else
                                break;
                            break;
                        case STRING:
                            if (i < currentLine.length()){
                                if(charArr[i] == '\\'){
                                    //如果遇到转义字符标志\，先存进buffer
                                    buffer.append(charArr[i]);
                                    if(!isESCAPE_SEQUENCE(charArr[i++])) {
                                        //如果下一个char不属于转义字符中的一种，就清空buffer，当成遇到错误情况
                                        buffer = new StringBuffer();
                                        writer.write("Line : " + lineNum + " error type\t\t: invalid string \r\n");
                                        writer.flush();
                                        state = STATE.ERROR;
                                    }
                                    //恢复i位置
                                    i--;
                                    //如果下一个char属于转义字符中的一种，就正常加入到buffer中，继续读
                                    if(isESCAPE_SEQUENCE(charArr[i++])){
                                        buffer.append(charArr[i]);
                                        state = STATE.STRING;
                                    }

                                }else if(charArr[i] == '"'){
                                    //遇到下一个双引号，视为结束一个String的读取
                                    buffer.append(charArr[i]);
                                    writer.write(String.valueOf(buffer) + "\t\t: " + tokenType+"\r\n");
                                    writer.flush();
                                    buffer = new StringBuffer();
                                    state = STATE.DONE;
                                }else{
                                    //其他字符则继续读下一个
                                    buffer.append(charArr[i]);
                                    state = STATE.STRING;
                                }
                                i++;
                            }else{
                                // 一直到本行结束都没有出现“，判定为错误的String格式
                                buffer = new StringBuffer();
                                writer.write("error type : Wrong String type \r\n");
                                writer.flush();
                                state = STATE.ERROR;
                            }
                            break;
                        case ZERO:
                            if (i < currentLine.length()){
                                if(charArr[i] == 'l' || charArr[i] == 'L'){
                                    buffer.append(charArr[i]);
                                    writer.write(String.valueOf(buffer) + "\t\t: " + tokenType+"\r\n");
                                    writer.flush();
                                    state = STATE.DONE;
                                }else{
                                    //根据最长匹配，认定前文字符为正确字符，所以将token输出
                                    String word = String.valueOf(buffer);
                                    writer.write(String.valueOf(buffer)+"\t\t: " + tokenType + "\r\n");
                                    writer.flush();
                                    buffer = new StringBuffer();
                                    state = STATE.DONE;
                                    i--;
                                }
                                i++;
                            }else
                                break;
                            break;
                        case GT:
                            if (i < currentLine.length()){
                                if(charArr[i] == '>'){
                                    buffer.append(charArr[i]);
                                    writer.write(String.valueOf(buffer) + "\t\t: " + tokenType+"\r\n");
                                    writer.flush();
                                    buffer = new StringBuffer();
                                    state = STATE.DONE;
                                }else{
                                    buffer = new StringBuffer();
                                    writer.write("error type : invalid assign \r\n");
                                    writer.flush();
                                    state = STATE.ERROR;
                                }
                                i++;
                            }else
                                break;
                            break;
                        case LT:
                            if (i < currentLine.length()){
                                if(charArr[i] == '<'){
                                    buffer.append(charArr[i]);
                                    writer.write(String.valueOf(buffer) + "\t\t: " + tokenType+"\r\n");
                                    writer.flush();
                                    buffer = new StringBuffer();
                                    state = STATE.DONE;
                                }else{
                                    buffer = new StringBuffer();
                                    writer.write("error type : invalid assign \r\n");
                                    writer.flush();
                                    state = STATE.ERROR;
                                }
                                i++;
                            }else
                                break;
                            break;
                        case DONE:

                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + state);
                    }
                }
            }
        }
    }
}
