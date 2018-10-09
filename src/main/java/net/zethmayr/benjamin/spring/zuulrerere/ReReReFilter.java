package net.zethmayr.benjamin.spring.zuulrerere;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.val;
import net.zethmayr.benjamin.spring.zuulrerere.config.ReReReFilterConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.stereotype.Service;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;
import static org.springframework.http.HttpHeaders.REFERER;

@Service
public class ReReReFilter extends ZuulFilter {

    private final ZuulProperties zuulConfig;
    private final ReReReFilterConfiguration config;

    public ReReReFilter(
            final @Autowired ZuulProperties zuulConfig,
            final @Autowired ReReReFilterConfiguration config
    ) {
        this.zuulConfig = zuulConfig;
        this.config = config;

    }

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return PRE_DECORATION_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        val context = RequestContext.getCurrentContext();
        val request = context.getRequest();
        val referrerString = request.getHeader(REFERER);
        if (referrerString == null || referrerString.trim().isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        return null;
    }
}
