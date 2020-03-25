package softuni.exam.services;


import java.io.FileNotFoundException;
import java.io.IOException;

public interface PlayerService {
    String importPlayers() throws FileNotFoundException;

    boolean areImported();

    String readPlayersJsonFile() throws IOException;

    String exportPlayersInATeam();

    public String exportPlayersWhereSalaryBiggerThan();
}
