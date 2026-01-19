package com.gege.activitypartner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for serving static pages like privacy policy, terms of service, etc. These pages are
 * publicly accessible without authentication.
 */
@Controller
public class PageController {

  @GetMapping("/privacy")
  public String privacy() {
    return "privacy";
  }

  @GetMapping("/terms")
  public String terms() {
    return "terms";
  }

  @GetMapping("/support")
  public String support() {
    return "support";
  }

  @GetMapping("/delete-account")
  public String deleteAccount() {
    return "delete-account";
  }

  @GetMapping("/admin")
  public String admin() {
    return "admin";
  }
}
