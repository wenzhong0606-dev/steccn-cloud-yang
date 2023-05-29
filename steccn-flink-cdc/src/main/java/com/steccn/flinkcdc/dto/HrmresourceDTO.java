package com.steccn.flinkcdc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * hrmresource
 *
 * @author AndrewLuo
 * @date 2023/05/26
 */
@Data
public class HrmresourceDTO {
    @JsonProperty("ID")
    private Integer id;

    /**
     *
     */
    @JsonProperty("LOGINID")
    private String loginid;

    /**
     *
     */
    @JsonProperty("PASSWORD")
    private String password;

    /**
     *
     */
    @JsonProperty("LASTNAME")
    private String lastname;

    /**
     *
     */
    @JsonProperty("SEX")
    private String sex;

    /**
     *
     */
    @JsonProperty("BIRTHDAY")
    private String birthday;

    /**
     *
     */
    @JsonProperty("NATIONALITY")
    private Integer nationality;

    /**
     *
     */
    @JsonProperty("SYSTEMLANGUAGE")
    private Integer systemlanguage;

    /**
     *
     */
    @JsonProperty("MARITALSTATUS")
    private String maritalstatus;

    /**
     *
     */
    @JsonProperty("TELEPHONE")
    private String telephone;

    /**
     *
     */
    @JsonProperty("MOBILE")
    private String mobile;

    /**
     *
     */
    @JsonProperty("MOBILECALL")
    private String mobilecall;

    /**
     *
     */
    @JsonProperty("EMAIL")
    private String email;

    /**
     *
     */
    @JsonProperty("LOCATIONID")
    private Integer locationid;

    /**
     *
     */
    @JsonProperty("WORKROOM")
    private String workroom;

    /**
     *
     */
    @JsonProperty("HOMEADDRESS")
    private String homeaddress;

    /**
     *
     */
    @JsonProperty("RESOURCETYPE")
    private String resourcetype;

    /**
     *
     */
    @JsonProperty("STARTDATE")
    private String startdate;

    /**
     *
     */
    @JsonProperty("ENDDATE")
    private String enddate;

    /**
     *
     */
    @JsonProperty("JOBTITLE")
    private Integer jobtitle;

    /**
     *
     */
    @JsonProperty("JOBACTIVITYDESC")
    private String jobactivitydesc;

    /**
     *
     */
    @JsonProperty("JOBLEVEL")
    private Integer joblevel;

    /**
     *
     */
    @JsonProperty("SECLEVEL")
    private Integer seclevel;

    /**
     *
     */
    @JsonProperty("DEPARTMENTID")
    private Integer departmentid;

    /**
     *
     */
    @JsonProperty("SUBCOMPANYID1")
    private Integer subcompanyid1;

    /**
     *
     */
    @JsonProperty("COSTCENTERID")
    private Integer costcenterid;

    /**
     *
     */
    @JsonProperty("MANAGERID")
    private Integer managerid;

    /**
     *
     */
    @JsonProperty("ASSISTANTID")
    private Integer assistantid;

    /**
     *
     */
    @JsonProperty("BANKID1")
    private Integer bankid1;

    /**
     *
     */
    @JsonProperty("ACCOUNTID1")
    private String accountid1;

    /**
     *
     */
    @JsonProperty("RESOURCEIMAGEID")
    private Integer resourceimageid;

    /**
     *
     */
    @JsonProperty("CREATERID")
    private Integer createrid;

    /**
     *
     */
    @JsonProperty("CREATEDATE")
    private String createdate;

    /**
     *
     */
    @JsonProperty("LASTMODID")
    private Integer lastmodid;

    /**
     *
     */
    @JsonProperty("LASTMODDATE")
    private String lastmoddate;

    /**
     *
     */
    @JsonProperty("LASTLOGINDATE")
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
    @JsonProperty("STATUS")
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
}
