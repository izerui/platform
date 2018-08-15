package com.ierp2.mrp.service;

import com.ierp2.mrp.dao.*;
import com.ierp2.mrp.entity.*;
import com.ierp2.mrp.support.jpa.impl.Conditions;
import com.ierp2.mrp.support.security.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Service
@Transactional
public class RbacService implements UserDetailsService {


    @Autowired
    UserDao userDao;
    @Autowired
    UserRoleDao userRoleDao;
    @Autowired
    RoleDao roleDao;
    @Autowired
    RoleResourceDao roleResourceDao;
    @Autowired
    ResourceDao resourceDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return loadUserByAccount(username);
    }

    private UserDetails loadUserByAccount(String account) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Boolean swithUser = (Boolean) request.getAttribute("switchUser");
        if (swithUser != null && swithUser) {
            account = (String) request.getAttribute("account");
        }
        //获取账号下多个账套
        Conditions conditions = Conditions.where("account").is(account).and("recordStatus").is(1);
        List<User> users = userDao.findAll(conditions);
        if (users != null && users.size() > 0) {
            String entCode = (String) request.getAttribute("entCode");
            //指定登录的账套
            if (isNotEmpty(entCode)) {
                Optional<User> first = users.stream().filter(user -> user.getEntCode().equals(entCode)).findFirst();
                if (first.isPresent()) {
                    return createUser(first.get());
                }
            }
            //取第一个可用的账套
            return createUser(users.get(0));
        }
        //没有可用的账套
        throw new UsernameNotFoundException("用户登录失败");
    }

    private UserSession createUser(User user) {
        //加载用户权限
        Set<String> authorities = new HashSet<>();
        if (user != null) {
            authorities = this.findAuthorities(user);
        }
        UserSession userSession = new UserSession(user.getAccount(),
                user.getPassword(),
                user.getRecordStatus() == 1,
                true,
                true,
                true,
                AuthorityUtils.createAuthorityList(authorities.toArray(new String[authorities.size()]))
        );
        //账套权限 权限集合
        userSession.setAccount(user.getAccount());
        userSession.setUserCode(user.getUserCode());
        userSession.setEntCode(user.getEntCode());
        return userSession;
    }


    private Set<String> findAuthorities(User user) {
        Set<String> authorities = new HashSet<>();
        if (user.getAdmin() == 1) {
            List<Resource> resources = this.findResources();
            for (Resource resource : resources) {
                authorities.add(resource.getResourceCode());
            }
        } else {
            List<Role> roles = this.findRolesByUserCode(user.getEntCode(), user.getUserCode());
            for (Role role : roles) {
                List<Resource> resources = this.findResourcesByRoleCode(role.getEntCode(), role.getRoleCode());
                for (Resource resource : resources) {
                    authorities.add(resource.getResourceCode());
                }
            }
        }
        return authorities;
    }

    public List<Resource> findResourcesByRoleCode(String entCode, String roleCode) {
        List<RoleResource> roleResources = roleResourceDao.findByEntCodeAndRoleCode(entCode, roleCode);
        List<String> resourceCodes = roleResources.stream().map(RoleResource::getResourceCode).collect(Collectors.toList());
        Conditions conditions = Conditions
                .where("resourceCode").in(resourceCodes);
        return resourceDao.findAll(conditions);
    }

    public List<Role> findRolesByUserCode(String entCode, String userCode) {
        List<UserRole> userRoles = userRoleDao.findByEntCodeAndUserCode(entCode, userCode);
        List<String> roleCodes = userRoles.stream().map(UserRole::getRoleCode).collect(Collectors.toList());
        Conditions conditions = Conditions
                .where("entCode").is(entCode)
                .and("roleCode").in(roleCodes);
        return roleDao.findAll(conditions);
    }

    public List<Resource> findResources() {
        return resourceDao.findAll();
    }

}
