package tn.esprit.autoloc.io;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import tn.esprit.autoloc.domain.Agence;

/**
 * Décorateur GoF (atelier 8, étape 14) : imprime l'en-tête sur la console, puis délègue.
 * Le résultat retourné est celui du délégué, inchangé.
 * {@code @Primary} = l'annotation qui fait de ce décorateur l'bean injecté quand on demande
 * un {@link RapportAgence} ; le bean de base est désigné par son nom via {@code @Qualifier}
 * (pas d'auto-injection : le décorateur ne s'injecte jamais lui-même).
 */
@Primary
@Component
public class ConsoleRapportDecorator implements RapportAgence {

    private final RapportAgence delegate;

    public ConsoleRapportDecorator(@Qualifier("basicRapport") RapportAgence delegate) {
        this.delegate = delegate;
    }

    @Override
    public String construire(Agence agence, double total) {
        System.out.print(BasicRapport.entete(agence));
        return delegate.construire(agence, total);
    }
}
