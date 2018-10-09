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
    public static WireMockClassRule fooWeb = new WireMockClassRule(localhost(8901));
    @ClassRule
    public static WireMockClassRule barWeb1 = new WireMockClassRule(localhost(8921));
    @ClassRule
    public static WireMockClassRule barWeb2 = new WireMockClassRule(localhost(8922));
    @ClassRule
    public static WireMockClassRule nothing = new WireMockClassRule(localhost(6666));

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
        rootOk(fooServiceA, fooServiceB, barService, fooWeb, barWeb1, barWeb2);
        hasIndex(fooWeb, barWeb1, barWeb2);
        hasJs(fooWeb, barWeb1, barWeb2);
    }
}
