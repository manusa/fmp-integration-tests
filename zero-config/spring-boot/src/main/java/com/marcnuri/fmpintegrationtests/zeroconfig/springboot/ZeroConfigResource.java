/*
 * HelloWorldResource.java
 *
 * Created on 2019-10-30, 13:25
 */
package com.marcnuri.fmpintegrationtests.zeroconfig.springboot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Marc Nuri <marc@marcnuri.com> on 2019-10-30.
 */
@RestController
@RequestMapping("/")
public class ZeroConfigResource {

  @GetMapping
  public String zeroConfig() {
    return "Zero Config";
  }
}
