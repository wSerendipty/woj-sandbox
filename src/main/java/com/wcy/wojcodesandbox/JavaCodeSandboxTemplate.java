package com.wcy.wojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.wcy.wojcodesandbox.model.*;
import com.wcy.wojcodesandbox.util.ProcessUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 王长远
 * @version 1.0
 * @date 2024/1/16 11:15
 */
@Slf4j
public abstract class JavaCodeSandboxTemplate implements CodeSandbox {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    private static final String GLOBAL_JAVA_COMPILE_ORDER = "javac -encoding utf-8 %s";

    private static final String GLOBAL_JAVA_RUN_ORDER = "java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s";

    private static final long TIME_OUT = 3000L;

    private static final String COMPILE = "编译";
    private static final String RUN = "运行";

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        // 1. 保存代码到文件
        File userCodeFile = saveCodeToFile(code);
        // 2. 编译代码
        ExecuteMessage executeMessage = compileCode(userCodeFile);

        if (StrUtil.isNotBlank(executeMessage.getErrorMessage())) {
            // 编译失败
            ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
            executeCodeResponse.setStatus(ExecuteStatusEnum.COMPILE_ERROR.getCode());
            executeCodeResponse.setMessage(executeMessage.getErrorMessage());
            // 文件清理
            boolean cleanFile = cleanFile(userCodeFile);
            if (!cleanFile) {
                log.error("deleteFile error, userCodeFilePath = {}", userCodeFile.getAbsolutePath());
            }
            return executeCodeResponse;
        }

        // 3. 执行代码
        List<ExecuteMessage> executeMessageList = runCode(userCodeFile, inputList);

        // 4. 整理执行结果
        ExecuteCodeResponse executeCodeResponse = getExecuteCodeResponse(executeMessageList);

        // 5. 文件清理
        boolean cleanFile = cleanFile(userCodeFile);
        if (!cleanFile) {
            log.error("deleteFile error, userCodeFilePath = {}", userCodeFile.getAbsolutePath());
        }
        return executeCodeResponse;
    }

    /**
     * 保存代码到文件
     */
    public File saveCodeToFile(String code) {
        // 获取当前工作目录
        String userDir = System.getProperty("user.dir");
        // 全局代码目录
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        // 判断全局代码目录是否存在，没有则新建
        if (!FileUtil.exist(globalCodePathName)) {
            File mkdir = FileUtil.mkdir(globalCodePathName);
            if (!mkdir.exists()) {
                throw new RuntimeException("创建全局代码目录失败");
            }
        }
        // 利用UUID生成文件目录
        String userCodeParentPathName = globalCodePathName + File.separator + UUID.randomUUID();
        // 生成存放代码的文件名
        String userCodeFileName = userCodeParentPathName + File.separator + GLOBAL_JAVA_CLASS_NAME;
        // 写入代码到文件
        File file = FileUtil.writeUtf8String(code, userCodeFileName);
        return file;
    }

    /**
     * 编译代码
     */
    public ExecuteMessage compileCode(File userCodeFile) {
        String compileOrder = String.format(GLOBAL_JAVA_COMPILE_ORDER, userCodeFile.getAbsolutePath());
        Runtime runtime = Runtime.getRuntime();
        try {
            Process compileProcess = runtime.exec(compileOrder);
            ExecuteMessage executeMessage = ProcessUtil.runProcessAndGetMessage(compileProcess, COMPILE);
            return executeMessage;
        } catch (Exception e) {
            throw new RuntimeException("编译失败");
        }
    }

    /**
     * 执行代码
     */
    public List<ExecuteMessage> runCode(File userCodeFile, List<String> inputList) {
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        List<ExecuteMessage> executeMessages = new ArrayList<>();
        try {
            for (String inputArgs : inputList) {
                inputArgs = inputArgs.replace("\n", " ");
                System.out.println(inputArgs);
                String runOrder = String.format(GLOBAL_JAVA_RUN_ORDER, userCodeParentPath, inputArgs);
                Runtime runtime = Runtime.getRuntime();
                Process runProcess = runtime.exec(runOrder);
                new Thread(() -> {
                    try {
                        Thread.sleep(TIME_OUT);
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
                ExecuteMessage executeMessage = ProcessUtil.runProcessAndGetMessage(runProcess, RUN);
                System.out.println(executeMessage);
                executeMessages.add(executeMessage);
            }
        } catch (Exception e) {
            throw new RuntimeException("运行失败", e);
        }
        return executeMessages;
    }

    /**
     * 整理执行结果
     */
    public ExecuteCodeResponse getExecuteCodeResponse(List<ExecuteMessage> executeMessageList) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        // 取最大时间
        long maxTime = 0;
        long maxMemory = 0;
        for (ExecuteMessage executeMessage : executeMessageList) {
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                executeCodeResponse.setMessage(errorMessage);
                // 用户提交的代码执行中存在错误
                executeCodeResponse.setStatus(ExecuteStatusEnum.RUNTIME_ERROR.getCode());
                break;
            }
            outputList.add(executeMessage.getMessage());
            Long time = executeMessage.getTime();
            Long memory = executeMessage.getMemory();
            if (time != null) {
                maxTime = Math.max(maxTime, time);
            }
            if (memory != null) {
                maxMemory = Math.max(maxMemory, memory);
            }
        }
        // 正常运行结束
        if (executeCodeResponse.getStatus() == null) {
            executeCodeResponse.setStatus(ExecuteStatusEnum.SUCCESS.getCode());
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime - 300 > 0 ? maxTime - 300 : 0);
        judgeInfo.setMemory(maxMemory);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }

    /**
     * 文件清理
     */
    public boolean cleanFile(File userCodeFile) {
        if (userCodeFile.getParentFile() != null) {
            String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
            return del;
        }
        return true;
    }


}
