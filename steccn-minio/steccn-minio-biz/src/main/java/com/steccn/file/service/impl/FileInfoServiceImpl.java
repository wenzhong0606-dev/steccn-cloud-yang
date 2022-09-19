package com.steccn.file.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.authserver.util.SteccnUser;
import com.steccn.common.vo.R;
import com.steccn.file.domain.FileInfo;
import com.steccn.file.mapper.FileInfoMapper;
import com.steccn.file.service.FileInfoService;
import com.steccn.file.util.MinioFileUtils;
import com.steccn.minio.dto.FileInfoDto;
import com.steccn.minio.vo.FileInfoVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author MongKnife
 * @description 针对表【file_info(文件信息)】的数据库操作Service实现
 * @createDate 2022-09-05 13:34:24
 */
@Service
@RequiredArgsConstructor
public class FileInfoServiceImpl extends ServiceImpl<FileInfoMapper, FileInfo>
        implements FileInfoService {

    private final MinioFileUtils minioFileUtils;
    private final FileInfoMapper fileInfoMapper;
    private final String UTF_8 = "UTF-8";
    private final String MD5 = "MD5";
    private final String bucketName = "jeesite";


    @Override
    public R<FileInfo> uploadFile(FileInfoDto fileInfoDto) {
        //1.定义返回数据信息
        R<FileInfo> r = new R<FileInfo>();
        //2.获取文件拓展名,文件名称
        MultipartFile file = fileInfoDto.getFile();
        String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String fileName = "file/" + md5(file.getOriginalFilename() + System.currentTimeMillis()) + fileExtension;
        String url;
        //3.上传文件并保存文件信息
        try {
            url = minioFileUtils.uploadFile(fileName, file);
            FileInfo fileInfo= new FileInfo();
            BeanUtils.copyProperties(file, fileInfo);
            fileInfo.setFileName(file.getOriginalFilename());
            fileInfo.setFileUrl(url);
            fileInfo.setBucketName(bucketName);
            fileInfo.setFileExtension(fileExtension);
            fileInfo.setFileAlias(fileInfoDto.getFileName());
            fileInfo.setFileSize((double) file.getSize());
            //3.1获取当前登录用户信息并保存
            SteccnUser user = SecurityUtils.getUser();
            fileInfo.setCreateName(user.getName());
            fileInfo.setCreateBy(user.getUserId());
            fileInfo.setCreateTime(LocalDateTimeUtil.now());
            int insert = fileInfoMapper.insert(fileInfo);
            //3.1 判断是否保存成功
            if (insert >= 1) {
                r.setCode(1);
                r.setMsg("保存文件成功！");
                r.setData(fileInfo);
            } else {
                r.setCode(0);
                r.setMsg("保存文件失败！");
            }
        } catch (Exception e) {
            throw new BusinessException("上传文件异常");
        }
        return r;
    }

    @Override
    public Map<Long, FileInfoVO> listByIdList(List<Long> idList) {
        Map<Long, FileInfoVO> resMap = Maps.newHashMap();
        Collection<FileInfo> fileInfoList = listByIds(idList);
        if (CollUtil.isEmpty(fileInfoList)) {
            return resMap;
        }
        List<FileInfoVO> sysDictVOList = BeanUtil.copyToList(fileInfoList, FileInfoVO.class);
        for (FileInfoVO fileInfoVO : sysDictVOList) {
            resMap.put(fileInfoVO.getId(), fileInfoVO);
        }
        return resMap;
    }

    /**
     * 对输入字符串进行md5散列.
     *
     * @param input 加密字符串
     */
    String md5(String input) {
        return md5(input, 1);
    }

    /**
     * 对输入字符串进行md5散列.
     *
     * @param input      加密字符串
     * @param iterations 迭代次数
     */
    String md5(String input, int iterations) {
        try {
            return encodeHex(digest(input.getBytes(UTF_8), MD5, null, iterations));
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /**
     * Hex编码.
     */
    String encodeHex(byte[] input) {
        return new String(Hex.encodeHex(input));
    }

    /**
     * 对字符串进行散列, 支持md5与sha1算法.
     *
     * @param input      需要散列的字符串
     * @param algorithm  散列算法（"SHA-1"、"MD5"）
     * @param salt
     * @param iterations 迭代次数
     * @return
     */
    byte[] digest(byte[] input, String algorithm, byte[] salt, int iterations) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);

            if (salt != null) {
                digest.update(salt);
            }

            byte[] result = digest.digest(input);

            for (int i = 1; i < iterations; i++) {
                digest.reset();
                result = digest.digest(result);
            }
            return result;
        } catch (GeneralSecurityException e) {
            throw unchecked(e);
        }
    }

    /**
     * 将CheckedException转换为UncheckedException.
     */
    RuntimeException unchecked(Exception e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        } else {
            return new RuntimeException(e);
        }
    }
}




