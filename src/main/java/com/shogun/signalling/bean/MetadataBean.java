package com.shogun.signalling.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class MetadataBean {
    private String callStatus;
    private Long duration;
}
