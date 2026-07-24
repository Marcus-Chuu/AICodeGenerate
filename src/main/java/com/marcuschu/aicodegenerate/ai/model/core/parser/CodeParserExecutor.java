package com.marcuschu.aicodegenerate.ai.model.core.parser;


import com.marcuschu.aicodegenerate.ai.model.enums.CodeGenTypeEnum;
import com.marcuschu.aicodegenerate.exception.BusinessException;
import com.marcuschu.aicodegenerate.exception.ErrorCode;

/**
 * 代码解析执行器
 */
public class CodeParserExecutor {

    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();

    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();

    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML -> htmlCodeParser.parseCode(codeContent);
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenTypeEnum);
        };
    }


}
