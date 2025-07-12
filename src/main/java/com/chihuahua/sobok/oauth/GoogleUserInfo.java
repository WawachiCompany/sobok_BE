package com.chihuahua.sobok.oauth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleUserInfo {

  private String sub; // 구글 고유 ID
  private String email;
  private String name;
  private String picture; // 프로필 이미지 URL
}
