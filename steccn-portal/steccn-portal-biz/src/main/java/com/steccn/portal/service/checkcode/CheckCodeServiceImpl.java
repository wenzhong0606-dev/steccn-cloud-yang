package com.steccn.portal.service.checkcode;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.steccn.portal.dto.CheckCodeGenerateDTO;
import com.steccn.portal.service.checkcode.AbstractCheckCodeService;
import com.steccn.portal.service.checkcode.CheckCodeService;
import com.steccn.portal.util.EncryptUtil;
import com.steccn.portal.vo.CheckCodeGenerateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Encoder;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author yequan
 * @version 1.0
 * @desc 图片验证码生成器
 */
@Service
public class CheckCodeServiceImpl extends AbstractCheckCodeService implements CheckCodeService {


    @Autowired
    private DefaultKaptcha kaptcha;

    @Resource(name = "NumberLetterCheckCodeGenerator")
    @Override
    public void setCheckCodeGenerator(CheckCodeGenerator checkCodeGenerator) {
        this.checkCodeGenerator = checkCodeGenerator;
    }

    @Resource(name = "UUIDKeyGenerator")
    @Override
    public void setKeyGenerator(KeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }


    @Resource(name = "RedisCheckCodeStore")
    @Override
    public void setCheckCodeStore(CheckCodeStore checkCodeStore) {
        this.checkCodeStore = checkCodeStore;
    }


    @Override
    public CheckCodeGenerateVO generate(CheckCodeGenerateDTO dto) {
        GenerateResult generate = generate(dto, 4, "checkcode:", 300);
        String key = generate.getKey();
        String code = generate.getCode();
        String pic = createPic(code);
        CheckCodeGenerateVO resVo = new CheckCodeGenerateVO();
        resVo.setCode(pic);
        resVo.setKey(key);
        return resVo;
    }

    private String createPic(String code) {
        // 生成图片验证码
        ByteArrayOutputStream outputStream = null;
        BufferedImage image = kaptcha.createImage(code);

        outputStream = new ByteArrayOutputStream();
        String imgBase64Encoder = null;
        try {
            // 对字节数组Base64编码
            BASE64Encoder base64Encoder = new BASE64Encoder();
            ImageIO.write(image, "png", outputStream);
            imgBase64Encoder = "data:image/png;base64," + EncryptUtil.encodeBase64(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return imgBase64Encoder;
    }
}
