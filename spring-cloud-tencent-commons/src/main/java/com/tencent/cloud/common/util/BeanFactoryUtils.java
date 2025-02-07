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

package com.tencent.cloud.common.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * the utils for bean factory.
 * @author lepdou 2022-05-23
 */
public final class BeanFactoryUtils {

	private BeanFactoryUtils() {
	}

	public static <T> List<T> getBeans(BeanFactory beanFactory, Class<T> requiredType) {
		if (!(beanFactory instanceof DefaultListableBeanFactory)) {
			throw new RuntimeException("bean factory not support get list bean. factory type = " + beanFactory.getClass()
					.getName());
		}

		String[] beanNames = ((DefaultListableBeanFactory) beanFactory).getBeanNamesForType(requiredType);

		if (beanNames.length == 0) {
			return Collections.emptyList();
		}

		return Arrays.stream(beanNames).map(
				beanName -> beanFactory.getBean(beanName, requiredType)
		).collect(Collectors.toList());
	}
}
