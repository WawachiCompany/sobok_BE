package com.chihuahua.sobok.oauth;

import lombok.Data;

@Data
public class AppleNativeLoginRequest {

  private String identityToken;  // Apple에서 받은 JWT identity token (필수)
  // authorizationCode와 user 필드 제거 - 현재 구현에서 사용되지 않음
}
