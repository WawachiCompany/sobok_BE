package com.chihuahua.sobok.oauth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppleUserInfo {

  private String sub;  // Apple 사용자 고유 ID
  private String email;
  private boolean emailVerified;
  private String name;  // 첫 로그인시에만 제공
}
