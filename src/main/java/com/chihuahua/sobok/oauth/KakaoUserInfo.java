package com.chihuahua.sobok.oauth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoUserInfo {

  private String id; // 카카오 고유 ID (sub 역할)
  private String email;
  private String nickname;
}
