package dev.starryeye.xmlresponse;

import com.fasterxml.jackson.annotation.JsonRootName;
import dev.starryeye.xmlresponse.config.LocalDateTimeAdapter;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@XmlRootElement(name = "article")
@JsonRootName(value = "article")
@Getter
@NoArgsConstructor
public class ArticleResponse {

    private String title;
    private String content;
    private LocalDateTime createdAt;
    private String nullProperty = null;

    @Builder
    private ArticleResponse(String title, String content, LocalDateTime createdAt) {
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }

    @XmlElement(name = "title_") // 모든 필드에 이걸 해줘야 xml 의 프로퍼티로 만들어준다..
    public void setTitle(String title) {
        this.title = title;
    }

    @XmlElement(name = "created_at")
    @XmlJavaTypeAdapter(value = LocalDateTimeAdapter.class)
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
