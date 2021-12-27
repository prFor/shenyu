/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shenyu.client.dubbo.common;

import org.apache.commons.lang3.StringUtils;
import org.apache.shenyu.client.core.annotaion.ShenyuClient;
import org.apache.shenyu.client.core.exception.ShenyuClientIllegalArgumentException;
import org.apache.shenyu.client.core.register.RefreshedShenyuClientRegister;
import org.apache.shenyu.client.dubbo.common.dto.DubboRpcExt;
import org.apache.shenyu.common.enums.RpcTypeEnum;
import org.apache.shenyu.common.utils.GsonUtils;
import org.apache.shenyu.register.client.api.ShenyuClientRegisterRepository;
import org.apache.shenyu.register.common.config.PropertiesConfig;
import org.apache.shenyu.register.common.dto.MetaDataRegisterDTO;
import org.apache.shenyu.register.common.dto.URIRegisterDTO;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * DubboServiceBeanListener .
 */
public abstract class DubboServiceBeanListener extends RefreshedShenyuClientRegister {
    
    /**
     * Instantiates a new Abstract shenyu client register.
     *
     * @param clientConfig                   the client config
     * @param shenyuClientRegisterRepository the shenyu client register repository
     */
    public DubboServiceBeanListener(final PropertiesConfig clientConfig, final ShenyuClientRegisterRepository shenyuClientRegisterRepository) {
        super(clientConfig, shenyuClientRegisterRepository);
    }
    
    /**
     * Build meta data dto meta data register dto.
     *
     * @param serviceBean  the service bean
     * @param shenyuClient the shenyu client
     * @param method       the method
     * @return the meta data register dto
     */
    protected MetaDataRegisterDTO buildMetaDataDTO(final ServiceData serviceBean, final ShenyuClient shenyuClient, final Method method) {
        String appName = getAppName(serviceBean.getAppName());
        String newPath = StringUtils.isBlank(shenyuClient.path()) ? method.getName() : shenyuClient.path();
        String path = superContext(newPath);
        String desc = shenyuClient.desc();
        String serviceName = serviceBean.getInterface();
        String configRuleName = shenyuClient.ruleName();
        String ruleName = ("".equals(configRuleName)) ? path : configRuleName;
        String methodName = method.getName();
        Class<?>[] parameterTypesClazz = method.getParameterTypes();
        String parameterTypes = Arrays.stream(parameterTypesClazz).map(Class::getName).collect(Collectors.joining(","));
        return MetaDataRegisterDTO.builder()
                .appName(appName)
                .serviceName(serviceName)
                .methodName(methodName)
                .contextPath(this.getContextPath())
                .host(getHost())
                .port(getPort(serviceBean.getPort()))
                .path(path)
                .ruleName(ruleName)
                .pathDesc(desc)
                .parameterTypes(parameterTypes)
                .rpcExt(buildRpcExt(serviceBean))
                .rpcType(RpcTypeEnum.DUBBO.getName())
                .enabled(shenyuClient.enabled())
                .build();
    }
    
    private String buildRpcExt(final ServiceData serviceBean) {
        DubboRpcExt build = DubboRpcExt.builder()
                .group(serviceBean.getGroup())
                .version(serviceBean.getVersion())
                .loadbalance(serviceBean.getLoadbalance())
                .retries(serviceBean.getRetries())
                .timeout(serviceBean.getTimeout())
                .sent(serviceBean.getSent())
                .cluster(serviceBean.getCluster())
                .url("")
                .build();
        return GsonUtils.getInstance().toJson(build);
    }
    
    private String superContext(String path) {
        return getContextPath() + path;
    }
    
    /**
     * Build uri register dto uri register dto.
     *
     * @param serviceBean the service bean
     * @return the uri register dto
     */
    protected URIRegisterDTO buildURIRegisterDTO(final ServiceData serviceBean) {
        return URIRegisterDTO.builder()
                .contextPath(this.getContextPath())
                .appName(this.getAppName(serviceBean.getAppName()))
                .rpcType(RpcTypeEnum.DUBBO.getName())
                .host(this.getHost())
                .port(this.getPort(serviceBean.getPort()))
                .build();
    }
    
    /**
     * Check param.
     */
    @Override
    public void checkParam() {
        if (StringUtils.isAnyBlank(this.getContextPath(), this.getAppName())) {
            throw new ShenyuClientIllegalArgumentException("apache dubbo client must config the contextPath or appName");
        }
    }
}
