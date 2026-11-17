package tn.esprit.autoloc.io;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tn.esprit.autoloc.domain.Agence;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleRapportDecoratorTest {

    private final ByteArrayOutputStream console = new ByteArrayOutputStream();
    private final PrintStream original = System.out;

    @AfterEach
    void restore() {
        System.setOut(original);
    }

    private Agence agence() {
        Agence a = new Agence();
        a.setIdAgence(1L);
        a.setNom("Tunis");
        a.setTelephone("20000000");
        return a;
    }

    @Test
    void decorator_printsHeaderOnConsoleAndReturnsDelegateResultUnchanged() {
        BasicRapport delegate = new BasicRapport();
        ConsoleRapportDecorator decorator = new ConsoleRapportDecorator(delegate);

        System.setOut(new PrintStream(console, true, StandardCharsets.UTF_8));
        String attendu = delegate.construire(agence(), 6000.0);
        String obtenu = decorator.construire(agence(), 6000.0);

        assertEquals(attendu, obtenu, "le décorateur retourne le résultat du délégué, inchangé");

        String imprime = console.toString(StandardCharsets.UTF_8);
        assertTrue(imprime.contains("Agence #1"), imprime);
        assertTrue(imprime.contains("Tunis"), imprime);
    }
}
