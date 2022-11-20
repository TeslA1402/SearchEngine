package searchengine.model;

import lombok.*;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Entity
@Table(name = "`index`", uniqueConstraints = @UniqueConstraint(columnNames = {"page_id", "lemma_id"}))
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "page_id", nullable = false, columnDefinition = "INT")
    private Page page;

    @ManyToOne
    @JoinColumn(name = "lemma_id", nullable = false, columnDefinition = "INT")
    private Lemma lemma;

    @Column(nullable = false, name = "`rank`", columnDefinition = "FLOAT")
    private Float rank;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Index index = (Index) o;
        return Objects.equals(page, index.page) && Objects.equals(lemma, index.lemma);
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, lemma);
    }
}
