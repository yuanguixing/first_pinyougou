package com.pinyougou.user.service;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;


/**
 * 认证服务类 用于查询安全数据
 * 用户信息 用户角色信息 用户权限信息
 */

public class UserDetailServiceImpl implements UserDetailsService {


    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //根据商家登陆名查询商家信息
        TbSeller seller = sellerService.findOne(username);
        if (seller != null) {
            if ("1".equals(seller.getStatus())) {
                List<GrantedAuthority> authorities = new ArrayList<>();
                //赋权操作
                authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
                return new User(username, seller.getPassword(), authorities);
            } else {
                return null;
            }

        } else {
            return null;
        }

    }
}
