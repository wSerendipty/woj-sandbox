package com.wcy.wojcodesandbox.controller;
import cn.hutool.crypto.digest.MD5;
import com.wcy.wojcodesandbox.JavaDockerCodeSandBox;
import com.wcy.wojcodesandbox.JavaNativeCodeSandbox;
import com.wcy.wojcodesandbox.model.ExecuteCodeRequest;
import com.wcy.wojcodesandbox.model.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController("/")
@Slf4j
public class MainController {

    // 定义鉴权请求头和密钥
    private static final String AUTH_REQUEST_HEADER = "Authorization";

    private static final String AUTH_REQUEST_SECRET = MD5.create().digestHex("Wcy0626..");

    @Resource
    private JavaNativeCodeSandbox javaNativeCodeSandbox;
    @Resource
    private JavaDockerCodeSandBox javaDockerCodeSandBox;


    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    @PostMapping("/executeCode")
   public ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request,
                                    HttpServletResponse response) {
        // 基本的认证
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        log.info("authHeader: " + authHeader);
        log.info("AUTH_REQUEST_SECRET: " + AUTH_REQUEST_SECRET);
        if (!AUTH_REQUEST_SECRET.equals(authHeader)) {
            response.setStatus(403);
            return null;
        }
        if (executeCodeRequest == null) {
            throw new RuntimeException("请求参数为空");
        }
        return javaNativeCodeSandbox.executeCode(executeCodeRequest);
    }

    /**
     * docker 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    @PostMapping("/executeCode/docker")
  public   ExecuteCodeResponse dockerExecuteCode(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request,
                                    HttpServletResponse response) {
        // 基本的认证
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        log.info("authHeader: " + authHeader);
        log.info("AUTH_REQUEST_SECRET: " + AUTH_REQUEST_SECRET);
        if (!AUTH_REQUEST_SECRET.equals(authHeader)) {
            response.setStatus(403);
            return null;
        }
        if (executeCodeRequest == null) {
            throw new RuntimeException("请求参数为空");
        }
        return javaDockerCodeSandBox.executeCode(executeCodeRequest);
    }

}
