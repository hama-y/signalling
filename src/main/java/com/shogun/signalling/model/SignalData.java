package com.shogun.signalling.model;

import lombok.Data;

import java.util.Map;

@Data
public class SignalData {
	
	private String type;
	private Map<String,Object> data;
}
