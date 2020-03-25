package softuni.exam.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import softuni.exam.constants.GlobalConstants.*;
import softuni.exam.models.dtos.PictureSeedRootDto;
import softuni.exam.models.entities.Picture;
import softuni.exam.repositories.PictureRepository;
import softuni.exam.utils.ValidationUtil;
import softuni.exam.utils.XmlParser;


import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static softuni.exam.constants.GlobalConstants.*;


@Service
public class PictureServiceImpl implements PictureService {
    private final PictureRepository pictureRepository;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final XmlParser xmlParser;

    @Autowired
    public PictureServiceImpl(PictureRepository pictureRepository, ModelMapper modelMapper, ValidationUtil validationUtil, XmlParser xmlParser) {
        this.pictureRepository = pictureRepository;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.xmlParser = xmlParser;
    }

    @Override
    public String importPictures() throws IOException, JAXBException {
        StringBuilder sb = new StringBuilder();

        PictureSeedRootDto pictureSeedRootDto =
                this.xmlParser.unmarshalFromFile(PICTURES_XML_FILE_PATH, PictureSeedRootDto.class);
        pictureSeedRootDto
                .getPictures()
                .forEach(pictureSeedDto -> {
                    if (this.validationUtil.isValid(pictureSeedDto)) {
                        if (this.pictureRepository.findByUrl(pictureSeedDto.getUrl()) == null) {
                            Picture picture =
                                    this.modelMapper.map(pictureSeedDto, Picture.class);
                            this.pictureRepository.saveAndFlush(picture);
                            sb.append("Successfully imported picture - ")
                            .append(picture.getUrl());
                        } else {
                            sb.append("Already in DB");
                        }
                    } else {
                        sb.append("Invalid picture");
                    }
                    sb.append(System.lineSeparator());
                });
       return sb.toString();
    }

    @Override
    public boolean areImported() {
        return this.pictureRepository.count() > 0;
    }

    @Override
    public String readPicturesXmlFile() throws IOException {
        return Files.readString(Path.of(PICTURES_XML_FILE_PATH));
    }

    @Override
    public Picture getPictureByUrl(String url) {
        return this.pictureRepository.findByUrl(url);
    }


}
