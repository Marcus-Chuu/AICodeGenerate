package com.marcuschu.aicodegenerate;

import com.marcuschu.aicodegenerate.ai.model.HtmlCodeResult;
import com.marcuschu.aicodegenerate.ai.model.MultiFileCodeResult;
import com.marcuschu.aicodegenerate.ai.model.core.parser.CodeParserExecutor;
import com.marcuschu.aicodegenerate.ai.model.enums.CodeGenTypeEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;


class CodeParserTest {


    @Test
    void parseHtmlCode() {
        String codeContent = """
                随便写一段描述：
                html 格式
                <!DOCTYPE html>
                <html>
                <head>
                    <title>测试页面</title>
                </head>
                <body>
                    <h1>Hello World!</h1>
                </body>
                </html>

                随便写一段描述
                """;
        Object result = CodeParserExecutor.executeParser(codeContent, CodeGenTypeEnum.HTML);
        HtmlCodeResult result1 = (HtmlCodeResult) result;
        assertNotNull(result);
        assertNotNull(result1.getHtmlCode());
    }

    @Test
    void parseMultiFileCode() {
        String codeContent = """
            创建一个完整的网页：
    
            ```html
            <!DOCTYPE html>
            <html>
            <head>
                <title>多文件示例</title>
                <link rel="stylesheet" href="style.css">
            </head>
            <body>
                <h1>欢迎使用</h1>
                <script src="script.js"></script>
            </body>
            </html>
            ```
    
            ```css
            h1 {
                color: blue;
                text-align: center;
            }
            ```
    
            ```js
            console.log('页面加载完成');
            ```
    
            文件创建完成！
        """;
        Object result = CodeParserExecutor.executeParser(codeContent, CodeGenTypeEnum.MULTI_FILE);
        MultiFileCodeResult result1 = (MultiFileCodeResult) result;
        assertNotNull(result);
        assertNotNull(result1.getHtmlCode());
        assertNotNull(result1.getCssCode());
        assertNotNull(result1.getJsCode());
    }
}
