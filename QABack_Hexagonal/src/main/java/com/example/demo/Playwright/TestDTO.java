package com.example.demo.Playwright;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestDTO {
    private Long regionId;
    private Long componentId;
    private String username;
    private String password;
    private ProductFlowType productFlowType;
    private String carrier;
    private String category;
    private String product;
    private boolean phoneNumberEnabled;
    private boolean clerkIdEnabled;
    private String phoneNumber;
    private String amount;
    private String clerkId;
}
