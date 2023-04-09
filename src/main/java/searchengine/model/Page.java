package searchengine.model;

import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Entity
@Table(name = "page", indexes = @javax.persistence.Index(columnList = "path"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"site_id", "path"}))
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false, columnDefinition = "INT")
    private Site site;

    @Column(nullable = false, columnDefinition = "VARCHAR(511)", length = 511)
    private String path;

    @Column(nullable = false, columnDefinition = "INT")
    private Integer code;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    @ToString.Exclude
    private String content;

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private Set<Index> indices = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Page page = (Page) o;
        return Objects.equals(site, page.site) && Objects.equals(path, page.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(site, path);
    }
}
