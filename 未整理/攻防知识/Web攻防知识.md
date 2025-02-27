##### 1. CSRF防御基础策略
* 尽量使用POST请求，限制GET请求

* 将cookie设置为HttpOnly

  - CSRF攻击大程度上利用了浏览器的cookie，为了防止站内XSS漏洞盗取cookie，需要在cookie中设置“HttpOnly”属性，这样通过程序（如JavaScript脚本、Applet等）就无法获取到cookie信息，避免了攻击者伪造cookie的情况

  - 在Java中Servlet设置cookie代码

    ```java
    response.setHeader("Set-Cookie", "cookiename=cookievalue:HttpOnly");
    ```

* 通过referer识别

  - Http请求会有refer信息，表示请求的来源，识别请求来源可以大概率拦截CSRF

