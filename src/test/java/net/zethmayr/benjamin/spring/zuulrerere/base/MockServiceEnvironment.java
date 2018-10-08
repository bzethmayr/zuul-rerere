package net.zethmayr.benjamin.spring.zuulrerere.base;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.ClassRule;

public abstract class MockServiceEnvironment {
    @ClassRule
    public static WireMockClassRule fooServiceA = new WireMockClassRule(8801);
    public static WireMockClassRule fooServiceB = new WireMockClassRule(8802);
    public static WireMockClassRule barService = new WireMockClassRule(8810);
    public static WireMockClassRule fooWeb1 = new WireMockClassRule(8901);
    public static WireMockClassRule fooHeavy1 = new WireMockClassRule(8911);
    public static WireMockClassRule fooHeavy2 = new WireMockClassRule(8912);
    public static WireMockClassRule barWeb1 = new WireMockClassRule(8921);
    public static WireMockClassRule barWeb2 = new WireMockClassRule(8922);
}
