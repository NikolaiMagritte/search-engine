package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.persistence.Index;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "page", indexes = {@Index(name = "idx_path", columnList = "path")})
@Getter
@Setter
public class PageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "site_id", referencedColumnName = "id")
    private SiteEntity siteId;

    @Column(length = 1000, columnDefinition = "VARCHAR(515)", nullable = false)
    private String path;

    @Column(nullable = false)
    private int code;

    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "pageId", cascade = CascadeType.ALL)
    protected List<IndexEntity> index = new ArrayList<>();
}