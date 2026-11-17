package tn.esprit.autoloc.io;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Fabrique de rapports (atelier 8, étape 14) — écho du {@code PaymentFactory.getPayment(type)}
 * du cours : on choisit l'objet rapport à partir d'un type ; type inconnu → IllegalArgumentException.
 * {@code @Component} : c'est Spring qui injecte les deux rapports (le cours montre des champs
 * @Autowired dans une classe sans @Component — à ne pas copier).
 */
@Component
public class RapportFactory {

    private final RapportAgence texte;
    private final RapportAgence console;

    public RapportFactory(@Qualifier("basicRapport") RapportAgence texte,
                          @Qualifier("consoleRapportDecorator") RapportAgence console) {
        this.texte = texte;
        this.console = console;
    }

    public RapportAgence getRapport(String type) {
        return switch (type) {
            case "TEXTE" -> texte;
            case "CONSOLE" -> console;   // en-tête imprimé + texte retourné
            default -> throw new IllegalArgumentException("Type de rapport inconnu : " + type);
        };
    }
}
