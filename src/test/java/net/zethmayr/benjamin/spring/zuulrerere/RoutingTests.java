package net.zethmayr.benjamin.spring.zuulrerere;

import lombok.val;
import net.zethmayr.benjamin.spring.zuulrerere.base.MockServiceEnvironment;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class RoutingTests extends MockServiceEnvironment {

    private RestTemplate http;
    private String zuul;

    @Autowired
    ZuulRerereApplication application;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void httpSetup() {
        super.setUp();
        http = new RestTemplate();
        zuul = "http://localhost:9000";
    }

    @Test
    public void canGetFooIndex() {
        val response = http.getForEntity(zuul+ "/foo/index.html", String.class);
        assertThat(response.getStatusCodeValue(), is(OK.value()));
    }

    @Test
    public void canGetFooStatic() {
        val response = http.getForEntity(zuul + "/foo/web/static/js/js.js", String.class);
        assertThat(response.getStatusCodeValue(), is(OK.value()));
    }

    @Test
    public void canGetBarIndex() {
        val response = http.getForEntity(zuul+ "/bar/index.html", String.class);
        assertThat(response.getStatusCodeValue(), is(OK.value()));
    }

    @Test
    public void canGetBarStatic() {
        val response = http.getForEntity(zuul + "/bar/static/js/js.js", String.class);
        assertThat(response.getStatusCodeValue(), is(OK.value()));
    }

    @Test
    public void cannotGetRootStatic() {
        thrown.expect(HttpClientErrorException.class);
        thrown.expectMessage("404 null");
        http.getForEntity(zuul + "/static/js/js.js", String.class);
    }

    @Test
    public void canGetRootStaticFromFooIndexRelative() {
        val headers = new HttpHeaders();
        headers.put("Referer",Collections.singletonList("/foo/index.html"));
        val request = new HttpEntity(headers);
        val response = http.exchange(zuul + "/static/js/js.js", GET, request, String.class);
    }

    @Test
    public void canGetBarStaticFromBarIndexRelative() {
        val headers = new HttpHeaders();
        headers.put("Referer",Collections.singletonList("/bar/index.html"));
        val request = new HttpEntity(headers);
        val response = http.exchange(zuul + "/static/js/js.js", GET, request, String.class);
    }
}
