package net.zethmayr.benjamin.spring.zuulrerere;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;
import net.zethmayr.benjamin.spring.zuulrerere.config.ReReReFilterConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.FORWARD_LOCATION_PREFIX;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.FORWARD_TO_KEY;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.HTTPS_SCHEME;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.HTTP_SCHEME;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PROXY_KEY;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.REQUEST_URI_KEY;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.RETRYABLE_KEY;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SERVICE_HEADER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SERVICE_ID_HEADER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SERVICE_ID_KEY;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.X_FORWARDED_FOR_HEADER;
import static org.springframework.http.HttpHeaders.REFERER;

@Service
@Slf4j
public class ReReReFilter extends ZuulFilter {

    private final ZuulProperties zuulConfig;
    private final ReReReFilterConfiguration config;
    private final NavigableMap<String, ZuulProperties.ZuulRoute> webRoutes;
    private final NavigableSet<String> prefixes;
    private final RouteLocator routeLocator;

    public ReReReFilter(
            final @Autowired ZuulProperties zuulConfig,
            final @Autowired ReReReFilterConfiguration config,
            final @Autowired RouteLocator routeLocator
    ) {
        LOG.info("Creating ReReReFilter");
        this.zuulConfig = zuulConfig;
        this.config = config;
        this.routeLocator = routeLocator;
        prefixes = new TreeSet<>();
        prefixes.addAll(config.getPrefixes());
        LOG.info("Prefixes are {}", prefixes);
        final String shibboleth = config.getWebShibboleth();
        webRoutes = new TreeMap<>();
        for (val routeEntry : zuulConfig.getRoutes().entrySet()) {
            val name = routeEntry.getKey();
            val route = routeEntry.getValue();
            if (name.contains(shibboleth) || route.getPath().contains(shibboleth)
                    || (route.getUrl() != null && route.getUrl().contains(shibboleth))
                    || (route.getServiceId() != null && route.getServiceId().contains(shibboleth))) {
                webRoutes.put(route.getPath(), route);
            }
        }
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;// PRE_DECORATION_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        if (!config.isEnabled()) {
            LOG.debug("bailed out, disabled");
            return false;
        }
        val context = RequestContext.getCurrentContext();
        val request = context.getRequest();
        val referrerString = request.getHeader(REFERER);
        if (referrerString == null || referrerString.trim().isEmpty()) {
            LOG.debug("bailed out, no referrer");
            return false;
        }
        try {
            val requestUri = request.getRequestURI();
            val path = Paths.get(requestUri);
            final String first = path.getName(0).toString();
            LOG.debug("first is {}", first);
            if (!prefixes.contains(first)) {
                LOG.debug("returning false, no prefix");
                return false;
            }
            val referrerUri = new URI(referrerString);
        } catch (URISyntaxException ise) {
            LOG.warn("referrer is not a URI", ise);
        }
        LOG.debug("Made it! returning true.");
        return true;
    }

    private URL getUrl(final String url) throws ZuulException {
        try {
            return new URL(url);
        } catch (MalformedURLException mue) {
            throw new ZuulException(mue, 500, "gremlins");
        }
    }

    @Override
    public Object run() throws ZuulException {
        LOG.debug("Running..");
        val context = RequestContext.getCurrentContext();
        val request = context.getRequest();
        val referrerString = request.getHeader(REFERER);
        val requestUri = request.getRequestURI();
        LOG.info("referrerString is {}", referrerString);
        LOG.info("requestUri is {}", requestUri);
        val route = routeLocator.getMatchingRoute(referrerString);
        LOG.info("referrer route is {}", route);
        val id = route.getId();
        //val location = route.getLocation();
        String location = route.getLocation();
        LOG.info("id is {}, requestUri is {}, location is {}", id, requestUri, location);
        if (location != null) {
            context.put(REQUEST_URI_KEY, requestUri);
            context.put(PROXY_KEY, route.getId());
//            if (!route.isCustomSensitiveHeaders()) {
//                this.proxyRequestHelper
//                        .addIgnoredHeaders(this.properties.getSensitiveHeaders().toArray(new String[0]));
//            }
//            else {
//                this.proxyRequestHelper.addIgnoredHeaders(route.getSensitiveHeaders().toArray(new String[0]));
//            }

            if (route.getRetryable() != null) {
                context.put(RETRYABLE_KEY, route.getRetryable());
            }

            if (location.startsWith(HTTP_SCHEME + ":") || location.startsWith(HTTPS_SCHEME + ":")) {
                context.setRouteHost(getUrl(location));
                context.addOriginResponseHeader(SERVICE_HEADER, location);
            } else if (location.startsWith(FORWARD_LOCATION_PREFIX)) {
                context.set(FORWARD_TO_KEY,
                        StringUtils.cleanPath(location.substring(FORWARD_LOCATION_PREFIX.length()) + route.getPath()));
                context.setRouteHost(null);
                return null;
            } else {
                // set serviceId for use in filters.route.RibbonRequest
                context.set(SERVICE_ID_KEY, location);
                context.setRouteHost(null);
                context.addOriginResponseHeader(SERVICE_ID_HEADER, location);
            }
//            if (this.properties.isAddProxyHeaders()) {
//                addProxyHeaders(context, route);
//                String xforwardedfor = context.getRequest().getHeader(X_FORWARDED_FOR_HEADER);
//                String remoteAddr = context.getRequest().getRemoteAddr();
//                if (xforwardedfor == null) {
//                    xforwardedfor = remoteAddr;
//                }
//                else if (!xforwardedfor.contains(remoteAddr)) { // Prevent duplicates
//                    xforwardedfor += ", " + remoteAddr;
//                }
//                context.addZuulRequestHeader(X_FORWARDED_FOR_HEADER, xforwardedfor);
//            }
//            if (this.properties.isAddHostHeader()) {
//                context.addZuulRequestHeader(HttpHeaders.HOST, toHostHeader(ctx.getRequest()));
//            }
            context.put(FORWARD_TO_KEY, true);
            context.put("sendForwardFilter.ran", true);
            return true;
        }
        return null;
    }
}
