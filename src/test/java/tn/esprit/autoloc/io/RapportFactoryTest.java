package tn.esprit.autoloc.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RapportFactoryTest {

    private RapportFactory factory() {
        BasicRapport basic = new BasicRapport();
        return new RapportFactory(basic, new ConsoleRapportDecorator(basic));
    }

    @Test
    void getRapport_CONSOLE_returnsDecorator() {
        assertInstanceOf(ConsoleRapportDecorator.class, factory().getRapport("CONSOLE"));
    }

    @Test
    void getRapport_TEXTE_returnsBasicRapport() {
        assertInstanceOf(BasicRapport.class, factory().getRapport("TEXTE"));
    }

    @Test
    void getRapport_unknownType_throwsIllegalArgument() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> factory().getRapport("XML"));
        assertTrue(ex.getMessage().contains("XML"), ex.getMessage());
    }
}
