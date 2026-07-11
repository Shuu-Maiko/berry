package com.shuu.berry.security;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import org.springframework.stereotype.Component;

@Component
public class SsrfValidator {

  public void validateUrl(String urlString) {
    try {
      URI uri = new URI(urlString);
      String host = uri.getHost();

      if (host == null) {
        throw new SecurityException("Invalid URL format: missing host.");
      }
      InetAddress ip = InetAddress.getByName(host);

      if (ip.isAnyLocalAddress() || ip.isLoopbackAddress() || ip.isLinkLocalAddress() || ip.isSiteLocalAddress()) {
        throw new SecurityException("SSRF Blocked: Cannot ping internal networks.");
      }

    } catch (UnknownHostException e) {
      throw new SecurityException("SSRF Blocked: Unknown host.");
    } catch (Exception e) {
      if (e instanceof SecurityException) {
        throw (SecurityException) e;
      }
      throw new SecurityException("SSRF Blocked: Invalid URL syntax.");
    }
  }
}
