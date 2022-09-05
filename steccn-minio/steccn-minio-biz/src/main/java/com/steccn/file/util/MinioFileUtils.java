package com.steccn.file.util;


import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * 使用minio 实现文件上传下载
 *
 * @author Mongknife
 * @date 2022/9/5
 */
@Service
public class MinioFileUtils {

    @Autowired
    private MinioProperties properties;
    /**
     * 获取minio 客户端实例
     *
     * @return
     */
    public MinioClient getMinioClient() {
        MinioClient minioClient = null;
        try {
            minioClient = new MinioClient(properties.getUrl(), properties.getAccessKey(), properties.getSecretKey());
        } catch (InvalidEndpointException e) {
            e.printStackTrace();
        } catch (InvalidPortException e) {
            e.printStackTrace();
        }
        return minioClient;
    }

    /**
     * 上传文件
     *
     * @param multipartFile
     * @return
     */
    public String uploadFile(String fileName, MultipartFile multipartFile) {
        try {
            MinioClient minioClient = getMinioClient();
            PutObjectOptions options = new PutObjectOptions(multipartFile.getSize(), -1L);
            minioClient.putObject(properties.getBucketName(), fileName, multipartFile.getInputStream(), options);
            String url = minioClient.getObjectUrl(properties.getBucketName(), fileName);
            return url;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("文件上传失败");
        }
    }

    /**
     * 获取文件输入流
     *
     * @param fileName
     * @return
     */
    public InputStream getInputStream(String fileName) {
        InputStream is = null;
        try {
            MinioClient minioClient = getMinioClient();
            //获取对象数据
            is = minioClient.getObject(properties.getBucketName(), fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return is;
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(String path) {
        try {
            MinioClient minioClient = getMinioClient();
            minioClient.removeObject(properties.getBucketName(), path);
            return true;
        } catch (IOException | ErrorResponseException | InsufficientDataException | InternalException | InvalidBucketNameException | InvalidKeyException | InvalidResponseException | NoSuchAlgorithmException | XmlParserException e) {
            e.printStackTrace();
            return false;
        }
    }
}
