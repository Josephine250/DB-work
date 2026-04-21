package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig — converter removed.
 * Department binding is now handled explicitly via departmentId in DashboardController,
 * so the StringToDepartmentConverter is no longer needed and was causing
 * ConversionFailedException on empty strings from the form select.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // intentionally empty — no custom converters needed
}
