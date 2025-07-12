package com.chihuahua.sobok.member;

import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

  private final MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    var result = memberRepository.findByUsername(username);
    if (result.isEmpty()) {
      throw new UsernameNotFoundException("그런 아이디 없음");
    }
    var user = result.get();

    CustomUser customUser = new CustomUser(user.getUsername(), user.getPassword());
    customUser.id = user.getId();
    customUser.name = user.getName();
    customUser.displayName = user.getDisplayName();
    customUser.birth = user.getBirth();
    customUser.email = user.getEmail();
    customUser.phoneNumber = user.getPhoneNumber();
    customUser.point = user.getPoint();
    return customUser;
  }

  public static class CustomUser extends User {

    public Long id;
    public String name;
    public String displayName;
    public String birth;
    public String email;
    public String phoneNumber;
    public Integer point;

    public CustomUser(String username, String password) {
      super(username, password, new ArrayList<>());
    }
  }
}
