package com.steccn.ems.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class EpPartitionChildTypeDTO implements Serializable {
   /**
    * 子节点id
    */
   private Long childId;

   /**
    * 能源类型
    */
   Integer type;
}
