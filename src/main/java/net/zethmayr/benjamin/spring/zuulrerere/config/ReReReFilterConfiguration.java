package net.zethmayr.benjamin.spring.zuulrerere.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.List;

@ConfigurationProperties("rerere")
@Service
@Getter
@Setter
public class ReReReFilterConfiguration {
    private boolean enabled;
    private String webShibboleth;
    private List<String> prefixes;
    private List<String> extensions;
}
