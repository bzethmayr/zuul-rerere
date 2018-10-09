package net.zethmayr.benjamin.spring.zuulrerere.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("rerere")
public class ReReReFilterConfiguration {
    private boolean enabled;
    private String webShibboleth;
    private List<String> prefixes;
    private List<String> extensions;
}
