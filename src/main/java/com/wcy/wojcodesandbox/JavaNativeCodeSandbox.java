package com.wcy.wojcodesandbox;

import com.wcy.wojcodesandbox.model.ExecuteCodeRequest;
import com.wcy.wojcodesandbox.model.ExecuteCodeResponse;

public class JavaNativeCodeSandbox extends JavaCodeSandboxTemplate{

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        ExecuteCodeResponse executeCodeResponse = super.executeCode(executeCodeRequest);
        return  executeCodeResponse;
    }
}