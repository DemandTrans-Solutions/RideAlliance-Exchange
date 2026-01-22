package com.clearinghouse.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


@Slf4j
public class OldCorsFilterDeprecated implements Filter {

    private static final String AUTH_HEADER_NAME = "X-AUTH-TOKEN";
    private static final String XSRF_TOKEN_HEADER_NAME = "X-XSRF-TOKEN";


    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        /*devolopment*/
//        String[] allowDomain = {"http://10.235.17.104", "http://10.235.4.9", "http://10.235.4.37","http://10.235.4.37:8080", "10.235.4.37:8080","http://52.45.196.193", "52.45.196.193", "http://52.45.196.193:8082", "http://tripexchange.com", "tripexchange.com", "52.45.196.193:8080", "125.99.44.122:9079", "http://125.99.44.122:9079", "http://10.235.11.75:9079", "10.235.11.75:9079", "localhost:8081", "10.235.4.9:8081", "http://localhost:8081", "10.235.4.9:8082", "localhost:8082", "http://localhost:4200", "http://10.235.4.37:3000", "10.235.4.37:3000", "http://10.235.4.9:4200", "10.235.4.9:4200", "localhost:4200", "http://10.235.4.37:4200"};

        /*separate out the things particular to specific link*/
        /*production*/
        String[] allowDomain = {"http://52.45.196.193", "52.45.196.193", "http://52.45.196.193:8082", "http://tripexchange.com", "tripexchange.com", "52.45.196.193:8082", "125.99.44.122:9079", "http://125.99.44.122:9079"};

        /*zcon server testing*/
//   String[] allowDomain = {"125.99.44.122:9079", "http://125.99.44.122:9079","http://10.235.11.75:9079"};
//"http://tripexchange.com","tripexchange.com",
        Set<String> allowedOrigins = new HashSet<>(Arrays.asList(allowDomain));
        HttpServletRequest request = (HttpServletRequest) req;
//      uncomment below to see all header - this is sometimes required to check CORS filter issue
//        Map<String, String> map = new HashMap<String, String>();
//        Enumeration headerNames = request.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//            String key = (String) headerNames.nextElement();
//            String value = request.getHeader(key);
//            map.put(key, value);
//        }

        String origin = getClientIpAddress(request);
        if (allowedOrigins.contains(origin)) {
            log.info("Requested origin is allowed [" + origin + "]");
            HttpServletResponse response = (HttpServletResponse) res;
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS, GET, PUT,  DELETE");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setHeader("Access-Control-Allow-Headers", "x-requested-with, Content-Type ");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization ," + AUTH_HEADER_NAME + "," + XSRF_TOKEN_HEADER_NAME);
            chain.doFilter(req, res);
        } else {
            log.error("Requested origin is NOT allowed [" + origin + "]");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
        log.info("Inside filter Init");
    }

    @Override
    public void destroy() {
        log.info("Inside filter Init");
    }

    private static final String[] HEADERS_TO_TRY = {
            "origin",
            "host",
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"};

    public static String getClientIpAddress(HttpServletRequest request) {
        for (String header : HEADERS_TO_TRY) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip) && !ip.contains("chrome-extension")) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

}
