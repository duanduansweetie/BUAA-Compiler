## SysyCompiler 设计文档

`made by duanduansweetie`

[TOC]

### 一、 参考编译器介绍

我的参考编译器是`javac`。`javac` 是 Java 语言的标准编译器，它将 Java 源代码编译为 Java 字节码，生成 `.class` 文件，可以在 Java 虚拟机 ( JVM) 上运行。`javac` 是 Java SDK（即 JDK）中的一部分，基于 Java 语言规范和 JVM 规范。选择 `javac` 的原因有以下几点：

- `sysY` 语言是c语言的子集，`javac` 的目标语言是 Java，Java 语言与c语言在很多语法结构上都有共通之处，在注释、for循环、变量定义和函数定义方面几乎没有区别。
- `javac` 完全使用 Java 编写，而我使用的也是 Java 语言，所以在语言、类的设计方面很方便进行参考。
- `javac` 是 Java 的官方编译器，具有高度优化的设计和实现，代码质量很高，遵循了最佳实践。研究 `javac` 源码有助于了解高质量编译器的开发标准。

### 二、编译器总体设计

#### 2.1 总体结构

我的编译器总体架构按照课程组要求的ppt，并参考了`javac`和课程示例编译器的设计，进行如下组织：

- 前端：包括词法分析器、语法分析器、语义分析器和中间代码生成器。
- 中端：实现机器无关代码优化功能。
- 后端：包括目标代码生成器、机器相关代码优化器。

#### 2.2 接口设计

**1. 总体架构与入口**

编译器的入口位于项目根目录的 `Compiler` 类的 `main` 方法。该方法作为整个编译流程的控制器，依次串联起前端、中端以及后端的各个模块。数据流从源代码文件输入开始，经过各级抽象表示（Token 流 -> AST -> LLVM IR -> MIPS ASM），最终输出目标汇编代码。

**2. 词法与语法分析接口**

这部分前端主要负责将源代码转换为抽象语法树（AST）。

词法分析`Lexer`类负责读取源代码字符流，其核心接口为getToken()，方法被语法分析器调用，每次返回一个识别出的 `Token` 对象，屏蔽了源码中的空白符和注释。

语法分析`Parser`类采用递归下降策略进行语法分析。针对 SysY 语言的文法规则，定义了`parseCompUnit`, `parseFuncDef`, `parseStmt` 等一系列处理方法。

- 接口交互：`Parser` 通过调用 `Lexer` 获取 Token 流，并构建出以 `CompUnit` 为根节点的抽象语法树。
- 错误处理：在递归下降的过程中，通过预读 Token 进行错误检测，并调用全局错误处理接口记录语法错误。

**3. LLVM中间代码生成接口**

中间代码生成模块位于 `llvmir` 包及其子包中，负责将 AST 转换为 LLVM IR。

- IR 构建器 (`IrBuilder`)：连接前端与中端的核心接口。
  - 输入：语法分析生成的 AST 节点。
  - 输出：构建完成的 `Module` 对象。
  - 交互：在遍历过程中，`IrBuilder` 会调用 `llvmir.value` 包下的工厂方法创建 `Instruction`、`BasicBlock`和 `Function`，并维护符号表以处理变量作用域。
- IR 结构表示：项目采用类层次结构表示 LLVM IR，所有 IR 节点均继承自 `Value` 类，使用者（如指令）继承自 `User` 类。最终通过 `Module` 类的 `toString()` 方法将内存中的 IR 对象序列化为文本格式输出。

**4. Mips目标代码生成接口**

后端部分位于 `mips` 包中，负责将 LLVM IR 转换为 MIPS 汇编代码，并进行寄存器分配优化。

- 后端入口 (`MipsBuilder`)：`MipsBuilder` 类是后端的总控接口。
  - 接口定义：`public void buildMipsModule(Module module)`。
  - 功能：它接收中端生成的 `Module` 对象，负责初始化汇编生成上下文，并按顺序调用寄存器分配器和指令翻译器。
- 指令翻译 (`InstructionTranslator`)：将单条 LLVM IR 指令翻译为对应的 MIPS 指令序列。
  - 接口设计：通过 `translate(BasicBlock bb)` 或针对具体指令的 `translateBinary`, `translateLoad` 等方法，将 IR 指令映射为 `mips.instructions` 包下的汇编指令对象（如 `MipsALU`, `MipsMem`）

- 寄存器分配接口在优化阶段进行迭代修改，本设计文档不作详细介绍，这部分会写在我的`优化文档`中。

- 代码输出：最终生成的 MIPS 代码结构存储在 `MipsModule`, `MipsFunc`, `MipsBlock` 等对象中，通过根对象的 `toString()` 方法生成最终的 `.asm` 汇编文件。

#### 2.3 文件组织

项目文件的组织结构如下：

```
E:.
├───error
├───lexer
├───llvmir
│   ├───type
│   └───value
│       ├───constants
│       ├───instructions
│       └───structure
├───mips
│   ├───instructions
│   ├───structure
│   └───value
├───node
├───parser
└───symbol
```

考虑到错误处理贯穿词法分析、语法分析、语义分析、中间代码生成四个过程，而与后续的中端和后端设计关系不是很紧密，目前我的设计是加入专门的 `error` 包，用来进行错误的专门处理。构造语法树是在语法分析过程中完成的，所以所有的语法树节点都被封装到 `node` 包中，在 `parser` 包中进行组织。

`lexer`包中主要包括以下类：

```
───lexer
│       LexerAnalyzer.java
│       LexerType.java
│       Token.java
```

`LexerType.java` 中枚举了语言中的保留字，`Token.java` 中定义了 `Token` 的数据结构，规范了 `Token` 的输出形式等内容，`LexerAnalyzer` 是具体的有限状态机的实现。

`parser`包中主要包含以下类：

```
───parser
│       IrBuilder.java
│       Parser.java
│       SymbolBuilder.java
```

`Parser.java` 类包含了所有解析语法树的处理函数，读取词法分析的token并用递归下降分析法构建抽象语法树，`SymbolBuilder.java` 中包含了建立符号表所需的方法,`IrBuilder.java`负责中间代码的构建，连接前端语法分析和终端IR表示。

`node`包中包含以下类：

```
├───node
│       AddExpNode.java
│       BlockItemNode.java
│       BlockNode.java
│       BTypeNode.java
│       CompUnitNode.java
│       CondNode.java
│       ConstDeclNode.java
│       ConstDefNode.java
│       ConstExpNode.java
│       ConstInitValNode.java
│       DeclNode.java
│       EqExpNode.java
│       ExpNode.java
│       ForStmtNode.java
│       FuncDefNode.java
│       FuncFParamNode.java
│       FuncFParamsNode.java
│       FuncRParamsNode.java
│       FuncTypeNode.java
│       InitValNode.java
│       LAndExpNode.java
│       LOrExpNode.java
│       LValNode.java
│       MainFuncDefNode.java
│       MulExpNode.java
│       Node.java
│       NumberNode.java
│       PrimaryExpNode.java
│       RelExpNode.java
│       StmtNode.java
│       UnaryExpNode.java
│       UnaryOpNode.java
│       VarDeclNode.java
│       VarDefNode.java
```

其中`Node.java`是一个抽象类，它定义了所有非终结符节点的根节点，其他java文件中包含所有的语法树节点。

`symbol`包中主要包含以下类：

```
└───symbol
        ArraySymbol.java
        BasicSymbol.java
        FuncSymbol.java
        ScopeStack.java
        Symbol.java
        SymbolKind.java
        SymbolMap.java
        SymbolType.java
```

`Symbol.java` 中定义了基本的符号类型，`ArraySymbol.java` 、`BasicSymbol.java`、`FuncSymbol.java`分别是它的继承类，分别定义了数组符号、基本类型符号和函数符号。`SymbolKind.java` 中定义的符号的事物量，如常量、变量。
`SymbolType.java` 中定义符号的类型，如 `int` 、`char` 、`void` 等。
`SymbolTable.java` 定义了符号表。
`ScopeStack.java` 定义了符号表的栈式结构，同时定义了外部接口访问和处理符号表的相关方法，是该部分的核心类。

`error`包中包含以下类：

```
───error
│       Error.java
│       ErrorManager.java
│       ErrorType.java
```

`Error.java` 定义了错误处理的输出逻辑。`ErrorType.java` 中定义了错误的类型。`ErrorManager.java` 中定义了错误的具体类，包含错误的类型，发生错误的行号等，同时定义了部分错误处理的方法。 

`llvmir` 包中包含以下类：

```
───llvmir
│   │   Printer.java
│   │
│   ├───type
│   │       CharArrayType.java
│   │       IntArrayType.java
│   │       Integer1Type.java
│   │       Integer32Type.java
│   │       Integer8Type.java
│   │       PointType.java
│   │       Type.java
│   │       VoidType.java
│   │
│   └───value
│       │   User.java
│       │   Value.java
│       │
│       ├───constants
│       │       Constant.java
│       │       ConstantArray.java
│       │       ConstantInt.java
│       │       ConstantString.java
│       │
│       ├───instructions
│       │       Alloca.java
│       │       BinaryOp.java
│       │       Br.java
│       │       Call.java
│       │       GetElementPtr.java
│       │       Icmp.java
│       │       Load.java
│       │       Ret.java
│       │       Store.java
│       │       Trunc.java
│       │       Zext.java
│       │
│       └───structure
│               BasicBlock.java
│               Function.java
│               GlobalVariable.java
│               Instruction.java
│               Module.java
│               Param.java
```

`llvmir` 包中包含三个文件夹，`type` 中定义了llvm中的 `value` 的类型， `value` 中定义的都是 `value` 的拓展类型。其中，`constants` 中定义了各种常量类，将数据转换为可以存储的 `value` ，`structure` 中定义了构成中间代码树的节点类，`instructions` 定义了具体的指令。

`mips`包中包含以下类：

```
├───mips
│   │   GenerationContext.java
│   │   GraphColoringAllocator.java
│   │   InstructionTranslator.java
│   │   LivenessAnalyzer.java
│   │   MipsBuilder.java
│   │   RegisterAllocator.java
│   │   StackAnalyzer.java
│   │
│   ├───instructions
│   │       MipsALU.java
│   │       MipsBranch.java
│   │       MipsCall.java
│   │       MipsJump.java
│   │       MipsMem.java
│   │       MipsMove.java
│   │       MipsReturn.java
│   │
│   ├───structure
│   │       MipsBlock.java
│   │       MipsFunc.java
│   │       MipsGV.java
│   │       MipsInstr.java
│   │       MipsModule.java
│   │
│   └───value
│           GlobalLabel.java
│           MipsImm.java
│           MipsOperand.java
│           PhyReg.java
│           RegManager.java
│           StackSlot.java
```

mips包中，`MipsBuilder`是后端代码生成的总入口和驱动类，`InstructionTranslator`负责将单条LLVM指令转换为等价的MIPS指令序列，`GenerationContext`负责维护全局状态，生成上下文环境。

在`structure`包下，其中`MipsInstr.java`是所有指令的抽象基类，定义通用的输出格式。`MipsModule.java`包含数据段和代码段，`MipsFunc.java`用来表示MIPS函数，`MipsBlock.java`表示一个MIPS基本块中顺序执行的指令，`MipsGV.java`表示全局变量。

具体指令位于`Instruction`包下，代表具体的汇编操作码。`MipsALU.java`中包含算术逻辑运算指令，`MipsMem.java`包含内存访问指令，`MipsBranch.java`中包含条件分支指令，`MipsJump.java`中包含无条件跳转指令，`MipsCall.java`中完成`jal`指令，`MipsMove.java`中存放`move`指令，`MipsReturn.java`中存放函数返回序列。

`value`包下存放指令的操作数。其中`PhyReg.java`表示物理寄存器，`RegManager.java`类是物理寄存器的管理器，提供分配临时寄存器、获取参数寄存器等方法；`MipsImm.java`表示立即数，`StackSlot.java`表示栈上的一个位置，用来存储溢出的变量或局部数组；`GlobalLable.java`表示全局标签。

### 三、词法分析设计

#### 3.1 编码前的整体设计

1. 编码前我仔细阅读了 `javac` 的源码结构，准备模仿 `javac` 设计词法分析的接口 `Lexer` 和 `Scanner` 进行具体的实现。同时我准备使用工厂模式来模仿 `javac` 源码的处理和设计方式。

2. 对于错误处理，一开始我没有设计单独的包，并计划在可能出现问题的位置直接进行错误处理。
3. 我计划使用一个`getsym()`方法返回一个`Token`，这样可以直接在语法分析中接着使用。

#### 3.2 编码的具体实现

1. 对于上述第一点，考虑到编译器的体量和实际的使用情况，我发现使用抽象类或者接口定义类的方法对于我的小型项目没有太多意义，因此我把`Lexer`和`Scanner`合并成为新的`LexerAnalyzer`，直接对输入输出进行处理。同时我放弃了工厂模式，直接把所有处理逻辑都写在了`LexerAnalyzer.java`里面。
2. 对于上述的错误处理，我发现其贯穿前端的词法、语法、语义分析三个过程，并且每个过程都会新增一些错误类型，且与后端的设计关系不大，我设计了专门的错误处理`error`包，并在其中`ErrorType`枚举类定义了每种错误类型：

```java
package error;
public enum ErrorType{
    A("非法符号","a"),
    B("名字重定义","b"),
    C("未定义的名字","c"),
    D("函数参数个数不匹配","d"),
    E("函数参数类型不匹配","e"),
    F("无返回值的函数存在不匹配的return语句","f"),
    G("有返回值的函数缺少return语句","g"),
    H("不能改变常量的值","h"),
    I("缺少分号","i"),
    J("缺少右小括号')'","j"),
    K("缺少右中括号']'","k"),
    L("printf中格式字符串与表达式个数不匹配","l"),
    M("在非循环块中使用break和continue语句","m");

    private final String value;
    private final String category;
    ErrorType(String value, String category){
        this.value = value;
        this.category = category;
    }
    @Override
    public String toString(){
        return category;
    }
    public String getNameString() {
        return value;
    }
}
```

同时，我设计了错误的处理类，对于不同错误进行不同错误处理。

```java
public class ErrorManager {
    private final List<Error> errorList = new ArrayList<>();
    public boolean hasError = false; // 是否有错误
    public int inFor=0;
    public boolean inVoidFunc = false;
    public boolean inIntFunc = false;
    public boolean inFor(){
        return inFor!=0;
    }
     private ScopeStack scopeStack; // 添加 ScopeStack 的引用

    public void setScopeStack(ScopeStack scopeStack) {
        this.scopeStack = scopeStack;
    }
    public void addError(int lineNumber, ErrorType errorType) {
        for(Error error : errorList) {
            if (error.getLineNumber() == lineNumber) {
                return; // Error already exists, do not add again
            }
        }
        hasError = true;
        errorList.add(new Error(lineNumber, errorType));
    }
    public void showErrors() {
        errorList.sort(Comparator.comparingInt(Error::getLineNumber));
        for (Error error : errorList) {
            System.out.println(error.getLineNumber() + " " + error.getErrorType());
        }
    }

    public void writeErrorsToFile(String filePath) throws IOException {
        errorList.sort(Comparator.comparingInt(Error::getLineNumber));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Error error : errorList) {
                writer.write(error.getLineNumber() + " " + error.getErrorType() + "\n");
            }
        }
    }
    ...
}
```

3. 对于上述第三点，进行如下的设计：

```java
 public Token getsym() throws IOException{
        getChar();
        clearToken();
        skipSpace();

        CharType charType = getCharType(nowChar, nextChar);
        switch(charType){
            case LETTER:
            case UNDERLINE:
                handleIdent();
                break;
            case DIGIT:
                handleNumber();
                break;
            case SINGLE_DELIM:
                handleSingleDelim();
                break;
            case DOUBLE_DELIM:
                handleDoubleDelim();
                break;
            case DOUBLE_QUOTE:
                handleStringConst();
                break;
            case ANNOTATION:
                handleAnnotation();
                break;
            case AND_OR:
                handleAndorOr();
                break;
            case SPACE:
            case NEWLINE:
            case TAB:
                skipSpace();
                return getsym(); // 跳过空白后递归调用
            case END:
                return null;
            default:
                //throw new RuntimeException("Unknown character: " + nowChar);
        }
        return nowToken;
    }
```

#### 3.3 核心部分设计

词法分析的核心步骤其实是一个有限状态机，处理方法如下，下图为示意图：

![63892afb42eb64cee9cb123727e8d02](E:\BUAA_study\大三\编译\实验-24\SysYCompiler\docs\assets\63892afb42eb64cee9cb123727e8d02.png)

通过`getsym`方法进行分发，将不同的`token`分配至不同的`handle`方法。

### 四、语法分析设计

#### 4.1 编码前的设计

语法分析的关键就是构建一颗语法树，通过语法树的后序遍历来完成整个工作。问题的难点在于怎么合理的构建一颗语法树。对于这个构建过程，我参考了Github上几个学长的项目写法，计划由一个`Node`基类开始，为每一个非终结符写一个扩展的`Node`类。将词法分析得到的`Tokens`到语法分析模块来使用，最后遍历整个树，完成输出的任务要求。

以下是我参考的仓库链接：

- https://github.com/hjc-owo/Compiler/blob/Compiler/src/frontend/Parser.java

- https://gitee.com/sethan/Compiler

#### 4.2 难点分析

**4.2.1 Stmt文法的处理**

对于Stmt文法的处理，先考虑文法

```
BlockItem → Decl | Stmt
```

对于这类文法，需要识别Decl和Stmt的区别。

对于Decl而言，有以下文法：

```
Decl → ConstDecl | VarDecl 
ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';' 
VarDecl → [ 'static' ] BType VarDef { ',' VarDef } ';'
BType → 'int'
```

可以看到，Decl会在nowToken为`const`或`int`时可能出现。

再分析Stmt：

```
Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
| [Exp] ';'
| Block
| 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省，1种情况 2. ForStmt与
Cond中缺省一个，3种情况 3. ForStmt与Cond中缺省两个，3种情况 4. ForStmt与Cond全部缺省，1种情况
| 'break' ';'
| 'continue' ';'
| 'return' [Exp] ';' // 1.有Exp 2.无Exp
| 'printf''('StringConst {','Exp}')'';' // 1.有Exp 2.无Exp
```

```
LVal → Ident ['[' Exp ']'] 
Exp → AddExp
AddExp → MulExp | AddExp ('+' | '−') MulExp
MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
UnaryOp → '+' | '−' | '!'
Block → '{' { BlockItem } '}' 
```

currentToken可能为Ident，"+"，"-"，"!"，"{"，'if'，'for'，'break'，'return' ，'printf'。

可以看出二者在读第一个token的时候就是可区分的，不需要预读再回溯。

**4.2.2 左递归文法的处理**

对于左递归文法的处理，一开始我设想为构建右递归文法，例如：

```
LAndExp → EqExp | LAndExp '&&' EqExp
改写文法为：
LAndExp → EqExp | EqExp '&&' LAndExp
```

这样固然解决了左递归的匹配问题，但是经过验证，这样不管怎么修改树的输出形式，都无法使得树正确的输出节点，具体问题是当出现多重表达式（例如 `a && b && c`），修改后的文法总会将语法树解析为如下顺序：`a && (b && c)`，这与我们预先期望的 `(a && b) && c` 不符。

于是，我决定将文法修改为：

```
LAndExp → EqExp {'&&' EqExp}
```

其他左递归文法类似。这样可以在处理多重表达式时人为的调整顺序。代码如下：

```java
package node;

import lexer.LexerType;
import parser.Parser;

import java.io.IOException;
import java.util.List;

public class LAndExpNode extends Node {
    public final String name = "<LAndExp>";
    private List<EqExpNode> eqExpNodeList;
    public LAndExpNode() {
    }

    public LAndExpNode(List<EqExpNode> eqExpNodeList) {
        this.eqExpNodeList = eqExpNodeList;
    }

    public List<EqExpNode> getEqExpNodeList() {
        return eqExpNodeList;
    }

    @Override
    public void show() throws IOException {
        for (int i = 0; i < eqExpNodeList.size(); i++) {
            if (i != 0) {
                Parser.parseWriter.write(LexerType.AND.name() + " " + "&&" + "\n");
            }

            eqExpNodeList.get(i).show();
            Parser.parseWriter.write(name + "\n");
        }
    }
}
```

```java
 public LAndExpNode parseLAndExp() {
        //LAndExp → EqExp { '&&' EqExp }
        List<EqExpNode> eqExpNodes = new ArrayList<>();
        EqExpNode eqExpNode = parseEqExp();
        if (eqExpNode != null)
            eqExpNodes.add(eqExpNode);
        while (nowToken.getType() == LexerType.AND) {
            getToken();
            eqExpNode = parseEqExp();
            if (eqExpNode != null)
                eqExpNodes.add(eqExpNode);
        }
        //System.out.println(nowToken.getValue()+" landexp");
        return new LAndExpNode(eqExpNodes);
    }
```

### 五、语义分析设计

语义分析部分要求完成符号表的建立和语义分析阶段的错误处理。这次的主要难点是错误处理的类型和要求都比之前高很多，对错误类的的处理逻辑显然更加复杂。

#### 5.1 编码前的设计

**5.1.1 符号表设计**

我的编译器准备使用栈式符号表来进行符号表的组织，关键在于对于符号表存放内容的设计。在阅读往年编译器后发现，往年编译器没有`static`类型，在设计的时候不需要考虑`static`类型的区分。

我从以下几个方面对符号的不同类型进行区分：

- 事物量：常量、普通变量、静态变量、函数
- 数据类型：`int` 、`void`
- 数据类别：数组、基本类型

可以看到，对于函数，文法中只允许其类型为`int`或者`void`，不允许函数的返回值是数组类型。而对于其他数据，其有可能是普通变量、静态变量与常量、数组和基本类型的任意组合。

数据类型只对强制类型转换有影响，所以这部分需要单独处理，但是其对数组的组织形式没有任何影响，所以数组可以定义为同一个 `Symbol`。变量和常量分别处理即可。

每进入新的作用域时，创建一个新的符号表并将其压入栈顶；退出作用域时，将符号表从栈中弹出。这种设计能够有效管理局部和全局符号的作用范围。

**下面主要介绍作用域栈 (`ScopeStack`) 的设计：**

`ScopeStack` 采用单例模式（Singleton），确保在整个编译过程中只有一个作用域栈实例。通过栈结构维护符号表，使符号查找符合作用域嵌套规则。

- `enterScope()`：进入新作用域时，创建新符号表并压入栈中。
- `exitScope()`：退出作用域时，将当前符号表弹出栈并存入符号列表以便后续输出。
- `addSymbol()`：向当前作用域符号表中添加符号，支持基本类型符号、数组符号和函数符号的存储。
- `lookup()`：支持从当前作用域及上层作用域查找符号，实现多层嵌套符号的快速访问。

**5.1.2 错误处理设计**

下面介绍我对各种类型错误的处理方法：

| 错误类型                             | 错误类别码                          | 处理方法                                                     |
| ------------------------------------ | ----------------------------------- | ------------------------------------------------------------ |
| 非法符号                             | a                                   | 当读到非法符号 '&' 和 '\|' 而非 '&&' 和 '\|\|' 时，直接将其加入到错误的列表中，并且将 '&' 和 '\|'解析成 '&&' 和 '\|\|'。 |
| 名字重定义                           | b                                   | 在添加符号表的过程中，添加之前先判断在**当前作用域下**是否存在该符号的同名符号，如果存在，则将其加入错误列表中，否则将符号添加到该层符号表。 |
| 未定义的名字                         | c                                   | 在使用该函数名或者变量名时，按照符号表栈的反向顺序搜索符号，即先在本层符号表中搜索，如果不存在该符号，在本层符号表的父作用域中查找，直到找到该符号。如果不存在，则将其加入错误列表中。 |
| 函数参数个数不匹配                   | d                                   | 在存储函数类型的符号时，将其形参列表也存储到符号表中，这样在调用函数时，获取其实参的个数，将形参个数和实参个数进行对比，如果不同，将其加入错误列表中。 |
| 函数参数类型不匹配                   | e                                   | 在调用函数时，获取其实参的类型（判断一个实参是否为数组，只需要判断实参是不是左值类型且该左值类型的Ident在符号表中是否为数组类型），将形参类型和实参类型进行对比，如果不同，将其加入错误列表中。 |
| 无返回值的函数存在不匹配的return语句 | f                                   | 在error中设置全局的变量，存储当前是否在void函数中，在进入void函数时，将该变量设为true。在return语句中检查return 后是否包含其他内容，如果return后包含返回值且当前正在void函数中，则将其加入错误列表中。 |
| 有返回值的函数缺少return语句         | g                                   | 直接访问函数Block中的最后一条语句，检测其是否为return即可。如果不是，则将其加入错误列表中。 |
| 不能改变常量的值                     | h                                   | 在访问一个左值时，从最近的符号表中检测其是否为常量类型，如果是，则将其加入错误列表中。 |
| 缺少分号                             | i                                   | 在解析语法的过程中，如果本来期待的位置不是分号，则将其加入错误列表中。 |
| 缺少右小括号                         | j                                   | 同上，在解析语法的过程中，如果本来期待的位置不是右小括号，则将其加入错误列表中。 |
| 缺少右中括号                         | k                                   | 同上，在解析语法的过程中，如果本来期待的位置不是右中括号，则将其加入错误列表中。 |
| printf中格式字符与表达式个数不匹配   | l                                   | 检测printf语句中的StringConst中包含%c和%d的个数，看其是否与后面的表达式个数相同。如果不同，则将其加入错误列表中。 |
| 在非循环块中使用break和continue语句  | printf中格式字符与表达式个数不匹配m | l设置全局变量，当进入循环块时，将其设置为true，离开循环块时设为false，在处理break和continue语句时，检测该全局变量的值，如果其为false，则加入错误列表中。 |

### 六、中间代码生成

#### 6.1 编码前的设计

由于我计划最后生成mips目标代码，参加竞速排序，因此无论我生成什么中间代码，最后都需要转换成mips。但如果我选择生成四元式，无法在生成完MIPS之前进行测评，那么将无法确定四元式的正确与否。且如果mips最后没有足够时间生成，则代码生成的两次作业都无法完成。因此我选择了生成llvm中间代码。

**6.1.1 中间代码结构设计**

使用指导书上给出的中间结构设计，如下图的树状结构：

![image-20251210150614465](C:\Users\张腾月\AppData\Roaming\Typora\typora-user-images\image-20251210150614465.png)

一个Module中包含若干的GlobalVariable和Function，GlobalVariable中包含全局的变常量，Function包含定义的函数和main函数。Function中包括若干Param和BasicBlock，Param是函数的形参，BasicBlock是需要完整执行的一个单元。BasicBlock中包含若干Instruction。一个Instruction中包含若干参与组成该指令的value。

对于每个value，都应该有一个自己的类型，主要包含下面的类型：

```
CharArrayType、IntArrayType、Integer1Type、Integer32Type、
Integer8Type、PointerType、VoidType
```

当然一些value不需要类型，或者他们的类型没有意义，我们设置成null即可。

在整体的架构方面，上面已经介绍的很清楚了，只需要设计下面的类：

```
BasicBlock.java
Function.java
GlobalVariable.java
Instruction.java
Module.java
Param.java
```

同时，每个指令应该有自己的一个具体的类型，用到的指令主要有：

```
 Alloca BinaryOp(+ - * / %) Br Call GetElementPtr Load Ret Store Trunc Zext
 Icmp(>= <= > < == !=)
```

当然，对于代码中出现的常量字符和字符串，也要有相应的value。这部分比较简单，这里不再赘述。

**6.1.2 构建中间结构的方法**

在中间代码生成过程中，因为从源语言到中间代码的过程仍然属于前端，我的中间代码生成是在语法分析、语义分析的过程中同步进行的，即在语法分析得到相应的语法子树时，就立刻进行符号表的建立和中间代码（llvm）的生成。这个过程中，我通过 `/parser/IrBuilder.java` 文件中的 `build + node()` 函数进行中间代码的构建。构建完成后，通过 `module` 中重写的 `toString()` 函数进行输出。具体的树型逻辑如下，但是我没有实现GlobalValue，直接将函数和全局变量存储在 `module` 中。

 <img src="C:\Users\张腾月\AppData\Roaming\Typora\typora-user-images\image-20251210151100539.png" alt="image-20251210151100539" style="zoom: 67%;" />

#### 6.2 难点分析

**6.2.1 符号表的构建和修改**

因为不想设计新的符号表，所以必须强行使用之前的符号表。很显然，符号表中应该存储一个value，用来标识该变量对应的虚拟寄存器，在使用到该变量时，可以直接通过虚拟寄存器访问该变量。

```java
package symbol;
import llvmir.value.Value;
public abstract class Symbol {
    private final String name;//符号名
    private final SymbolType type;//符号类型
    private final int scopelevel;//符号所在层次
    private final int scopenum;//符号所在层次编号
    private Value value;
    public Symbol(String name, SymbolType type, int level, int num) {
        this.name = name;
        this.type = type;
        this.scopelevel = level;
        this.scopenum = num;
    }
    ...
```

但是新的问题出现了，创建一个符号时，还没有出现它的value，所以所有的value都是在代码生成过程中set进去的。而且不是每个变量都能得到其本身的value，所以实际存储在符号表中的其实是一个value实值的指针。

因此，原本属于语义分析的符号表可以复用到代码生成阶段。

但我发现，还是会有新的问题，当语义分析在 if 或者 for 所在的 block 中，会创建一个新的符号表作用域，之后离开该 block 时将该符号表从栈中弹出。如果在这些基本块中定义新的变量，那么在代码生成的过程中，这些符号将会丢失。

```java
funcDefNode = new FuncDefNode(funcTypeNode, identNode, funcFParamsNode, blockNode);
Function function = irBuilder.buildFunction(funcDefNode);
scopeStack.exitScope();
irBuilder.buildFunctionBody(blockNode, function);
return funcDefNode;
```

在构建函数时，代码生成的 build 不能深入到语句中，而是在函数这个级别就进行了，所以上述的问题无法简单解决。当然，最终还是有一个不成熟的解决方法：将每个block都进行标号，如果处理到这个 block，从弹出的符号表中把这部分符号再弹回来，之后处理完再弹出去。这样就算遇到多个 block 嵌套，那也可以解决符号不在符号表中的问题。

**6.2.2 getElementPtr指令**

首先，给出指导书中对他的解释：

```javascript
<result> = getelementptr <ty>, <ty>* <ptrval>{, [inrange] <ty> <idx>}*
```

也可以为 `getelementptr` 的参数添加括号，如下。

```javascript
<result> = getelementptr (<ty>, <ty>* <ptrval>{, [inrange] <ty> <idx>}*)
```

现在来理解一下上面这一条指令。第一个 `<ty>` 表示的是第一个索引所指向的类型，有时也是**返回值的类型**。第二个 `<ty>` 表示的是后面的指针基地址 `<ptrval>` 的类型， `<ty> <index>` 表示的是一组索引的类型和值，在本实验中索引的类型为 `i32`。索引指向的基本类型确定的是增加索引值时指针的偏移量。

说完理论，不如结合一个实例来讲解。考虑数组 `a[5]`，需要获取 `a[3]` 的地址，有如下写法：

```javascript
%1 = getelementptr [5 x i32], [5 x i32]* @a, i32 0, i32 3

%2 = getelementptr [5 x i32], [5 x i32]* @a, i32 0
%3 = getelementptr i32, i32* %2, i32 3

%3 = getelementptr i32, i32* @a, i32 3
```

简单点，这个指令可以传好几个参，对于一元数组来说，那么他最多传俩参。

这个指令类似 load，所以传进去的要么是二维的指针，要么是数组的指针，要么是一维指针，没有其他的可能性了。

传一个参不改变类型，传进来的是二维指针，那出去也是二维指针，传进来是数组指针，那么出去的时候就是数组指针。当然，传进来是一维指针这个倒是不可能。这个倒是不常用，当且仅当在处理左值时，如果传入的是个数组指针，那要先load再getElementPtr.

```java
if (lValNode.getExpNode() != null) {
    // 数组元素
    if (value.getType() instanceof PointerType
            && ((PointerType) value.getType()).getPoint() instanceof PointerType) {
        Load load = new Load(value, currentBlock);
        currentBlock.addInstruction(load);
        GetElementPtr getElementPtr = new GetElementPtr(buildExp(lValNode.getExpNode()),
                load, currentBlock, true);
        currentBlock.addInstruction(getElementPtr);
        return getElementPtr;
    }
    Value index = buildExp(lValNode.getExpNode());
    GetElementPtr getElementPtr = new GetElementPtr(index, value, currentBlock);
    currentBlock.addInstruction(getElementPtr);
    return getElementPtr;
}
```

传两个参降一个维度，这是这个指令最多的用法。具体而言，传进来一维指针，传出的是一个i32或者i8，传进来是数组指针，传出去的就是个指针，因为他实际的意义是找索引，当然不可能传出去一个数组，传进来二维指针，那就传出一维指针。

```java
package llvmir.value.instructions;

import llvmir.type.CharArrayType;
import llvmir.type.IntArrayType;
import llvmir.type.Integer32Type;
import llvmir.type.Integer8Type;
import llvmir.type.PointerType;
import llvmir.value.Value;
import llvmir.value.structure.BasicBlock;
import llvmir.value.structure.Instruction;

public class GetElementPtr extends Instruction {
    // 只设计两个索引的情况，所以返回值类型一定是指针或者数组类型的儿子，即两种基本类型
    // 换句话说，使用该指令必将解引用
    // 传入的类型必定是一个指针类型
    Boolean isSingle = false;

    public GetElementPtr(Value index, Value array, BasicBlock basicBlock) {
        // 根据数组类型生成GetElementPtr的类型
        super(null, ((PointerType) array.getType()).getPoint(), basicBlock);
        operands.add(array);
        operands.add(index);
        if (this.getType() instanceof IntArrayType) {
            this.setType(new PointerType(new Integer32Type()));
        } else if (this.getType() instanceof CharArrayType) {
            this.setType(new PointerType(new Integer8Type()));
        }
    }

    public GetElementPtr(Value index, Value array, BasicBlock basicBlock, Boolean isSingle) {
        // 根据数组类型生成GetElementPtr的类型
        super(null, array.getType(), basicBlock);
        operands.add(array);
        operands.add(index);
        if (this.getType() instanceof IntArrayType) {
            this.setType(new PointerType(new Integer32Type()));
        } else if (this.getType() instanceof CharArrayType) {
            this.setType(new PointerType(new Integer8Type()));
        }
        this.isSingle = isSingle;
    }

    @Override
    public String toString() {
        if (isSingle)
            return name + " = getelementptr inbounds " + ((PointerType) operands.get(0).getType()).getPoint() + ", "
                    + operands.get(0).getType() + " "
                    + operands.get(0).getName() + ", i32 " + operands.get(1).getName();
        else
            return name + " = getelementptr inbounds " + ((PointerType) operands.get(0).getType()).getPoint() + ", "
                    + operands.get(0).getType() + " "
                    + operands.get(0).getName() + ", i32 0, i32 " + operands.get(1).getName();
    }
}
```

**6.2.3 void函数的函数结束语句**

在处理过程中，发现如果void函数没有return语句的话，llvm将无法正确执行，而对于后续的if和for分割基本块更是无法退出函数，所以，需要给这样的函数增加一个return void语句，当然这不是什么很难的事情，只不过确实需要注意，而且为我造成了一些困难。

首先在IrBuilder中维护这么一个变量：

```java
public boolean voidFuncHasReturn = true;
```

然后，每当进入一个函数，先判断其是否是没有返回语句的void函数：

```java
if (!isReturn) {
    // index-2是非main函数的左括号前的token
    if (error.inIntFunc || error.inCharFunc)
        error.addError(tokenList.get(index - 2).getLineno(), ErrorType.G);
    else if (error.inVoidFunc)
        irBuilder.voidFuncHasReturn = false;
}
```

当然要注意在出函数的时候维护它。最后要做的就是在函数的最后加上这个语句。

```java
public void buildFunctionBody(BlockNode blockNode, Function function) {
    currentBlock = new BasicBlock(function);
    currentFunction = function;
    inGlobal = false;
    scopeStack.addSymbolTable(blockNode.symbolTableIndex);
    List<Param> params = function.getParams();
    for (int i = 0; i < params.size(); i++) {
        Param param = params.get(i);
        Alloca alloca = new Alloca(param.getType(), currentBlock);
        currentBlock.addInstruction(alloca);
        scopeStack.lookup(param.getRealName()).setValue(alloca);
        Store store = new Store(param, alloca, currentBlock);
        currentBlock.addInstruction(store);
    }
    scopeStack.popSymbolTable();
    buildBlock(blockNode);
    if (!voidFuncHasReturn) {
        Ret ret = new Ret(null, currentBlock);
        currentBlock.addInstruction(ret);
    }
}
```

**6.2.4 短路求值**

实际上，短路求值这个东西我也是看了很多佬的代码才知道具体的实现步骤，这个逻辑很简单，就是实现起来没有那么容易。

简单来说，一个成功块一个失败块一个新块。对于||的运算，第一次肯定要建一个新块，计算第一个LAndExp，之后每次计算一个LAndExp，如果它是真的，那么整个式子都是真的，那就直接跳到成功块。如果它是假的，那就跳到那个新块，然后再接着算。直到最后一个之前，都是这样的。那么最后一个如果成功，也跳到成功块，否则跳到失败块（而不是新块）。

对于&&，一样的道理。第一次肯定要建一个新块，计算第一个EqExp，之后每次计算一个EqExp，如果它是假的，那么整个式子都是假的，那就直接跳到失败块。如果它是真的，那就跳到那个新块，然后再接着算。直到最后一个之前，都是这样的。那么最后一个如果失败，也跳到s失败块，否则跳到成功块（而不是新块）。

实现代码如下：

```java
public void bulidLOrExp(LOrExpNode lOrExpNode, BasicBlock trueBlock, BasicBlock falseBlock) {
    // 先计算内部的instrution,再计算当前的instruction
    // 短路求值
    BasicBlock nextBlock;
    int i;
    for (i = 0; i < lOrExpNode.getlAndExpNodeList().size() - 1; i++) {
        LAndExpNode lAndExpNode = lOrExpNode.getlAndExpNodeList().get(i);
        nextBlock = new BasicBlock(currentFunction);
        buildLAndExp(lAndExpNode, trueBlock, nextBlock);
        currentBlock = nextBlock;
    }
    // 最后一个
    LAndExpNode lAndExpNode = lOrExpNode.getlAndExpNodeList().get(i);
    buildLAndExp(lAndExpNode, trueBlock, falseBlock);
}

public void buildLAndExp(LAndExpNode lAndExpNode, BasicBlock trueBlock, BasicBlock falseBlock) {
    // 先计算内部的instrution，再计算当前的instruction
    // 短路求值
    BasicBlock nextBlock;
    int i;
    for (i = 0; i < lAndExpNode.getEqExpNodeList().size() - 1; i++) {
        EqExpNode eqExpNode = lAndExpNode.getEqExpNodeList().get(i);
        nextBlock = new BasicBlock(currentFunction);
        Value value = buildEqExp(eqExpNode);
        Br br = new Br(nextBlock, falseBlock, value, currentBlock);
        currentBlock.addInstruction(br);
        currentBlock = nextBlock;
    }
    // 都成功的话，进入true
    EqExpNode eqExpNode = lAndExpNode.getEqExpNodeList().get(i);
    Value value = buildEqExp(eqExpNode);
    Br br = new Br(trueBlock, falseBlock, value, currentBlock);
    currentBlock.addInstruction(br);
}
```

#### 6.3 编码后的修改

其他修改基本上在优化阶段我才进行，详情见优化文档。

### 七、目标代码生成

#### 7.1 编码前的设计

这部分需要把llvm代码转换成mips，我认为这部分的难点主要在于对函数调用的处理，还有就是在不进行寄存器分配优化算法的基础上对寄存器进行合理的分析。我现在没有进行全局寄存器的分配，只使用了临时寄存器，在函数调用时保存使用的临时寄存器。

**7.1.1 value设计**

参考了许多前人的代码，虽然我已经没有时间完成一些比较耗时的优化，但是还是相对完整的实现了一些mips的结构。

首先是一个类llvm中Value的类：MipsValue。

```java
package mips.value;

public class MipsValue {
    // 这个类似于一个llvmir中的虚拟寄存器，用来存储MIPS指令中的操作数
    // 一个MipsValue可以是一个立即数，一个寄存器，一个内存地址
}
```

这个类类似于一个llvmir中的虚拟寄存器，用来存储MIPS指令中的操作符，也是寄存器分配的基础。接着我分别设计了以下类继承自MipsValue，具体的作用见下：

```java
enum type{
    Data,			// 全局字符数组变常量的寄存器，存储全局变量的名称，方便调用
    Immediate,		// 存储立即数，方便toString()的输出
    Register,		// 真实的物理寄存器，一共32个
    VirRegister		// 虚拟的寄存器，存储放在栈上的变量，空间
}
```

**7.1.2 寄存器分配**

对于MARS的32个寄存器，我作如下的设计：

```java
// 常数0
public static final Register ZERO = new Register("$zero", 0, Type.SPECIAL);
// 汇编器保留寄存器，由汇编器在特定场景（通常是加载大常数）自动生成
public static final Register AT = new Register("$at", 1, Type.SPECIAL);
// 函数返回值
public static final Register V0 = new Register("$v0", 2, Type.RESULT);
// 这个寄存器我用来存放一些特殊的值，所以不对外开放（比如一些确定只使用一次的全局变量）
public static final Register V1 = new Register("$v1", 3, Type.SPECIAL);
// 函数调用参数
public static final Register A0 = new Register("$a0", 4, Type.ARGUMENT);
public static final Register A1 = new Register("$a1", 5, Type.ARGUMENT);
public static final Register A2 = new Register("$a2", 6, Type.ARGUMENT);
public static final Register A3 = new Register("$a3", 7, Type.ARGUMENT);

// 临时寄存器
public static final Register T0 = new Register("$t0", 8, Type.TEMPORARY);
public static final Register T1 = new Register("$t1", 9, Type.TEMPORARY);
public static final Register T2 = new Register("$t2", 10, Type.TEMPORARY);
public static final Register T3 = new Register("$t3", 11, Type.TEMPORARY);
public static final Register T4 = new Register("$t4", 12, Type.TEMPORARY);
public static final Register T5 = new Register("$t5", 13, Type.TEMPORARY);
public static final Register T6 = new Register("$t6", 14, Type.TEMPORARY);
public static final Register T7 = new Register("$t7", 15, Type.TEMPORARY);
public static final Register T8 = new Register("$t8", 24, Type.TEMPORARY);
public static final Register T9 = new Register("$t9", 25, Type.TEMPORARY);
public static final Register S0 = new Register("$s0", 16, Type.TEMPORARY);
public static final Register S1 = new Register("$s1", 17, Type.TEMPORARY);
public static final Register S2 = new Register("$s2", 18, Type.TEMPORARY);
public static final Register S3 = new Register("$s3", 19, Type.TEMPORARY);
public static final Register S4 = new Register("$s4", 20, Type.TEMPORARY);
public static final Register S5 = new Register("$s5", 21, Type.TEMPORARY);
public static final Register S6 = new Register("$s6", 22, Type.TEMPORARY);
public static final Register S7 = new Register("$s7", 23, Type.TEMPORARY);
// 全局寄存器
// 现在这些全局寄存器都没用到，感觉以后也用不到了
// 系统保留寄存器，在系统发生中断时使用
// 我们这里用作全局寄存器
public static final Register K0 = new Register("$k0", 26, Type.SAVED);
public static final Register K1 = new Register("$k1", 27, Type.SAVED);
// 维护函数的栈帧，用于访问局部变量和函数参数
// 我们这里用作全局寄存器
public static final Register FP = new Register("$fp", 30, Type.SAVED);
// 全局指针，指向全局变量所在区域
// 我们这里用作全局寄存器
public static final Register GP = new Register("$gp", 28, Type.SAVED);
// 栈帧寄存器
public static final Register SP = new Register("$sp", 29, Type.SPECIAL);
// 返回地址
public static final Register RA = new Register("$ra", 31, Type.SPECIAL);
```

在进行优化之前，我把变量都存在了栈上，所以使用的都是临时寄存器。

**7.1.3 结构设计**

对于这部分，我使用了类似中端的结构，具体如下图：

<img src="C:\Users\张腾月\AppData\Roaming\Typora\typora-user-images\image-20251210171012303.png" alt="image-20251210171012303" style="zoom:67%;" />

**7.1.4 函数调用**

如果函数有返回值（非void），为其分配一个临时寄存器：

```java
if (!call.getFirstOperand().getType().isVoidType()) {
    if (call.getRegister() == null) {
        call.setRegister(Register.getTempRegister(currFunc));
    }
}
```

如果函数没有参数，即操作数只有函数名，可以先提取函数名，判断是否是库函数，设置栈空间大小`offset`：库函数`offset-1`，其他函数根据栈的大小设置，并设计`jal`指令跳转到函数。

```java
if (call.getOperands().size() == 1) {
    String funcName = call.getOperands().get(0).getName().substring(1);
    int offset = funcName.matches("putint|putch|getint|getchar|putstr") ? -1 
                 : mipsModule.getFunction(funcName).getStackSize();
    currBlock.addInstruction(new Jal(funcName, offset, currFunc));
}
```

如果函数有参数，则为每一个参数都分配寄存器。常量用立即数寄存器，变量用临时寄存器。

```java
List<MipsValue> args = new ArrayList<>();
for (int i = 1; i < call.getOperands().size(); i++) {
    Value arg = call.getOperands().get(i);
    if (arg.getRegister() == null) {
        arg.setRegister(arg instanceof ConstantInt || arg instanceof ConstantChar 
                        ? new Immediate(arg.getValue()) 
                        : Register.getTempRegister(currFunc));
    }
    args.add(arg.getRegister());
}
```

如果函数有返回值，把V0寄存器的值存入分配的寄存器。

```java
if (!call.getFirstOperand().getType().isVoidType()) {
    currBlock.addInstruction(new Move(call.getRegister(), Register.V0));
}
```

**7.1.5 函数调用过程中的寄存器保存**

- **寄存器保存**

  调用函数前，将当前函数中使用的寄存器（`savedRegisters`）的值保存到栈中。寄存器保存的地址偏移量基于当前函数的栈帧大小和寄存器的临时寄存器偏移量。

  ```java
  for (Register register : savedRegisters) {
      sb.append("sw ").append(register).append(", ")
        .append(stackOffset + Register.TEMP_REGISTER_OFFSET.get(register)).append("($sp)\n");
  }
  ```

  函数调用结束后，把保存的寄存器值从栈中恢复。

  ```java
  for (Register register : savedRegisters) {
      sb.append("lw ").append(register).append(", ")
        .append(stackOffset + Register.TEMP_REGISTER_OFFSET.get(register)).append("($sp)\n");
  }
  ```

- **参数传递**

  对于前 n 个参数（其中 n 为 MIPS 的参数寄存器数量），直接通过参数寄存器传递：

  ```java
  for (int i = 0; i < args.size() && i < Register.getArgumentRegistersCount(); i++) {
      if (args.get(i) instanceof Register)
          sb.append("move ").append(Register.getArgumentRegisters().get(i)).append(", ").append(args.get(i)).append("\n");
      else
          sb.append("li ").append(Register.getArgumentRegisters().get(i)).append(", ").append(args.get(i)).append("\n");
  }
  ```

  对于超过寄存器限制的参数，通过栈传递，参数存储在 $sp 的指定偏移量位置：

  ```java
  for (int i = Register.getArgumentRegistersCount(); i < args.size(); i++) {
      if (args.get(i) instanceof Register) {
          sb.append("sw ").append(args.get(i)).append(", ").append(offset).append("($sp)\n");
      } else {
          sb.append("li $v1, ").append(args.get(i)).append("\n");
          sb.append("sw $v1, ").append(offset).append("($sp)\n");
      }
      offset += 4;
  }
  ```

**7.1.6 其他设计**

- **寄存器释放**

  寄存器数目有限，要通过合适的寄存器释放算法来处理寄存器的分配。

```java
public void tryToReleaseRegister(Value instruction, BasicBlock bb, int i) {
    Instruction nowInstruction = (Instruction) instruction;
    int flag = 0;
    for (int j = i + 1; j < bb.getInstructions().size(); j++) {
        Instruction nextInstruction = (Instruction) bb.getInstructions().get(j);
        if (nextInstruction.getOperands() == null || (nextInstruction.getOperands().size() == 0)) 
            continue;
        for (Value operand : nextInstruction.getOperands())
            if (operand == nowInstruction) 
                flag++;
    }
    if (flag == 0 && instruction.getRegister() != null) {
        instruction.clearRegister(currFunc);
    }
    if (((Instruction) instruction).getOperands() == null
            || ((Instruction) instruction).getOperands().size() == 0) {
        return;
    }
    for (Value operand : nowInstruction.getOperands()) {
        int flags = 0;
        for (int j = i + 1; j < bb.getInstructions().size(); j++) {
            Instruction nextInstruction = (Instruction) bb.getInstructions().get(j);
            if (nextInstruction.getOperands() == null || (nextInstruction.getOperands().size() == 0)) 
                continue;
            for (Value operandNext : nextInstruction.getOperands()) 
                if (operand == operandNext) 
                    flags++;
        }
        if (flags == 0 && operand != null && operand.getRegister() != null)
            operand.clearRegister(currFunc);
    }
}
```

​	我的处理方法是，在处理完一条指令时，运行上面的方法，尝试释放寄存器，如果基本块后续不再使用这个	虚拟寄存器，那就将其对应的物理寄存器也释放掉。

#### 7.2 编码后的修改

这部分也都是在优化过程中修改的，详情见优化文档

### 八、 代码优化

好吧，依然可以见优化文档。
