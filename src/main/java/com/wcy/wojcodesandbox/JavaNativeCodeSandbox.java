package com.wcy.wojcodesandbox;

import cn.hutool.core.io.resource.ResourceUtil;
import com.wcy.wojcodesandbox.model.ExecuteCodeRequest;
import com.wcy.wojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
public class JavaNativeCodeSandbox extends JavaCodeSandboxTemplate{

    private static final String REPLACE_Code = "[wojwojwojwojwojwojwojwojwojwojwojwojwojwojwojwojwojwojwojwojwojwoj]";


    public static void main(String[] args) {
        JavaNativeCodeSandbox javaNativeCodeSandbox = new JavaNativeCodeSandbox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("[2,7,11,15]\n9", "[3,2,4]\n6", "[3,3]\n6"));
        String code = ResourceUtil.readStr("codeTemplate/Main.java", StandardCharsets.UTF_8);
        String userCode = ResourceUtil.readStr("codeTemplate/temp.java", StandardCharsets.UTF_8);
        // 替换其中的占位符
        code = code.replace(REPLACE_Code, userCode);

        System.out.println(code);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        String code = executeCodeRequest.getCode();
        String template = ResourceUtil.readStr("codeTemplate/Main.java", StandardCharsets.UTF_8);
        // 替换其中的占位符
        String finish_code = template.replace(REPLACE_Code, code);
        executeCodeRequest.setCode(finish_code);
        return super.executeCode(executeCodeRequest);
    }
}