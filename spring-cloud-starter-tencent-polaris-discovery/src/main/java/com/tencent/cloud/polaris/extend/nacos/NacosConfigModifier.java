/*
 * Tencent is pleased to support the open source community by making Spring Cloud Tencent available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.cloud.polaris.extend.nacos;

import com.tencent.cloud.common.constant.ContextConstant;
import com.tencent.cloud.polaris.context.PolarisConfigModifier;
import com.tencent.polaris.api.config.plugin.DefaultPlugins;
import com.tencent.polaris.factory.config.ConfigurationImpl;
import com.tencent.polaris.factory.config.consumer.DiscoveryConfigImpl;
import com.tencent.polaris.factory.config.global.ServerConnectorConfigImpl;
import com.tencent.polaris.factory.config.provider.RegisterConfigImpl;
import org.apache.commons.lang.StringUtils;

import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * @author lingxiao.wlx
 */
public class NacosConfigModifier implements PolarisConfigModifier {
	private static final String ID = "nacos";
	private static final String ADDRESS_FORMAT = "%s:%s@%s";
	private static final String INTERNAL_NACOS_CLUSTER = "internal-nacos-cluster";

	private final NacosContextProperties nacosContextProperties;

	public NacosConfigModifier(NacosContextProperties nacosContextProperties) {
		this.nacosContextProperties = nacosContextProperties;
	}

	@Override
	public void modify(ConfigurationImpl configuration) {
		if (Objects.isNull(nacosContextProperties) || !nacosContextProperties.isEnabled()) {
			return;
		}

		if (CollectionUtils.isEmpty(configuration.getGlobal().getServerConnectors())) {
			configuration.getGlobal().setServerConnectors(new ArrayList<>());
		}
		if (CollectionUtils.isEmpty(configuration.getGlobal().getServerConnectors())
				&& null != configuration.getGlobal().getServerConnector()) {
			configuration.getGlobal().getServerConnectors().add(configuration.getGlobal().getServerConnector());
		}
		ServerConnectorConfigImpl serverConnectorConfig = new ServerConnectorConfigImpl();
		serverConnectorConfig.setId(ID);
		// Nacos Address URI: nacos:nacos@127.0.0.1:8848
		String address = String.format(ADDRESS_FORMAT, nacosContextProperties.getUsername(), nacosContextProperties.getPassword(), nacosContextProperties.getServerAddr());
		serverConnectorConfig.setAddresses(
				Collections.singletonList(address));
		serverConnectorConfig.setProtocol(DefaultPlugins.SERVER_CONNECTOR_NACOS);
		Map<String, String> metadata = serverConnectorConfig.getMetadata();
		// set internal-nacos-cluster if necessary
		String clusterName = nacosContextProperties.getClusterName();
		if (StringUtils.isNotBlank(clusterName)) {
			metadata.put(INTERNAL_NACOS_CLUSTER, clusterName);
		}
		configuration.getGlobal().getServerConnectors().add(serverConnectorConfig);
		DiscoveryConfigImpl discoveryConfig = new DiscoveryConfigImpl();
		discoveryConfig.setServerConnectorId(ID);
		discoveryConfig.setEnable(nacosContextProperties.isDiscoveryEnabled());
		configuration.getConsumer().getDiscoveries().add(discoveryConfig);

		RegisterConfigImpl registerConfig = new RegisterConfigImpl();
		registerConfig.setServerConnectorId(ID);
		registerConfig.setEnable(nacosContextProperties.isRegisterEnabled());
		configuration.getProvider().getRegisters().add(registerConfig);
	}

	@Override
	public int getOrder() {
		return ContextConstant.ModifierOrder.LAST;
	}
}
