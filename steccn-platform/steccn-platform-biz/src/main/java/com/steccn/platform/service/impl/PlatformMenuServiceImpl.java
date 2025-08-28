package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.constant.DcsConstant;
import com.steccn.platform.domain.PlatformMenu;
import com.steccn.platform.domain.PlatformOffice;
import com.steccn.platform.domain.PlatformRoleMenu;
import com.steccn.platform.domain.PlatformUserRole;
import com.steccn.platform.dto.PlatformMenuDTO;
import com.steccn.platform.dto.PlatformOfficeDTO;
import com.steccn.platform.dto.PlatformRoleMenuDTO;
import com.steccn.platform.mapper.PlatformRoleMenuMapper;
import com.steccn.platform.mapper.PlatformUserRoleMapper;
import com.steccn.platform.service.PlatformMenuService;
import com.steccn.platform.mapper.PlatformMenuMapper;
import com.steccn.platform.vo.PlatformMenuVO;
import com.steccn.platform.vo.PlatformOfficeVO;
import com.steccn.platform.vo.PlatformRoleVO;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import java.security.*;
import java.util.Date;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import java.net.URLEncoder;

/**
* @author Admin
* @description 针对表【platform_menu(菜单表)】的数据库操作Service实现
* @createDate 2025-08-05 13:32:56
*/
@Service
@RequiredArgsConstructor
public class PlatformMenuServiceImpl extends ServiceImpl<PlatformMenuMapper, PlatformMenu>
    implements PlatformMenuService{
    private final PlatformUserRoleMapper platformUserRoleMapper;
    private final PlatformRoleMenuMapper platformRoleMenuMapper;

    @Override
    public Boolean add(PlatformMenuDTO dto) {
        if(Objects.isNull(dto.getParentId())){
            dto.setParentId(OaConstant.NO);
        }else {
            PlatformMenu byId = getById(dto.getParentId());
            if(Objects.isNull(byId)){
                throw new BusinessException("父级节点不存在");
            }
        }
//        PlatformMenu one = getOne(new LambdaQueryWrapper<PlatformMenu>().eq(PlatformMenu::getMenuCode, dto.getMenuCode()),false);
//        if(Objects.nonNull(one)){
//            throw new BusinessException("编码重复");
//        }
        PlatformMenu model = new PlatformMenu();
        BeanUtil.copyProperties(dto,model);
        save(model);
        return true;
    }

    @Override
    public Boolean update(PlatformMenuDTO dto) {
        if(Objects.isNull(dto.getId())){
            throw new BusinessException("主键id不能为空");
        }
        if(Objects.isNull(dto.getParentId())||dto.getParentId().equals(OaConstant.NO)){
            dto.setParentId(OaConstant.NO);
        }else {
            PlatformMenu byId = getById(dto.getParentId());
            if(Objects.isNull(byId)){
                throw new BusinessException("父级节点不存在");
            }
        }
        PlatformMenu model = new PlatformMenu();
        BeanUtil.copyProperties(dto,model);
        //编码无法修改
//        model.setMenuCode(null);
        updateById(model);
        return true;
    }

    @Override
    public Boolean deleteById(Set<Integer> ids) {
        removeByIds(ids);
        return true;
    }

    @Override
    public PlatformMenuVO getTreeById(Integer id) {
        List<PlatformMenu> list = list();
        List<PlatformMenuVO> results = BeanUtil.copyToList(list, PlatformMenuVO.class);
        PlatformMenuVO platformOfficeVO = buildSubTree(results, id);
        return platformOfficeVO;
    }

    @Override
    public List<PlatformMenuVO> getTreeList() {

        List<PlatformMenu> list = list();
        List<PlatformMenuVO> data = BeanUtil.copyToList(list, PlatformMenuVO.class);
        List<PlatformMenuVO> parent = data.stream().filter(v -> v.getParentId().equals(OaConstant.NO))
                .sorted(Comparator.comparing(PlatformMenuVO::getSort, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());

        List<PlatformMenuVO> results = Lists.newArrayList();
        parent.forEach(tmp->{
            PlatformMenuVO platformOfficeVO = buildSubTree(data, tmp.getId());
            results.add(platformOfficeVO);
        });
        return results;
    }

    @Override
    public List<PlatformMenuVO> getUserTreeList() {
        if(SecurityUtils.getUser().getUsername().equals("system")){
            List<PlatformMenuVO> treeList = getTreeList();
            return treeList;
        }
        List<PlatformUserRole> platformUserRoles = platformUserRoleMapper.selectList(new LambdaQueryWrapper<PlatformUserRole>()
                .eq(PlatformUserRole::getUserId, SecurityUtils.getUser().getUserId()));
        List<PlatformMenu> list = Lists.newArrayList();
        if(CollUtil.isNotEmpty(platformUserRoles)){
            List<PlatformRoleMenu> platformRoleMenus = platformRoleMenuMapper.selectList(new LambdaQueryWrapper<PlatformRoleMenu>()
                    .in(PlatformRoleMenu::getRoleId, platformUserRoles.stream().map(PlatformUserRole::getRoleId).collect(Collectors.toSet())));
            if(CollUtil.isNotEmpty(platformRoleMenus)){
                list = listByIds(platformRoleMenus.stream().map(PlatformRoleMenu::getMenuId).collect(Collectors.toSet()));
            }
        }
        List<PlatformMenuVO> results = Lists.newArrayList();
        if(CollUtil.isEmpty(list)){
            return results;
        }
        List<PlatformMenuVO> data = BeanUtil.copyToList(list, PlatformMenuVO.class);
        List<PlatformMenuVO> parent = data.stream().filter(v -> v.getParentId().equals(OaConstant.NO))
                .sorted(Comparator.comparing(PlatformMenuVO::getSort, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());
        parent.forEach(tmp->{
            PlatformMenuVO platformOfficeVO = buildSubTree(data, tmp.getId());
            results.add(platformOfficeVO);
        });
        return results;
    }

    @Override
    public List<PlatformMenuVO> getBindTreeList(PlatformRoleMenuDTO dto) {
        Assert.notNull(dto.getRoleId(),"角色id不能为空");
        List<PlatformMenuVO> resultList = Lists.newArrayList();
        //找到用户绑定的角色
        List<PlatformRoleMenu> list = platformRoleMenuMapper.selectList(new LambdaQueryWrapper<PlatformRoleMenu>()
                .eq(PlatformRoleMenu::getRoleId, dto.getRoleId()));
        if(CollUtil.isNotEmpty(list)){
//            Map<Integer, PlatformRoleMenu> collect = list.stream().collect(Collectors.toMap(PlatformRoleMenu::getMenuId, Function.identity()));
//            traverseMenuList(treeList,collect);
            List<PlatformMenu> platformMenus = listByIds(list.stream().map(PlatformRoleMenu::getMenuId).collect(Collectors.toSet()));
            resultList = BeanUtil.copyToList(platformMenus, PlatformMenuVO.class);
        }
        return resultList;
    }

    @Override
    public String getDcsUrl(Integer id) {
        DcsConstant.DCS1 byId = DcsConstant.DCS1.getById(id);
        if(Objects.isNull(byId)){
            throw new BusinessException("对应的画面不存在");
        }
        String signedUrl = getSignedUrl(byId.getPageId(), SecurityUtils.getUser().getUsername());
        return signedUrl;
    }

    public static PlatformMenuVO buildSubTree(List<PlatformMenuVO> allNodes, Integer parentId) {
        PlatformMenuVO root = allNodes.stream()
                .filter(node -> node.getId().equals(parentId))
                .findFirst()
                .orElse(null);

        if (root == null) return null;

        buildChildren(root, allNodes);
        return root;
    }

    private static void buildChildren(PlatformMenuVO parent, List<PlatformMenuVO> allNodes) {
        List<PlatformMenuVO> children = allNodes.stream()
                .filter(node -> Objects.equals(node.getParentId(), parent.getId()))
                .sorted(Comparator.comparing(PlatformMenuVO::getSort, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());

        for (PlatformMenuVO child : children) {
            buildChildren(child, allNodes); // 递归构建子节点
        }
        parent.setChildren(children);
    }

    public void traverseMenuList(List<PlatformMenuVO> menuList,Map<Integer, PlatformRoleMenu> collect) {
        for (PlatformMenuVO menu : menuList) {
            // 这里可以处理当前节点
            if(Objects.nonNull(collect.get(menu.getId()))){
                menu.setIsBind(1);
            }else {
                menu.setIsBind(0);
            }
            // 如果有子菜单则递归
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                traverseMenuList(menu.getChildren(),collect);
            }
        }
    }

    public static String getSignedUrl(String pageID,String username){
        Date date = new Date();
        Long time = date.getTime();
        String token = "ff39a4e926204771ae1e064f3229be6a";
//        String pageID = "efb7b37a2060402192f98c2a44f3****";
//        String username = "any";// 第三方平台的当前用户名
        String op = "readonly"; // 第三方平台自定义的权限信息
        String stringToSign = pageID + "|" + String.valueOf(time) + "|" + username + "|" + op;
        String signature = HMACSHA256(stringToSign.getBytes(), token.getBytes());
        String url = "http://60.171.128.8:82/static/topv/public/viewer/"+pageID+"?time="+time+"&username="+username+"&op="+op+"&signature="+ signature;
        return url;
    }

    /**
     * 利用Java原生的摘要实现SHA256加密
     * @param str 加密后的报文
     * @return
     */
    public static String HMACSHA256(byte[] data, byte[] key)
    {
        try  {
            SecretKeySpec signingKey = new SecretKeySpec(key, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            return URLEncoder.encode(byte2Base64(mac.doFinal(data)));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String byte2Base64(byte[] bytes){
        return Base64.encodeBase64String(bytes);
    }
}




