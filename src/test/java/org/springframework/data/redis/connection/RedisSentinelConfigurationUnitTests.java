/*
 * Copyright 2015-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.redis.connection;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;
import org.springframework.core.env.PropertySource;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Strobl
 */
public class RedisSentinelConfigurationUnitTests {

	static final String HOST_AND_PORT_1 = "127.0.0.1:123";
	static final String HOST_AND_PORT_2 = "localhost:456";
	static final String HOST_AND_PORT_3 = "localhost:789";
	static final String HOST_AND_NO_PORT = "localhost";

	@Test // DATAREDIS-372
	public void shouldCreateRedisSentinelConfigurationCorrectlyGivenMasterAndSingleHostAndPortString() {

		RedisSentinelConfiguration config = new RedisSentinelConfiguration("mymaster",
				Collections.singleton(HOST_AND_PORT_1));

		assertThat(config.getSentinels().size()).isEqualTo(1);
		assertThat(config.getSentinels()).contains(new RedisNode("127.0.0.1", 123));
	}

	@Test // DATAREDIS-372
	public void shouldCreateRedisSentinelConfigurationCorrectlyGivenMasterAndMultipleHostAndPortStrings() {

		RedisSentinelConfiguration config = new RedisSentinelConfiguration("mymaster",
				new HashSet<>(Arrays.asList(HOST_AND_PORT_1, HOST_AND_PORT_2, HOST_AND_PORT_3)));

		assertThat(config.getSentinels().size()).isEqualTo(3);
		assertThat(config.getSentinels()).contains(new RedisNode("127.0.0.1", 123), new RedisNode("localhost", 456),
				new RedisNode("localhost", 789));
	}

	@Test // DATAREDIS-372
	public void shouldThrowExecptionOnInvalidHostAndPortString() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new RedisSentinelConfiguration("mymaster", Collections.singleton(HOST_AND_NO_PORT)));
	}

	@Test // DATAREDIS-372
	public void shouldThrowExceptionWhenListOfHostAndPortIsNull() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new RedisSentinelConfiguration("mymaster", Collections.<String> singleton(null)));
	}

	@Test // DATAREDIS-372
	public void shouldNotFailWhenListOfHostAndPortIsEmpty() {

		RedisSentinelConfiguration config = new RedisSentinelConfiguration("mymaster", Collections.<String> emptySet());

		assertThat(config.getSentinels().size()).isEqualTo(0);
	}

	@Test // DATAREDIS-372
	public void shouldThrowExceptionGivenNullPropertySource() {
		assertThatIllegalArgumentException().isThrownBy(() -> new RedisSentinelConfiguration((PropertySource<?>) null));
	}

	@Test // DATAREDIS-372
	public void shouldNotFailWhenGivenPropertySourceNotContainingRelevantProperties() {

		RedisSentinelConfiguration config = new RedisSentinelConfiguration(new MockPropertySource());

		assertThat(config.getMaster()).isNull();
		assertThat(config.getSentinels().size()).isEqualTo(0);
	}

	@Test // DATAREDIS-372
	public void shouldBeCreatedCorrecltyGivenValidPropertySourceWithMasterAndSingleHostPort() {

		MockPropertySource propertySource = new MockPropertySource();
		propertySource.setProperty("spring.redis.sentinel.master", "myMaster");
		propertySource.setProperty("spring.redis.sentinel.nodes", HOST_AND_PORT_1);

		RedisSentinelConfiguration config = new RedisSentinelConfiguration(propertySource);

		assertThat(config.getMaster().getName()).isEqualTo("myMaster");
		assertThat(config.getSentinels().size()).isEqualTo(1);
		assertThat(config.getSentinels()).contains(new RedisNode("127.0.0.1", 123));
	}

	@Test // DATAREDIS-372
	public void shouldBeCreatedCorrecltyGivenValidPropertySourceWithMasterAndMultipleHostPort() {

		MockPropertySource propertySource = new MockPropertySource();
		propertySource.setProperty("spring.redis.sentinel.master", "myMaster");
		propertySource.setProperty("spring.redis.sentinel.nodes",
				StringUtils.collectionToCommaDelimitedString(Arrays.asList(HOST_AND_PORT_1, HOST_AND_PORT_2, HOST_AND_PORT_3)));

		RedisSentinelConfiguration config = new RedisSentinelConfiguration(propertySource);

		assertThat(config.getSentinels().size()).isEqualTo(3);
		assertThat(config.getSentinels()).contains(new RedisNode("127.0.0.1", 123), new RedisNode("localhost", 456),
				new RedisNode("localhost", 789));
	}

	@Test // DATAREDIS-1060
	public void dataNodePasswordDoesNotAffectSentinelPassword() {

		RedisPassword password = RedisPassword.of("88888888-8x8-getting-creative-now");
		RedisSentinelConfiguration configuration = new RedisSentinelConfiguration("myMaster",
				Collections.singleton(HOST_AND_PORT_1));
		configuration.setPassword(password);

		assertThat(configuration.getSentinelPassword()).isEqualTo(RedisPassword.none());
	}

	@Test // DATAREDIS-1060
	public void readSentinelPasswordFromConfigProperty() {

		MockPropertySource propertySource = new MockPropertySource();
		propertySource.setProperty("spring.redis.sentinel.master", "myMaster");
		propertySource.setProperty("spring.redis.sentinel.nodes", HOST_AND_PORT_1);
		propertySource.setProperty("spring.redis.sentinel.password", "computer-says-no");

		RedisSentinelConfiguration config = new RedisSentinelConfiguration(propertySource);

		assertThat(config.getSentinelPassword()).isEqualTo(RedisPassword.of("computer-says-no"));
		assertThat(config.getSentinels()).hasSize(1).contains(new RedisNode("127.0.0.1", 123));
	}
}
