package com.steccn.oa.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName hrmresource
 */
@TableName(value ="hrmresource")
@Data
public class Hrmresource implements Serializable {
    /**
     * 
     */
    @TableId
    private Integer id;

    /**
     * 
     */
    private String loginid;

    /**
     * 
     */
    private String password;

    /**
     * 
     */
    private String lastname;

    /**
     * 
     */
    private String sex;

    /**
     * 
     */
    private String birthday;

    /**
     * 
     */
    private Integer nationality;

    /**
     * 
     */
    private Integer systemlanguage;

    /**
     * 
     */
    private String maritalstatus;

    /**
     * 
     */
    private String telephone;

    /**
     * 
     */
    private String mobile;

    /**
     * 
     */
    private String mobilecall;

    /**
     * 
     */
    private String email;

    /**
     * 
     */
    private Integer locationid;

    /**
     * 
     */
    private String workroom;

    /**
     * 
     */
    private String homeaddress;

    /**
     * 
     */
    private String resourcetype;

    /**
     * 
     */
    private String startdate;

    /**
     * 
     */
    private String enddate;

    /**
     * 
     */
    private Integer jobtitle;

    /**
     * 
     */
    private String jobactivitydesc;

    /**
     * 
     */
    private Integer joblevel;

    /**
     * 
     */
    private Integer seclevel;

    /**
     * 
     */
    private Integer departmentid;

    /**
     * 
     */
    private Integer subcompanyid1;

    /**
     * 
     */
    private Integer costcenterid;

    /**
     * 
     */
    private Integer managerid;

    /**
     * 
     */
    private Integer assistantid;

    /**
     * 
     */
    private Integer bankid1;

    /**
     * 
     */
    private String accountid1;

    /**
     * 
     */
    private Integer resourceimageid;

    /**
     * 
     */
    private Integer createrid;

    /**
     * 
     */
    private String createdate;

    /**
     * 
     */
    private Integer lastmodid;

    /**
     * 
     */
    private String lastmoddate;

    /**
     * 
     */
    private String lastlogindate;

    /**
     * 
     */
    private String datefield1;

    /**
     * 
     */
    private String datefield2;

    /**
     * 
     */
    private String datefield3;

    /**
     * 
     */
    private String datefield4;

    /**
     * 
     */
    private String datefield5;

    /**
     * 
     */
    private Double numberfield1;

    /**
     * 
     */
    private Double numberfield2;

    /**
     * 
     */
    private Double numberfield3;

    /**
     * 
     */
    private Double numberfield4;

    /**
     * 
     */
    private Double numberfield5;

    /**
     * 
     */
    private String textfield1;

    /**
     * 
     */
    private String textfield2;

    /**
     * 
     */
    private String textfield3;

    /**
     * 
     */
    private String textfield4;

    /**
     * 
     */
    private String textfield5;

    /**
     * 
     */
    private Integer tinyintfield1;

    /**
     * 
     */
    private Integer tinyintfield2;

    /**
     * 
     */
    private Integer tinyintfield3;

    /**
     * 
     */
    private Integer tinyintfield4;

    /**
     * 
     */
    private Integer tinyintfield5;

    /**
     * 
     */
    private String certificatenum;

    /**
     * 
     */
    private String nativeplace;

    /**
     * 
     */
    private Integer educationlevel;

    /**
     * 
     */
    private String bememberdate;

    /**
     * 
     */
    private String bepartydate;

    /**
     * 
     */
    private String workcode;

    /**
     * 
     */
    private String regresidentplace;

    /**
     * 
     */
    private String healthinfo;

    /**
     * 
     */
    private String residentplace;

    /**
     * 
     */
    private String policy;

    /**
     * 
     */
    private String degree;

    /**
     * 
     */
    private String height;

    /**
     * 
     */
    private Integer usekind;

    /**
     * 
     */
    private Integer jobcall;

    /**
     * 
     */
    private String accumfundaccount;

    /**
     * 
     */
    private String birthplace;

    /**
     * 
     */
    private String folk;

    /**
     * 
     */
    private String residentphone;

    /**
     * 
     */
    private String residentpostcode;

    /**
     * 
     */
    private String extphone;

    /**
     * 
     */
    private String managerstr;

    /**
     * 
     */
    private Integer status;

    /**
     * 
     */
    private String fax;

    /**
     * 
     */
    private String islabouunion;

    /**
     * 
     */
    private Integer weight;

    /**
     * 
     */
    private String tempresidentnumber;

    /**
     * 
     */
    private String probationenddate;

    /**
     * 
     */
    private Integer countryid;

    /**
     * 
     */
    private String passwdchgdate;

    /**
     * 
     */
    private Integer needusb;

    /**
     * 
     */
    private String serial;

    /**
     * 
     */
    private String account;

    /**
     * 
     */
    private String lloginid;

    /**
     * 
     */
    private Integer needdynapass;

    /**
     * 
     */
    private Double dsporder;

    /**
     * 
     */
    private Integer passwordstate;

    /**
     * 
     */
    private Integer accounttype;

    /**
     * 
     */
    private Integer belongto;

    /**
     * 
     */
    private String dactylogram;

    /**
     * 
     */
    private String assistantdactylogram;

    /**
     * 
     */
    private Integer passwordlock;

    /**
     * 
     */
    private Integer sumpasswordwrong;

    /**
     * 
     */
    private String oldpassword1;

    /**
     * 
     */
    private String oldpassword2;

    /**
     * 
     */
    private String msgstyle;

    /**
     * 
     */
    private String messagerurl;

    /**
     * 
     */
    private String pinyinlastname;

    /**
     * 
     */
    private String tokenkey;

    /**
     * 
     */
    private String userusbtype;

    /**
     * 
     */
    private String outkey;

    /**
     * 
     */
    private String adsjgs;

    /**
     * 
     */
    private String adgs;

    /**
     * 
     */
    private String adbm;

    /**
     * 
     */
    private Integer mobileshowtype;

    /**
     * 
     */
    private Integer usbstate;

    /**
     * 
     */
    private Double totalspace;

    /**
     * 
     */
    private Double occupyspace;

    /**
     * 
     */
    private String ecologyPinyinSearch;

    /**
     * 
     */
    private String isadaccount;

    /**
     * 
     */
    private String accountname;

    /**
     * 
     */
    private String haschangepwd;

    /**
     * 
     */
    private Date created;

    /**
     * 
     */
    private Integer creater;

    /**
     * 
     */
    private Date modified;

    /**
     * 
     */
    private Integer modifier;

    /**
     * 
     */
    private Date passwordlocktime;

    /**
     * 
     */
    private String mobilecaflag;

    /**
     * 
     */
    private String salt;

    /**
     * 
     */
    private String companystartdate;

    /**
     * 
     */
    private String workstartdate;

    /**
     * 
     */
    private String secondarypwd;

    /**
     * 
     */
    private Integer usesecondarypwd;

    /**
     * 
     */
    private String classification;

    /**
     * 
     */
    private String uuid;

    /**
     * 
     */
    private String passwordlockreason;

    /**
     * 
     */
    private BigDecimal companyworkyear;

    /**
     * 
     */
    private BigDecimal workyear;

    /**
     * 
     */
    private String dismissdate;

    /**
     * 
     */
    private String enckey;

    /**
     * 
     */
    private String crc;

    /**
     * 
     */
    private Integer usbscope;

    /**
     * 
     */
    private String tenantKey;

    /**
     * 
     */
    private String clauthtype;

    /**
     * 
     */
    private String hashdata;

    /**
     * 
     */
    private String signdata;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}