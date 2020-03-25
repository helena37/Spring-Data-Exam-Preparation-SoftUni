package softuni.exam.services;


import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dtos.TeamSeedRootDto;
import softuni.exam.models.entities.Picture;
import softuni.exam.models.entities.Team;
import softuni.exam.repositories.TeamRepository;
import softuni.exam.utils.ValidationUtil;
import softuni.exam.utils.XmlParser;


import javax.transaction.Transactional;
import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static softuni.exam.constants.GlobalConstants.*;


@Service
@Transactional
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final XmlParser xmlParser;
    private final PictureService pictureService;

    public TeamServiceImpl(TeamRepository teamRepository, ModelMapper modelMapper, ValidationUtil validationUtil, XmlParser xmlParser, PictureService pictureService) {
        this.teamRepository = teamRepository;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.xmlParser = xmlParser;
        this.pictureService = pictureService;
    }


    @Override
    public String importTeams() throws JAXBException, FileNotFoundException {
        StringBuilder sb = new StringBuilder();
        TeamSeedRootDto teamSeedRootDto =
                this.xmlParser.unmarshalFromFile(TEAMS_XML_FILE_PATH, TeamSeedRootDto.class);

        teamSeedRootDto
                .getTeams()
                .forEach(teamSeedDto -> {
                    if (this.validationUtil.isValid(teamSeedDto)) {
                        if (this.teamRepository.findByName(teamSeedDto.getName()) == null) {
                            if (this.pictureService.getPictureByUrl(teamSeedDto.getPicture().getUrl()) != null) {
                                Team team = this.modelMapper.map(teamSeedDto, Team.class);
                                Picture picture = this.pictureService.getPictureByUrl(teamSeedDto.getPicture().getUrl());
                                team.setPicture(picture);
                                this.teamRepository.saveAndFlush(team);
                                sb.append("Successfully imported - ")
                                        .append(team.getName());
                            }
                        } else {
                            sb.append("Already in DB");
                        }
                    } else {
                        sb.append("Invalid team");
                    }
                    sb.append(System.lineSeparator());
                });
        return sb.toString();
    }

    @Override
    public boolean areImported() {
        return this.teamRepository.count() > 0;
    }

    @Override
    public String readTeamsXmlFile() throws IOException {
        return Files.readString(Path.of(TEAMS_XML_FILE_PATH));
    }

    @Override
    public Team getTeamByName(String name) {
        return this.teamRepository.findByName(name);
    }

}
