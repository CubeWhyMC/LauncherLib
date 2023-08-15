package org.cubewhy.lunarcn;

import org.cubewhy.server.LoginService;
import org.junit.jupiter.api.Test;

public class TestService {
    @Test
    public void test() throws InterruptedException {
        LoginService service = new LoginService();
        service.addAccount("https://cubewhy.eu.org/test1");
        service.addAccount("https://cubewhy.eu.org/test2");
        service.addAccount("https://cubewhy.eu.org/test3");
        service.startServer();
        while (true) {
            Thread.sleep(1000);
        }
    }
}
