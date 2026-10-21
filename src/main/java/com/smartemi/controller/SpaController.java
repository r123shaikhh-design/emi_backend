package com.smartemi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Serves the built React app (static/index.html) for client-side routes when deployed as one jar. */
@Controller
public class SpaController {
    @GetMapping({"/", "/calculator", "/compare", "/dashboard", "/history", "/login", "/register"})
    String index() { return "forward:/index.html"; }
}
