package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.service.IReservationService;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Gestion des réservations")
public class ReservationController extends AbstractCrudController<Reservation> {

    public ReservationController(IReservationService service) {
        super(service);
    }
}
