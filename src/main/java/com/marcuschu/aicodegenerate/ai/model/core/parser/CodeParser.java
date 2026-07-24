package com.marcuschu.aicodegenerate.ai.model.core.parser;

/**
 * 代码解析器
 */
public interface CodeParser<T> {

    /**
     * 解析代码内容
     * @param codeContent 原始代码内容
     * @return 解析后的对象结果
     */
    T parseCode(String codeContent);

}
