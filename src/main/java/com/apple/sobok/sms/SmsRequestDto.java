package com.apple.sobok.sms;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class SmsRequestDto {
    @NotBlank
    private String phoneNumber;

    private String code;
}
