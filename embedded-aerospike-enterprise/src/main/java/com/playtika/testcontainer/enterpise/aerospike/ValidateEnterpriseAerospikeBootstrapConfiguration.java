package com.playtika.testcontainer.enterpise.aerospike;

import com.playtika.testcontainer.aerospike.EmbeddedAerospikeBootstrapConfiguration;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

@Slf4j
@AutoConfiguration(before = EmbeddedAerospikeBootstrapConfiguration.class)
@ConditionalOnExpression("${embedded.containers.enabled:true}")
@ConditionalOnProperty(value = "embedded.aerospike.enabled", matchIfMissing = true)
@PropertySource("classpath:/embedded-enterprise-aerospike.properties")
public class ValidateEnterpriseAerospikeBootstrapConfiguration {

    private static final String DOCKER_IMAGE = "aerospike/aerospike-server-enterprise:6.3.0.16_1";
    private static final String AEROSPIKE_DOCKER_IMAGE_PROPERTY = "embedded.aerospike.dockerImage";
    private static final ImageVersion SUITABLE_IMAGE_VERSION = new ImageVersion(6, 3);

    private ConfigurableEnvironment environment;

    @Autowired
    public void setEnvironment(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void verifyAerospikeImage() {
        log.info("Verify Aerospike Enterprise Image");

        String dockerImage = environment.getProperty(AEROSPIKE_DOCKER_IMAGE_PROPERTY);
        if (!isEnterpriseImage(dockerImage)) {
            throw new IllegalStateException("You should use enterprise image for the Aerospike container with equal or higher version: " + DOCKER_IMAGE);
        }
    }

    private boolean isEnterpriseImage(String dockerImage) {
        return dockerImage != null
                && dockerImage.contains("enterprise")
                && isSuitableVersion(dockerImage);
    }

    private boolean isSuitableVersion(String dockerImage) {
        int index = dockerImage.indexOf(":");
        if (index == -1) {
            return false;
        }
        String version = dockerImage.substring(index + 1);
        ImageVersion imageVersion = ImageVersion.parse(version);
        return imageVersion.compareTo(SUITABLE_IMAGE_VERSION) >= 0;
    }

}
