package com.ecommerce.apigateway.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import reactor.core.publisher.Mono;

@Controller
public class IndexController {

    private final DiscoveryClient discoveryClient;
    
    @Value("${spring.application.name}")
    private String applicationName;

    public IndexController(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Mono<String> index() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>")
            .append("<html lang=\"en\">")
            .append("<head>")
            .append("<meta charset=\"UTF-8\">")
            .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
            .append("<meta http-equiv=\"refresh\" content=\"0; url=/dashboard\">")
            .append("<title>API Gateway</title>")
            .append("<style>")
            .append("body { font-family: Arial, sans-serif; display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0; }")
            .append(".container { text-align: center; }")
            .append("</style>")
            .append("</head>")
            .append("<body>")
            .append("<div class=\"container\">")
            .append("<h1>Redirecting to Dashboard...</h1>")
            .append("<p>If you are not redirected automatically, <a href=\"/dashboard\">click here</a>.</p>")
            .append("</div>")
            .append("</body>")
            .append("</html>");
        
        return Mono.just(html.toString());
    }
    
    @GetMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Mono<Map<String, Object>> apiInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", applicationName);
        response.put("status", "running");
        response.put("timestamp", System.currentTimeMillis());
        
        List<Map<String, Object>> services = discoveryClient.getServices().stream()
                .map(serviceName -> {
                    Map<String, Object> serviceDetails = new HashMap<>();
                    serviceDetails.put("name", serviceName);
                    serviceDetails.put("instances", discoveryClient.getInstances(serviceName).size());
                    return serviceDetails;
                })
                .collect(Collectors.toList());
        
        response.put("registeredServices", services);
        
        return Mono.just(response);
    }
    
    @GetMapping(value = "/info", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Mono<String> info() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>")
            .append("<html lang=\"en\">")
            .append("<head>")
            .append("<meta charset=\"UTF-8\">")
            .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
            .append("<title>API Gateway Information</title>")
            .append("<style>")
            .append("body { font-family: Arial, sans-serif; margin: 0; padding: 20px; color: #333; }")
            .append("h1 { color: #2c3e50; }")
            .append("h2 { color: #3498db; margin-top: 20px; }")
            .append(".container { max-width: 1000px; margin: 0 auto; }")
            .append(".card { background: #f9f9f9; border-radius: 5px; padding: 15px; margin-bottom: 15px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }")
            .append("table { width: 100%; border-collapse: collapse; }")
            .append("th, td { text-align: left; padding: 8px; border-bottom: 1px solid #ddd; }")
            .append("th { background-color: #f2f2f2; }")
            .append("tr:hover { background-color: #f5f5f5; }")
            .append(".dashboard-link { display: block; margin: 20px 0; padding: 10px 15px; background-color: #3498db; color: white; text-decoration: none; border-radius: 5px; text-align: center; }")
            .append(".dashboard-link:hover { background-color: #2980b9; }")
            .append("</style>")
            .append("</head>")
            .append("<body>")
            .append("<div class=\"container\">")
            .append("<h1>API Gateway Information</h1>")
            .append("<a href=\"/dashboard\" class=\"dashboard-link\">Go to Dashboard</a>")
            .append("<div class=\"card\">")
            .append("<p><strong>Service:</strong> ").append(applicationName).append("</p>")
            .append("<p><strong>Status:</strong> Running</p>")
            .append("<p><strong>Timestamp:</strong> ").append(new java.util.Date()).append("</p>")
            .append("</div>")
            .append("<h2>Registered Services</h2>");
        
        List<String> services = discoveryClient.getServices();
        
        if (services.isEmpty()) {
            html.append("<p>No services registered</p>");
        } else {
            html.append("<table>")
                .append("<tr><th>Service Name</th><th>Instances</th><th>Status</th></tr>");
            
            for (String serviceName : services) {
                int instances = discoveryClient.getInstances(serviceName).size();
                html.append("<tr>")
                    .append("<td>").append(serviceName).append("</td>")
                    .append("<td>").append(instances).append("</td>")
                    .append("<td>").append(instances > 0 ? "UP" : "DOWN").append("</td>")
                    .append("</tr>");
            }
            
            html.append("</table>");
        }
        
        html.append("</div></body></html>");
        
        return Mono.just(html.toString());
    }
} 