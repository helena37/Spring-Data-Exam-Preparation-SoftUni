package softuni.exam.services;


import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dtos.PlayerSeedDto;
import softuni.exam.models.entities.Picture;
import softuni.exam.models.entities.Player;
import softuni.exam.models.entities.Team;
import softuni.exam.repositories.PlayerRepository;
import softuni.exam.utils.ValidationUtil;


import javax.transaction.Transactional;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static softuni.exam.constants.GlobalConstants.*;


@Service
@Transactional
public class PlayerServiceImpl implements PlayerService {
    private final PlayerRepository playerRepository;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final Gson gson;
    private final TeamService teamService;
    private final PictureService pictureService;

    public PlayerServiceImpl(PlayerRepository playerRepository, ModelMapper modelMapper, ValidationUtil validationUtil, Gson gson, TeamService teamService, PictureService pictureService) {
        this.playerRepository = playerRepository;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.gson = gson;
        this.teamService = teamService;
        this.pictureService = pictureService;
    }

    @Override
    public String importPlayers() throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();
        PlayerSeedDto[] playerSeedDtos =
                this.gson
                .fromJson(new FileReader(PLAYERS_JSON_FILE_PATH), PlayerSeedDto[].class);
        Arrays.stream(playerSeedDtos)
                .forEach(playerSeedDto -> {
                    if (this.validationUtil.isValid(playerSeedDto)) {
                        if (this.playerRepository.findByFirstNameAndLastName(
                                playerSeedDto.getFirstName(), playerSeedDto.getLastName()) == null) {
                            Player player = this.modelMapper.map(playerSeedDto, Player.class);

                            Team team = this.teamService.getTeamByName(playerSeedDto.getTeam().getName());
                            player.setTeam(team);

                            Picture picture = this.pictureService.getPictureByUrl(playerSeedDto.getPicture().getUrl());
                            player.setPicture(picture);

                            this.playerRepository.saveAndFlush(player);

                            sb.append("Successfully imported player - ")
                                    .append(playerSeedDto.getFirstName())
                                    .append(" ")
                                    .append(playerSeedDto.getLastName());
                        } else {
                            sb.append("Already in DB");
                        }
                    } else {
                        sb.append("Invalid player");
                    }
                    sb.append(System.lineSeparator());
                });
       return sb.toString();
    }

    @Override
    public boolean areImported() {
        return this.playerRepository.count() > 0;
    }

    @Override
    public String readPlayersJsonFile() throws IOException {
        return Files.readString(Path.of(PLAYERS_JSON_FILE_PATH));
    }

    @Override
    public String exportPlayersWhereSalaryBiggerThan() {
       StringBuilder sb = new StringBuilder();

       this.playerRepository
               .findAllBySalaryGreaterThanOrderBySalaryDesc(BigDecimal.valueOf(100000))
               .forEach(player -> {
                   sb.append(String.format(
                           "Player name: %s %s \n" +
                                   "\tNumber: %d\n" +
                                   "\tSalary: %.2f\n" +
                                   "\tTeam: %s",
                           player.getFirstName(),
                           player.getLastName(),
                           player.getNumber(),
                           player.getSalary(),
                           player.getTeam().getName()
                   )).append(System.lineSeparator());
               });
        return sb.toString();
    }

    @Override
    public String exportPlayersInATeam() {
        StringBuilder sb = new StringBuilder();

                this.playerRepository
                        .findAllByTeamName("North Hub")
                .forEach(player -> {
                    sb.append(String.format(
                                    "Player name: %s %s - %s\n" +
                                    "Number: %d",
                            player.getFirstName(),
                            player.getLastName(),
                            player.getPosition(),
                            player.getNumber()
                    )).append(System.lineSeparator());
                });

        return sb.toString();
    }


}
