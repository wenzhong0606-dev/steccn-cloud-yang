package com.steccn.pms.vo;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class MesClassificationVO {

  private List<ClassificationVO> list;
}
