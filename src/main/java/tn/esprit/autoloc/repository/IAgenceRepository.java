package tn.esprit.autoloc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.autoloc.domain.Agence;

@Repository // Q11: Annotation Bean appropriée
public interface IAgenceRepository extends JpaRepository<Agence, Long> {
}