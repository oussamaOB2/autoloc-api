package tn.esprit.autoloc.service;

import org.springframework.stereotype.Service;
import tn.esprit.autoloc.Utils;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.repository.IClientRepository;

@Service
public class ClientServiceImpl extends AbstractCrudService<Client> implements IClientService {

    public ClientServiceImpl(IClientRepository repository) {
        super(repository, "Client", Client::getIdClient);
    }

    @Override
    public Client add(Client client) {
        if (!Utils.isNotEmpty(client.getNom())) {
            throw new IllegalArgumentException("Le nom du client est obligatoire");
        }
        return super.add(client);
    }
}
