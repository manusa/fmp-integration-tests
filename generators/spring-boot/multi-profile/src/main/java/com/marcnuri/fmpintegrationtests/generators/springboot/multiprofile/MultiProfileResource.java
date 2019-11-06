/*
 * MultiProfileResource.java
 *
 * Created on 2019-11-06, 6:58
 */
package com.marcnuri.fmpintegrationtests.generators.springboot.multiprofile;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Marc Nuri <marc@marcnuri.com> on 2019-11-06.
 */
@RestController
@RequestMapping("/")
public class MultiProfileResource {

  private final String response;

  public MultiProfileResource(@Value("${multi-profile.response}") String response) {
    this.response = response;
  }

  @GetMapping
  public String multiProfile() {
    return response;
  }
}
