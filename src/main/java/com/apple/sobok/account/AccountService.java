package com.apple.sobok.account;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    public List<AccountDto> getAccountList(List<Account> result) {
        return result.stream().map(account -> {
            AccountDto dto = new AccountDto();
            dto.setId(account.getId());
            dto.setTitle(account.getTitle());
            dto.setTarget(account.getTarget());
            dto.setIsPublic(account.getIsPublic());
            dto.setTime(account.getTime());
            dto.setDuration(account.getDuration());
            dto.setIsValid(account.getIsValid());
            dto.setInterest(account.getInterest());
            return dto;
        }).collect(Collectors.toList());

    }

}
