package com.health.canteen.service.impl;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.health.canteen.exception.BusinessException;
import com.health.canteen.properties.AliyunOSSProperties;
import com.health.canteen.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 阿里云OSS对象存储：图片不落应用服务器本地，便于多实例部署与静态资源分发
 */
@Slf4j
@Service
public class AliyunOSSFileServiceImpl implements FileService {

    private static final List<String> ALLOWED_EXT = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    @Autowired
    private AliyunOSSProperties ossProperties;

    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }
        String ext = resolveExtension(file.getOriginalFilename());
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException("仅支持 jpg、jpeg、png、gif、webp 格式的图片");
        }
        if (isBlank(ossProperties.getEndpoint()) || isBlank(ossProperties.getBucketName())
                || isBlank(ossProperties.getRegion())) {
            throw new BusinessException("未配置阿里云OSS，请设置 aliyun.oss.endpoint / bucket-name / region");
        }

        //文件名由服务端生成，避免使用客户端原始文件名造成路径穿越或同名覆盖
        String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String objectName = dir + "/" + UUID.randomUUID().toString().replace("-", "") + "." + ext;

        OSS ossClient = null;
        try {
            EnvironmentVariableCredentialsProvider credentialsProvider =
                    CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
            ClientBuilderConfiguration configuration = new ClientBuilderConfiguration();
            configuration.setSignatureVersion(SignVersion.V4);
            ossClient = OSSClientBuilder.create()
                    .endpoint(ossProperties.getEndpoint())
                    .credentialsProvider(credentialsProvider)
                    .clientConfiguration(configuration)
                    .region(ossProperties.getRegion())
                    .build();
            ossClient.putObject(ossProperties.getBucketName(), objectName,
                    new ByteArrayInputStream(file.getBytes()));
        } catch (Exception e) {
            log.error("上传阿里云OSS失败, objectName: {}", objectName, e);
            throw new BusinessException("图片上传失败，请确认OSS配置正确且环境变量 "
                    + "OSS_ACCESS_KEY_ID / OSS_ACCESS_KEY_SECRET 已设置");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        String url = buildPublicUrl(objectName);
        log.info("图片已上传OSS: {}", url);
        return url;
    }

    /**
     * 拼接公网访问地址：{scheme}://{bucket}.{endpoint主机}/{objectName}
     */
    private String buildPublicUrl(String objectName) {
        String endpoint = ossProperties.getEndpoint();
        int hostStart = endpoint.indexOf("//") + 2;
        return endpoint.substring(0, hostStart) + ossProperties.getBucketName() + "."
                + endpoint.substring(hostStart) + "/" + objectName;
    }

    private String resolveExtension(String originalName) {
        if (originalName == null) {
            return "";
        }
        int index = originalName.lastIndexOf('.');
        if (index < 0 || index == originalName.length() - 1) {
            return "";
        }
        return originalName.substring(index + 1).toLowerCase();
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

}
