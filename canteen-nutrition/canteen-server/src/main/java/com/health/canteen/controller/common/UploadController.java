package com.health.canteen.controller.common;

import com.health.canteen.annotation.RequireRole;
import com.health.canteen.result.Result;
import com.health.canteen.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController("commonUploadController")
public class UploadController {

    @Autowired
    private FileService fileService;

    /**
     * 上传菜品图片等文件
     */
    @PostMapping("/upload")
    @RequireRole({2, 3})
    public Result upload(@RequestParam("file") MultipartFile file) {
        log.info("接收上传文件: {}", file == null ? null : file.getOriginalFilename());
        return Result.success(fileService.upload(file));
    }

}
