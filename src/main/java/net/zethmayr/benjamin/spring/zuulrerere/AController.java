package net.zethmayr.benjamin.spring.zuulrerere;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.REFERER;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@Controller("/**")
@Slf4j
public class AController {

    @Autowired
    private RouteLocator routeLocator;


    @ResponseStatus(NOT_FOUND)
    class FallbackFailedException extends RuntimeException {
    }

    @ResponseStatus(OK)
    class EverythingIsFineException extends RuntimeException {
    }

    @RequestMapping
    public String root(final @Autowired HttpServletRequest request) {
        val requestUri = request.getRequestURI();
        if (requestUri.equals("/")) {
            throw new EverythingIsFineException();
        }
        val referrerString = request.getHeader(REFERER);
        if (referrerString == null || referrerString.trim().isEmpty()) {
            throw new FallbackFailedException();
        }
        LOG.info("referrerString is {}", referrerString);
        val route = routeLocator.getMatchingRoute(referrerString);
        if (route == null) {
            throw new FallbackFailedException();
        }
        LOG.info("referrer route is {}", route);
        val id = route.getId();
        //val location = route.getLocation();
        String location = route.getLocation();
        LOG.info("id is {}, requestUri is {}, location is {}", id, requestUri, location);
        return "forward:" + route.getFullPath().replace(route.getPath(),requestUri);
    }


}
