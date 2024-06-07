package dev.starryeye.xmlresponse;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.StringWriter;
import java.time.LocalDateTime;

@Slf4j
@RestController
public class ArticleController {

    @GetMapping("/articles")
    public Mono<ResponseEntity<ArticleResponse>> get() {

        ArticleResponse response = ArticleResponse.builder()
                .title("title")
                .content("content")
                .createdAt(LocalDateTime.of(2024, 6, 7, 20, 24))
                .build();

        // 직접 변환해보기 (object -> xml, marshal)
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ArticleResponse.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(response, stringWriter);
            log.info("xml = {}", stringWriter.toString());
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }

        return Mono.just(
                ResponseEntity.accepted()
//                .header()
                .body(response)
        );
    }
}