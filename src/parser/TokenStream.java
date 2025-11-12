package parser;
import lexer.Token;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
//如果是新增一个词法的类型，lexertype要新增，analyzer里面可能要新增chartype枚举类，
// 更可能是要在getchartype函数里面新增符号。然后修改状态机的handle方法。
// parser部分这时候要改这个新增词法用到的语句，对应的parse方法,然后对应的node类构造方法和show方法都要改
// 或许还有其他要改的，估计需要debug才能看出来.
// 如果是那种给stmt新增一种文法大概率不需要有新的词法元素，不过也小心原本文法没有？或者：等，比如那个三元式也得新增他俩，
// 主要是修改对应的node的构造方法和show以及parse里面parsestmt以及对应分支的parsexxstmt，
// 注意可能需要回溯的情况，以及大概率要新增bool变量来进行分支判断
public class TokenStream {
    private List<Token> tokenList;
    private Token nowToken;
    private Token nextToken;
    private int index=0;//tokenlist索引
    public TokenStream(){

    }
    //推进token
    public void advance(){
        if(index<tokenList.size()){
            nowToken= tokenList.get(index);
            if(index+1<tokenList.size()){
                nextToken = tokenList.get(index+1);
                index++;
            }else{
                nextToken = null;
                index++;
            }
        }
        else{
            nowToken = null;
            nextToken = null;
        }
    }
    //回退token
    public void retract(){
        if(index-2<0)
            index=0;
        else
            index-=2;
        advance();
    }
    public void setTokenList(List<Token> tokenList){
        this.tokenList = tokenList;
    }
    public List<Token> getTokenList(){
        return tokenList;
    }

    // 获取当前 Token
    public Token getCurrentToken() {
        return nowToken;
    }

    // 获取下一个 Token
    public Token getNextToken() {
        return nextToken;
    }
}
