package com.health.canteen.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    /**
     * 保存上传文件并返回访问地址
     */
    String upload(MultipartFile file);

}
