package com.ctrip.apollo.adminservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ctrip.apollo.biz.entity.AppNamespace;
import com.ctrip.apollo.biz.service.AppNamespaceService;
import com.ctrip.apollo.common.auth.ActiveUser;
import com.ctrip.apollo.common.utils.BeanUtils;
import com.ctrip.apollo.core.dto.AppNamespaceDTO;

import java.util.List;

@RestController
public class AppNamespaceController {

  @Autowired
  private AppNamespaceService appNamespaceService;

  @RequestMapping("/apps/{appId}/appnamespace/{appnamespace}/unique")
  public boolean isAppNamespaceUnique(@PathVariable("appId") String appId,
      @PathVariable("appnamespace") String appnamespace) {
    return appNamespaceService.isAppNamespaceNameUnique(appId, appnamespace);
  }

  @RequestMapping("/appnamespaces/public")
  public List<AppNamespaceDTO> findPublicAppNamespaces(){
    List<AppNamespace> appNamespaces = appNamespaceService.findPublicAppNamespaces();
    return BeanUtils.batchTransform(AppNamespaceDTO.class, appNamespaces);
  }

  @RequestMapping(value = "/apps/{appId}/appnamespaces", method = RequestMethod.POST)
  public AppNamespaceDTO createOrUpdate( @RequestBody AppNamespaceDTO appNamespace, @ActiveUser UserDetails user){

    AppNamespace entity = BeanUtils.transfrom(AppNamespace.class, appNamespace);
    AppNamespace managedEntity = appNamespaceService.findOne(entity.getAppId(), entity.getName());

    String userName = user.getUsername();
    if (managedEntity != null){
      managedEntity.setDataChangeLastModifiedBy(userName);
      BeanUtils.copyEntityProperties(entity, managedEntity);
      entity = appNamespaceService.update(managedEntity);
    }else {
      entity.setDataChangeLastModifiedBy(userName);
      entity.setDataChangeCreatedBy(userName);
      entity = appNamespaceService.createAppNamespace(entity, userName);
    }

    return BeanUtils.transfrom(AppNamespaceDTO.class, entity);

  }

}