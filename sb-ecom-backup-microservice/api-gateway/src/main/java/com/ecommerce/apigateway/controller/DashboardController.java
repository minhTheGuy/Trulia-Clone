package com.ecommerce.apigateway.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import reactor.core.publisher.Mono;

@Controller
public class DashboardController {

    private final DiscoveryClient discoveryClient;
    
    @Value("${spring.application.name}")
    private String applicationName;
    
    @Value("${server.port}")
    private String serverPort;

    public DashboardController(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @GetMapping(value = "/dashboard", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Mono<String> dashboard() {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>")
            .append("<html lang=\"en\">")
            .append("<head>")
            .append("<meta charset=\"UTF-8\">")
            .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
            .append("<title>E-Commerce Microservices Dashboard</title>")
            .append("<link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css\" rel=\"stylesheet\">")
            .append("<style>")
            .append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8f9fa; }")
            .append(".header { background-color: #343a40; color: white; padding: 2rem 0; margin-bottom: 2rem; }")
            .append(".card { margin-bottom: 1.5rem; border-radius: 0.5rem; box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075); }")
            .append(".card-header { font-weight: 600; }")
            .append(".service-up { color: #28a745; }")
            .append(".service-down { color: #dc3545; }")
            .append(".instance-item { border-left: 4px solid #6c757d; padding-left: 1rem; margin-bottom: 0.5rem; }")
            .append(".badge-primary { background-color: #007bff; }")
            .append(".badge-success { background-color: #28a745; }")
            .append(".footer { margin-top: 3rem; padding: 1.5rem 0; background-color: #f8f9fa; border-top: 1px solid #dee2e6; }")
            .append("</style>")
            .append("</head>")
            .append("<body>");
            
        // Header
        html.append("<div class=\"header\">")
            .append("<div class=\"container\">")
            .append("<h1>E-Commerce Microservices Dashboard</h1>")
            .append("<p class=\"lead\">API Gateway and Service Registry Status</p>")
            .append("</div>")
            .append("</div>");
            
        // Main content
        html.append("<div class=\"container\">");
            
        // Gateway Info
        html.append("<div class=\"row mb-4\">")
            .append("<div class=\"col-md-6\">")
            .append("<div class=\"card\">")
            .append("<div class=\"card-header bg-primary text-white\">API Gateway Status</div>")
            .append("<div class=\"card-body\">")
            .append("<p><strong>Service Name:</strong> ").append(applicationName).append("</p>")
            .append("<p><strong>Status:</strong> <span class=\"badge bg-success\">Running</span></p>")
            .append("<p><strong>Port:</strong> ").append(serverPort).append("</p>")
            .append("<p><strong>Timestamp:</strong> ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>")
            .append("</div>")
            .append("</div>")
            .append("</div>");
            
        // Quick Access
        html.append("<div class=\"col-md-6\">")
            .append("<div class=\"card\">")
            .append("<div class=\"card-header bg-info text-white\">Quick Access</div>")
            .append("<div class=\"card-body\">")
            .append("<div class=\"list-group\">");
            
        // Add links
        html.append("<a href=\"/info\" class=\"list-group-item list-group-item-action d-flex justify-content-between align-items-center\">")
            .append("Service Info <span class=\"badge bg-primary\">INFO</span></a>");
            
        html.append("<a href=\"/actuator\" class=\"list-group-item list-group-item-action d-flex justify-content-between align-items-center\">")
            .append("Actuator Endpoints <span class=\"badge bg-primary\">ACTUATOR</span></a>");
            
        html.append("<a href=\"/actuator/health\" class=\"list-group-item list-group-item-action d-flex justify-content-between align-items-center\">")
            .append("Health Status <span class=\"badge bg-success\">HEALTH</span></a>");
            
        html.append("</div></div></div></div></div>");
            
        // Registered Services
        html.append("<div class=\"row\">")
            .append("<div class=\"col-12\">")
            .append("<h2 class=\"mb-3\">Registered Microservices</h2>");
            
        List<String> services = discoveryClient.getServices();
        
        if (services.isEmpty()) {
            html.append("<div class=\"alert alert-warning\">No services registered with Eureka</div>");
        } else {
            services.forEach(serviceName -> {
                List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
                boolean isUp = !instances.isEmpty();
                
                html.append("<div class=\"card mb-3\">")
                    .append("<div class=\"card-header d-flex justify-content-between\">")
                    .append("<span>").append(serviceName).append("</span>");
                    
                if (isUp) {
                    html.append("<span class=\"badge bg-success\">UP</span>");
                } else {
                    html.append("<span class=\"badge bg-danger\">DOWN</span>");
                }
                
                html.append("</div>") // end card-header
                    .append("<div class=\"card-body\">");
                
                if (isUp) {
                    html.append("<p><strong>Instances:</strong> ").append(instances.size()).append("</p>");
                    html.append("<div class=\"instances\">");
                    
                    for (ServiceInstance instance : instances) {
                        html.append("<div class=\"instance-item\">")
                            .append("<p><strong>Host:</strong> ").append(instance.getHost()).append("</p>")
                            .append("<p><strong>Port:</strong> ").append(instance.getPort()).append("</p>")
                            .append("<p><strong>URI:</strong> ").append(instance.getUri()).append("</p>")
                            .append("</div>");
                    }
                    
                    html.append("</div>"); // end instances
                } else {
                    html.append("<p class=\"text-muted\">No instances available</p>");
                }
                
                html.append("</div>") // end card-body
                    .append("</div>"); // end card
            });
        }
        
        html.append("</div></div>"); // end col and row
            
        // Footer
        html.append("<footer class=\"footer\">")
            .append("<div class=\"container\">")
            .append("<div class=\"text-center\">")
            .append("<p>&copy; ").append(LocalDateTime.now().getYear()).append(" E-Commerce Microservice System</p>")
            .append("</div>")
            .append("</div>")
            .append("</footer>");
        
        // Scripts
        html.append("<script src=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js\"></script>")
            .append("</body></html>");
        
        return Mono.just(html.toString());
    }
} 