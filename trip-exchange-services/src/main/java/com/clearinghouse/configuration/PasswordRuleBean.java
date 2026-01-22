package com.clearinghouse.configuration;

import lombok.Getter;

@Getter
public class PasswordRuleBean {

    private final int noOfCAPSAlpha = 1;

    private final int noOfDigits = 1;

    private final int noOfSplChars = 1;

    private final int minLen = 8;

    private final int maxLen = 14;

}
