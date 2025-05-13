package com.springboot.EventApp.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * This class is only used to redirect the root to the swagger
 *
 * @author Lucas Horn
 */
@Controller
public class RedirectController {

    @Operation(summary = "redirect root:8080/ to the swagger documentation")
    @GetMapping("/")
    public String redirectToSwagger() {
        return "redirect:/swagger-ui/index.html";
    }
}