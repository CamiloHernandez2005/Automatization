package com.example.demo.e2e;

import com.example.demo.config.TestConfig;
import com.example.demo.e2e.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Login E2E")
class LoginTest extends BasePlaywrightTest {

    @Test
    @DisplayName("Login exitoso con credenciales válidas")
    void loginConCredencialesValidas() {
        String user = TestConfig.getString("login.username");
        String password = TestConfig.getString("login.password");

        String urlAntesDelLogin = page.url();

        new LoginPage(page)
                .navigate(BASE_URL)
                .loginAs(user, password);

        String urlDespuesDelLogin = page.url();

        assertNotEquals(urlAntesDelLogin, urlDespuesDelLogin,
                "La URL no cambió tras el login, posible fallo de autenticación");

        boolean sinMensajeError = page.locator(
                "text=/invalid|incorrect|error|incorrecto|inválido/i").count() == 0;
        assertTrue(sinMensajeError,
                "Se detectó un mensaje de error en la página tras intentar iniciar sesión");
    }
}
