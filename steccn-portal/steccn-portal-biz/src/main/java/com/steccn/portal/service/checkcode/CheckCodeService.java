package com.steccn.portal.service.checkcode;

import com.steccn.portal.dto.CheckCodeGenerateDTO;
import com.steccn.portal.vo.CheckCodeGenerateVO;

/**
 * @author yequan
 * @desc 验证码接口
 */
public interface CheckCodeService {


    /**
     * @description 生成验证码
     * @param dto 生成验证码参数
    */
     CheckCodeGenerateVO generate(CheckCodeGenerateDTO dto);

     /**
      * @description 校验验证码
      * @param key
      * @param code
      * @return boolean
      * @author Mr.M
      * @date 2022/9/29 18:46
     */
    public boolean verify(String key, String code);


    /**
     * @description 验证码生成器
     * @author Mr.M
     * @date 2022/9/29 16:34
    */
    public interface CheckCodeGenerator{
        /**
         * 验证码生成
         * @return 验证码
         */
        String generate(int length);
        

    }

    /**
     * @description key生成器
     * @author Mr.M
     * @date 2022/9/29 16:34
     */
    public interface KeyGenerator{

        /**
         * key生成
         * @return 验证码
         */
        String generate(String prefix);
    }


    /**
     * @description 验证码存储
     * @author Mr.M
     * @date 2022/9/29 16:34
     */
    public interface CheckCodeStore{

        /**
         * @description 向缓存设置key
         * @param key key
         * @param value value
         * @param expire 过期时间,单位秒
         * @return void
         * @author Mr.M
         * @date 2022/9/29 17:15
        */
        void set(String key, String value, Integer expire);

        String get(String key);

        void remove(String key);
    }
}
