package net.zethmayr.benjamin.spring.zuulrerere.base;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import lombok.val;
import org.junit.Before;
import org.junit.ClassRule;
//import org.springframework.cloud.contract.wiremock.WireMockSpring;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public abstract class MockServiceEnvironment {
    @ClassRule
    public static WireMockClassRule fooServiceA = new WireMockClassRule(localhost(8801));
    @ClassRule
    public static WireMockClassRule fooServiceB = new WireMockClassRule(localhost(8802));
    @ClassRule
    public static WireMockClassRule barService = new WireMockClassRule(localhost(8810));
    @ClassRule
    public static WireMockClassRule fooWeb1 = new WireMockClassRule(localhost(8901));
    @ClassRule
    public static WireMockClassRule fooHeavy1 = new WireMockClassRule(localhost(8911));
    @ClassRule
    public static WireMockClassRule fooHeavy2 = new WireMockClassRule(localhost(8912));
    @ClassRule
    public static WireMockClassRule barWeb1 = new WireMockClassRule(localhost(8921));
    @ClassRule
    public static WireMockClassRule barWeb2 = new WireMockClassRule(localhost(8922));

    private static WireMockConfiguration localhost(final int port) {
        return WireMockConfiguration.options()
                .port(port)
                .bindAddress("localhost");
    }

    private void rootOk(final WireMockClassRule... rules) {
        for (val rule : rules) {
            rule.stubFor(get(urlEqualTo("/")).willReturn(
                    aResponse()
                            .withStatus(200)
                            .withBody(rule.toString())
            ));
        }
    }

    private void hasIndex(final WireMockClassRule... rules) {
        for (val rule : rules) {
            rule.stubFor(get(urlEqualTo("/index.html")).willReturn(
                    aResponse()
                            .withStatus(200)
                            .withBody(rule.toString())
            ));
        }
    }

    private void hasJs(final WireMockClassRule... rules) {
        for (val rule : rules) {
            rule.stubFor(get(urlEqualTo("/static/js/js.js")).willReturn(
                    aResponse()
                            .withStatus(200)
                            .withBody(rule.toString())
            ));
        }
    }

    @Before
    public void setUp() {
        rootOk(fooServiceA, fooServiceB, barService, fooWeb1, fooHeavy1, fooHeavy2, barWeb1, barWeb2);
        hasIndex(fooWeb1, barWeb1, barWeb2);
        hasJs(fooHeavy1, fooHeavy2, barWeb1, barWeb2);
    }
}
