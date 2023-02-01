package com.myj.tools.javac.parser;

import com.myj.tools.javac.util.Context;
import com.myj.tools.javac.util.Name;
import com.myj.tools.javac.util.Names;

import java.nio.CharBuffer;

/**
 * 语法解析器
 */
public class Scanner implements Lexer{

    /**
     * 换行符、换页符
     */
    final static byte FF    = 0xC;

    /**
     * 换行符
     */
    final static byte LF    = 0xA;

    /**
     * 回车符
     */
    final static byte CR    = 0xD;

    final static byte EOI   = 0x1A;


    public char[] buf; // 存放文件字符

    /**
     * 当前字符
     */
    public char curChar;

    public int buflen;

    public int eofPos;

    public int bp;

    public int preEndPos;

    public int endPos;

    public int pos;
    public int sp;

    // 进制
    public int radix;

    public char[] sbuf = new char[128];  // 存放需要解析的字符

    // 当前Token对象
    public Token token;

    // 当前Name对象
    public Name name;

    public Names names;

    private Keywords keywords;

    public Scanner(Context context, CharBuffer readSource) {

        this.keywords = Keywords.instance(context);
        this.names = Names.instance(context);

        char[] curBuf = readSource.array();
        int limit = readSource.limit();
        eofPos = limit;
        if (limit == readSource.array().length) {
            if (readSource.array().length > 0 && Character.isWhitespace(readSource.array()[limit - 1])) {
                limit --;
            } else {
                char[] newBuf = new char[limit + 1];
                System.arraycopy(readSource.array(), 0, newBuf, 0, limit);
                curBuf = newBuf;
            }
        }
        this.pos = 0;
        this.buf = curBuf;
        buflen = limit;
        this.buf[buflen] = EOI;
        this.bp = -1;
        this.scanChar();
    }

    @Override
    public Token token() {
        return token;
    }

    @Override
    public Name name() {
        return name;
    }

    @Override
    public int pos() {
        return pos;
    }

    @Override
    public int radix() {
        return radix;
    }

    @Override
    public String stringVal() {
        return new String(sbuf, 0, sp);
    }

    /**
     * 获取下个token
     */
    @Override
    public void nextToken() {
        try {

            sp = 0; // 重置sbuf位置。

            while (true) {
                pos = bp;
                switch (curChar) {
                    case ' ':
                    case '\t': // 制表符
                    case FF:
                        do {
                            scanChar();
                        } while (curChar == ' ' || curChar == '\t' || curChar == FF);
                        break;
                    case LF:
                        scanChar();
                        break;
                    case CR:
                        scanChar();
                        if (curChar == LF) {
                            scanChar();
                        }
                        break;
                    case 'A': case 'B': case 'C': case 'D': case 'E':
                    case 'F': case 'G': case 'H': case 'I': case 'J':
                    case 'K': case 'L': case 'M': case 'N': case 'O':
                    case 'P': case 'Q': case 'R': case 'S': case 'T':
                    case 'U': case 'V': case 'W': case 'X': case 'Y':
                    case 'Z':
                    case 'a': case 'b': case 'c': case 'd': case 'e':
                    case 'f': case 'g': case 'h': case 'i': case 'j':
                    case 'k': case 'l': case 'm': case 'n': case 'o':
                    case 'p': case 'q': case 'r': case 's': case 't':
                    case 'u': case 'v': case 'w': case 'x': case 'y':
                    case 'z':
                    case '$': case '_':
                        scanIdent();   //解析标识符
                        return;
                        // 数字处理
                    case '0':
                        // 十六进制处理
                        scanChar();
                        if (curChar == 'x' || curChar == 'X') {
                            scanChar();
                            if (curChar == '.') {

                            } else {
                                scanNumber(16);
                            }
                        } else if (curChar == 'b' || curChar == 'B') {
                            scanChar();
                            scanNumber(2);
                        }
                        return;
                    case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        scanNumber(10);
                        return;
                    case '.':
                        scanChar();
                        if ('0' < curChar && curChar < '9') {

                        } else {
                            token = Token.DOT;
                        }
                        return;
                        // 分割符处理
                    case ',':
                        scanChar(); token = Token.COMMA; return;
                    case ';':
                        scanChar(); token = Token.SEMI; return;
                    case '(':
                        scanChar(); token = Token.LPAREN; return;
                    case ')':
                        scanChar(); token = Token.RPAREN; return;
                    case '[':
                        scanChar(); token = Token.LBRACKET; return;
                    case ']':
                        scanChar(); token = Token.RBRACKET; return;
                    case '{':
                        scanChar(); token = Token.LBRACE; return;
                    case '}':
                        scanChar(); token = Token.RBRACE; return;
                    case '/':
                        return;
                        // 对单引号的处理
                    case '\'':
                        // 获取下个字符
                        scanChar();
                        scanLitChar();
                        if (curChar == '\'') {
                            scanChar();
                            token = Token.CHARLITERAL;
                        } else {
                            //错误
                        }
                        return;
                        // 对双引号的处理
                    case '\"':
                        scanChar();
                        while (curChar != '\"' && curChar != CR && curChar!= LF && bp < buflen)
                            scanLitChar();
                        if (curChar == '\"') {
                            token = Token.STRINGLITERAL;
                            scanChar();
                        }
                        return;
                    default:
                        if (isSpecial(curChar)) {
                            scanOperator();
                        }
                        if (bp == buflen || (bp + 1 == buflen)) {
                            token = Token.EOF;
                        }
                        return;

                }
            }

        } catch (Exception e) {

        }
    }

    /**
     * 获取操作符
     */
    private void scanOperator() {
        while (true) {
            putChar(curChar);
            Name newname = names.fromChars(sbuf, 0, sp);
            if (keywords.key(newname) == Token.IDENTIFIER) {
                sp --;
                break;
            }
            name = newname;
            token = keywords.key(newname);
            scanChar();
            if (!isSpecial(curChar)) break;
        }
    }

    /**
     * 判断是否是操作符
     * @param curChar
     * @return
     */
    private boolean isSpecial(char curChar) {
        switch (curChar) {
            case '!': case '%': case '&': case '*': case '?':
            case '+': case '-': case ':': case '<': case '=':
            case '>': case '^': case '|': case '~':
            case '@':
                return true;
            default:
                return false;
        }
    }

    /**
     * 获取单个字符
     */
    private void scanLitChar() {
        if (curChar == '\\') {
            if (buf[bp + 1] == '\\') {
                bp ++;
                putChar('\\');
                scanChar();
            } else {
                scanChar();
                switch (curChar) {
                    case 'b':
                        putChar('\b'); scanChar(); break;
                    case 't':
                        putChar('\t'); scanChar(); break;
                    case 'n':
                        putChar('\n'); scanChar(); break;
                    case 'f':
                        putChar('\f'); scanChar(); break;
                    case 'r':
                        putChar('\r'); scanChar(); break;
                    case '\'':
                        putChar('\''); scanChar(); break;
                    case '\"':
                        putChar('\"'); scanChar(); break;
                    case '\\':
                        putChar('\\'); scanChar(); break;
                }
            }
        } else if (bp != buflen){
            putChar(curChar); scanChar();
        }
    }

    /**
     * 往sbuf中存放字符
     * @param ch
     */
    private void putChar(char ch) {
        if (sp == sbuf.length) {
            char[] newChars = new char[sbuf.length * 2];
            System.arraycopy(sbuf, 0, newChars, 0, sbuf.length);
            this.sbuf = newChars;
        }
        sbuf[sp ++] = ch;
    }

    /**
     * 解析数字
     * @param radix
     */
    private void scanNumber(int radix) {
        this.radix = radix;

        if (digit(radix) >= 0) {
            scanDigits(radix);
        }


        if (radix == 16 && curChar == '.') {
            scanHexFractionAndSuffix();
        } else if (radix == 16 && (curChar == 'p' ||  curChar == 'P')) {
            scanHexExponentAndSuffix();
        } else if (radix == 10 && curChar == '.') {
            putChar(curChar);
            scanChar();
            scanFractionAndSuffix();
        } else if (radix == 10 && (
                curChar == 'e' || curChar == 'E' ||
                        curChar == 'f' || curChar == 'F' ||
                        curChar == 'd' || curChar == 'D'
                )) {
            scanFractionAndSuffix();
        } else {
            if (curChar == 'l' || curChar == 'L') {
                scanChar();
                token = Token.LONGLITERAL;
            } else {
                token = Token.INTLITERAL;
            }
        }
    }

    /**
     * 扫描数字
     * @param radix
     */
    private void scanDigits(int radix) {
        do {
            if (curChar != '_') {
                putChar(curChar);
            }

            scanChar();
        } while (digit(radix) >= 0 || curChar == '_');
    }

    /**
     * 将字符转换成数字
     * @param radix
     * @return
     */
    private int digit(int radix) {
        char ch = curChar;
        int result = Character.digit(ch, radix);
        return result;
    }

    private void scanFractionAndSuffix() {
        this.radix = 10;
        scanFraction();
        if (curChar == 'f' || curChar == 'F') {
            putChar(curChar);
            scanChar();
            token = Token.FLOATLITERAL;
        } else {
            if (curChar == 'd' || curChar == 'D') {
                putChar(curChar);
                scanChar();
            }
            token = Token.DOUBLELITERAL;
        }
    }

    private void scanFraction() {
        if ('0' <= curChar && curChar <= '9') {
            scanDigits(10);
        }

    }

    /**
     * 解析16进制指数
     */
    private void scanHexExponentAndSuffix() {
    }

    /**
     * 解析16进制小数
     */
    private void scanHexFractionAndSuffix() {

    }

    /**
     * 解析标识符
     */
    private void scanIdent() {



        while (true) {
            // 将字符存入sbuf里
            sbuf[sp++] = curChar;

            scanChar();

            switch (curChar) {
                case 'A': case 'B': case 'C': case 'D': case 'E':
                case 'F': case 'G': case 'H': case 'I': case 'J':
                case 'K': case 'L': case 'M': case 'N': case 'O':
                case 'P': case 'Q': case 'R': case 'S': case 'T':
                case 'U': case 'V': case 'W': case 'X': case 'Y':
                case 'Z':
                case 'a': case 'b': case 'c': case 'd': case 'e':
                case 'f': case 'g': case 'h': case 'i': case 'j':
                case 'k': case 'l': case 'm': case 'n': case 'o':
                case 'p': case 'q': case 'r': case 's': case 't':
                case 'u': case 'v': case 'w': case 'x': case 'y':
                case 'z':
                case '$': case '_':
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    break;
                default:
                    name = names.fromChars(sbuf, 0, sp);
                    token = keywords.key(name);
                    return;
            }

        }
    }

    @Override
    public void nextChar() {

    }

    @Override
    public void scanChar() {
        curChar = buf[++bp];
    }
}
