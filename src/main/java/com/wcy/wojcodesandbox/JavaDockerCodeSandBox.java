package com.wcy.wojcodesandbox;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.wcy.wojcodesandbox.model.ExecuteCodeRequest;
import com.wcy.wojcodesandbox.model.ExecuteCodeResponse;
import com.wcy.wojcodesandbox.model.ExecuteMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author 王长远
 * @version 1.0
 * @date 2024/1/16 15:59
 */
@Component
@Slf4j
public class JavaDockerCodeSandBox extends JavaCodeSandboxTemplate {
    private static final long TIME_OUT = 5000L;
    private static final String JAVA_IMAGE = "openjdk:8-alpine";

    private static final long JAVA_MEMORY = 100 * 1000 * 1000L;

    private static final String ROOT_URL = "/app";

    private static final String MAIN_CLASS = "Main";

    private static String[] JAVA_RUN_ORDER = new String[]{"java", "-cp", ROOT_URL, MAIN_CLASS};

    private static String CONTAINER_ID = null;

    private static String IMAGE_ID = null;

    private static final String REPLACE_Code = "[wojwojwojwojwojwojwojwojwojwojwojwojwojwojwojwojwojwojwojwojwojwoj]";

    public static void main(String[] args) {
        JavaDockerCodeSandBox javaDockerCodeSandBox = new JavaDockerCodeSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("[2,7,11,15]\n9", "[3,2,4]\n6", "[3,3]\n6"));

        System.out.println(Arrays.toString(executeCodeRequest.getInputList().toArray()));
        String code = ResourceUtil.readStr("codeTemplate/Main.java", StandardCharsets.UTF_8);
        String userCode = ResourceUtil.readStr("codeTemplate/temp.java", StandardCharsets.UTF_8);
        // 替换其中的占位符
        code = code.replace(REPLACE_Code, userCode);

        System.out.println(code);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        ExecuteCodeResponse executeCodeResponse = javaDockerCodeSandBox.executeCode(executeCodeRequest);
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

    @Override
    public List<ExecuteMessage> runCode(File userCodeFile, List<String> inputList) {
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        // 获取默认的 Docker Client
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();

        ListImagesCmd listImagesCmd = dockerClient.listImagesCmd();
        List<Image> imageList = listImagesCmd.exec();
        for (Image image : imageList) {
            String[] repoTags = image.getRepoTags();
            for (String repoTag : repoTags) {
                if (JAVA_IMAGE.equals(repoTag)) {
                    IMAGE_ID = image.getId();
                    break;
                }
            }
        }


        // 拉取镜像
//        if (init) {
//            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(JAVA_IMAGE);
//            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
//                @Override
//                public void onNext(PullResponseItem item) {
//                    System.out.println("下载镜像：" + item.getStatus());
//                    super.onNext(item);
//                }
//            };
//            try {
//                pullImageCmd
//                        .exec(pullImageResultCallback)
//                        .awaitCompletion();
//            } catch (InterruptedException e) {
//                System.out.println("拉取镜像异常");
//                throw new RuntimeException(e);
//            }
//        }

        // 创建容器
        // 查看该镜像下的所有容器
        ListContainersCmd listContainersCmd = dockerClient.listContainersCmd().withShowAll(true).withFilter("ancestor", new ArrayList<>(Collections.singletonList(IMAGE_ID)));

        List<Container> containerList = listContainersCmd.exec();

        if (containerList.size() > 0) {
            CONTAINER_ID = containerList.get(0).getId();
        }
        if (CONTAINER_ID == null) {
            CreateContainerCmd containerCmd = dockerClient.createContainerCmd(JAVA_IMAGE);
            HostConfig hostConfig = new HostConfig();
            hostConfig.withMemory(JAVA_MEMORY);
            hostConfig.withMemorySwap(0L);
            hostConfig.withCpuCount(1L);

            // 挂载文件
            hostConfig.setBinds(new Bind(userCodeParentPath, new Volume(ROOT_URL)));
            CreateContainerResponse createContainerResponse = containerCmd
                    .withHostConfig(hostConfig)
                    .withNetworkDisabled(true)
                    .withAttachStdin(true)
                    .withAttachStderr(true)
                    .withAttachStdout(true)
                    .withTty(true)
                    .exec();
            CONTAINER_ID = createContainerResponse.getId();
            dockerClient.startContainerCmd(CONTAINER_ID).exec();
        } else {
            // 启动容器
            String status = containerList.get(0).getStatus();
            if (!status.contains("Up")) {
                dockerClient.startContainerCmd(CONTAINER_ID).exec();
            }

            // 拷贝文件到容器
            dockerClient.copyArchiveToContainerCmd(CONTAINER_ID)
                    .withHostResource(userCodeParentPath)
                    .withRemotePath(ROOT_URL)
                    .exec();
        }

        // docker exec keen_blackwell java -cp /app Main 1 3
        // 执行命令并获取结果
        JAVA_RUN_ORDER[2] = ROOT_URL + File.separator + userCodeFile.getParentFile().getName();
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            StopWatch stopWatch = new StopWatch();
            String[] inputArgsArray = inputArgs.split("\n");
            String[] cmdArray = ArrayUtil.append(JAVA_RUN_ORDER, inputArgsArray);
            System.out.println("执行命令：" + Arrays.toString(cmdArray));
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(CONTAINER_ID)
                    .withCmd(cmdArray)
                    .withAttachStderr(true)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .exec();
            System.out.println("创建执行命令：" + execCreateCmdResponse);

            ExecuteMessage executeMessage = new ExecuteMessage();
            long time = 0L;
            // 判断是否超时
            String execId = execCreateCmdResponse.getId();
            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
                @Override
                public void onComplete() {
                    // 如果执行完成，则表示没超时
                    System.out.println("执行完成");
                    super.onComplete();
                }

                @Override
                public void onNext(Frame frame) {
                    StreamType streamType = frame.getStreamType();
                    if (StreamType.STDERR.equals(streamType)) {
                        executeMessage.setErrorMessage(new String(frame.getPayload()));
                        System.out.println("输出错误结果：" + new String(frame.getPayload()));
                    } else {
                        String payload = new String(frame.getPayload());
                        executeMessage.setMessage(StringUtils.chomp(payload));
                        System.out.println("输出结果：" + new String(frame.getPayload()));
                    }
                    super.onNext(frame);
                }
            };


            // 获取占用的内存
            StatsCmd statsCmd = dockerClient.statsCmd(CONTAINER_ID);
            ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {

                @Override
                public void onNext(Statistics statistics) {
                    long memory = executeMessage.getMemory() == null ? 0L : executeMessage.getMemory();
                    executeMessage.setMemory(Math.max(statistics.getMemoryStats().getUsage(), memory));
                }

                @Override
                public void close() {
                    log.info("获取内存占用完成");
                }

                @Override
                public void onStart(Closeable closeable) {

                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onComplete() {

                }
            });
            statsCmd.exec(statisticsResultCallback);
            try {
                stopWatch.start();
                dockerClient.execStartCmd(execId)
                        .exec(execStartResultCallback)
                        .awaitCompletion(TIME_OUT, TimeUnit.MICROSECONDS);
                stopWatch.stop();
                time = stopWatch.getLastTaskTimeMillis();
                statsCmd.close();
            } catch (InterruptedException e) {
                System.out.println("程序执行异常");
                throw new RuntimeException(e);
            }

            executeMessage.setTime(time);
            executeMessageList.add(executeMessage);
        }
        // 关闭容器
        dockerClient.stopContainerCmd(CONTAINER_ID).exec();
//        // 删除容器
//        dockerClient.removeContainerCmd(CONTAINER_ID).exec();
        return executeMessageList;
    }


}
