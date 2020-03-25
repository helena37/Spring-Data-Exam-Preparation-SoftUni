package softuni.exam.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import softuni.exam.models.entities.Player;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Player findByFirstNameAndLastName(String firstName, String lastName);
    List<Player> findAllByTeamName(String name);
    List<Player> findAllBySalaryGreaterThanOrderBySalaryDesc(BigDecimal salary);
}
